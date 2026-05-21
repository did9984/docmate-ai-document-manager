package com.park.docmate.document.service;

import com.park.docmate.document.Document;
import com.park.docmate.document.repository.DocumentRepository;
import com.park.docmate.summary.repository.SummaryRepository;
import com.park.docmate.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.park.docmate.review.repository.DocumentReviewRepository;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DocumentService {

    // このクラスで発生した処理内容をログに出力するためのLogger
    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final SummaryRepository summaryRepository;
    private final DocumentReviewRepository documentReviewRepository;

    //ファイルを保存するフォルダ名
    private final String uploadDir = "uploads";

    public DocumentService(DocumentRepository documentRepository, SummaryRepository summaryRepository, DocumentReviewRepository documentReviewRepository) {
        this.documentRepository = documentRepository;
        this.summaryRepository = summaryRepository;
        this.documentReviewRepository = documentReviewRepository;
    }

    //元のファイル名から拡張子を取得する
    private String getFileExtension(String originalFileName){

        //最後のドットの位置を取得する
        int dotIndex = originalFileName.lastIndexOf(".");

        //拡張子がない場合は穴文字を返す
        if (dotIndex == -1){
            return "";
        }

        //拡張子を小文字で返す
        return originalFileName.substring(dotIndex).toLowerCase();
    }

    //サーバーに保存する安全なファイル名を作成する
    private String createStoredFileName(String originalFileName){

        //元のファイル名から拡張子だけを取り出す
        String extension = getFileExtension(originalFileName);

        // UUIDと拡張子だけで保存用ファイル名を作成する
        return UUID.randomUUID() + extension;
    }

    //書類IDとログイン中のユーザー情報をもとに、本人の書類だけを取得する共通処理
    private Document findUserDocument(Long documentId, User user){
        return documentRepository.findByIdAndUser(documentId, user)
                .orElseThrow(() ->{log.warn("Document not found or access denied: userId={}, documentId={}", user.getId(),documentId);

                return new IllegalArgumentException("書類が見つからないか、アクセス権限がありません。");
        });
    }

    //書類IDとログインのユーザー情報をもとに、本人の書類だけを削除する
    //DBのデータだけでなく、ユーザー上に保存されている実ファイルを削除する
    @Transactional
    public void deleteDocument(Long documentId,User user){

        //本人の書類かどうかを確認しながら、削除対象の書類情報を取得する
        Document document = findUserDocumentForDelete(documentId, user);

        // 書類に紐づくAIレビュー結果を先に削除する
        documentReviewRepository.deleteByDocument(document);

        // 書類に紐づくAI要約結果を先に削除する
        summaryRepository.deleteByDocument(document);

        try{
            //DBに保存されているファイルばすをPathに交換する
            Path filepath = Paths.get(document.getFilePath());

            //実ファイルが存在する場合は削除する
            boolean deleted = Files.deleteIfExists(filepath);

            //実ファイルが存在しなかった場合は警告ログを出力する
            if(!deleted){
                log.warn("Physical file not found during delete: userId={}, documentId={}, filePath={}",
                        user.getId(),
                        document.getId(),
                        document.getFilePath());
            }

            //書類に紐づくAI要約データを先に削除する
            summaryRepository.deleteByDocument(document);

            //DBから書類情報を削除する
            documentRepository.delete(document);

            // 削除成功ログ
            log.info("Document delete success: userId={}, documentId={}, storedFileName={}",
                    user.getId(),
                    document.getId(),
                    document.getStoredFileName());
        }catch (IOException e){
            // 実ファイルの削除に失敗した場合はエラーにする
            throw new RuntimeException("ファイルの削除に失敗しました。", e);
        }
    }

    // 書類IDとユーザー情報をもとに、削除対象の本人書類だけを取得する
    // 削除処理では、実ファイルが存在しない場合でもDBデータを削除できるようにする
    private Document findUserDocumentForDelete(Long documentId, User user) {
        return documentRepository.findByIdAndUser(documentId, user)
                .orElseThrow(() -> {
                    log.warn("Delete failed. Document not found or access denied: userId={}, documentId={}",
                            user.getId(),
                            documentId);

                    return new IllegalArgumentException("書類が見つからないか、アクセス権限がありません。");
                });
    }

    //書類IDとログイン中のユーザー情報をもとに、本人の書類だけを取得する
    // 他のユーザーの書類をダウンロードできないようにするための確認処理
    public Document findDownloadDocument(Long documentId, User user){

        // 書類IDとログイン中のユーザー情報をもとに、本人の書類だけを取得する
        Document document = findUserDocument(documentId, user);

        Path filePath = Paths.get(document.getFilePath());

        // 実ファイルが存在するか確認する
        if (!Files.exists(filePath)) {
            log.warn("Download failed. File does not exist: userId={}, documentId={}, originalFileName={}, storedFileName={}, filePath={}",
                    user.getId(),
                    document.getId(),
                    document.getOriginalFileName(),
                    document.getStoredFileName(),
                    document.getFilePath());

            throw new IllegalArgumentException("ファイルが存在しません。");
        }

        // 通常のファイルか確認する
        if (!Files.isRegularFile(filePath)) {
            log.warn("Download failed. Path is not a regular file: userId={}, documentId={}, originalFileName={}, storedFileName={}, filePath={}",
                    user.getId(),
                    document.getId(),
                    document.getOriginalFileName(),
                    document.getStoredFileName(),
                    document.getFilePath());

            throw new IllegalArgumentException("ダウンロード対象が正しいファイルではありません。");
        }

        // 読み取り可能か確認する
        if (!Files.isReadable(filePath)) {
            log.warn("Download failed. File is not readable: userId={}, documentId={}, originalFileName={}, storedFileName={}, filePath={}",
                    user.getId(),
                    document.getId(),
                    document.getOriginalFileName(),
                    document.getStoredFileName(),
                    document.getFilePath());

            throw new IllegalArgumentException("ファイルを読み取ることができません。");
        }

        log.info("Document download success: userId={}, documentId={}, originalFileName={}, storedFileName={}, fileSize={}, fileType={}",
                user.getId(),
                document.getId(),
                document.getOriginalFileName(),
                document.getStoredFileName(),
                document.getFileSize(),
                document.getFileType());

        return document;
    }

    //ログイン中のユーザーがアップロードした文書一覧を取得する
    public List<Document> findDocumentsByUser(User user){
        // ユーザーに紐づく文書だけを新しい順で取得する
        return documentRepository.findByUserOrderByIdDesc(user);
    }

    public void saveFile(MultipartFile file, User user){
        // アップロード前にファイル内容を検証する
        validateUploadFile(file);

        try{
            //uploads フォルダがなければ作成する
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }

            //元のファイル名
            String originalFileName = file.getOriginalFilename();

            //サーバーに保存尻ファイル名(複数防止)
            String storedFileName = createStoredFileName(originalFileName);

            //保存先パス
            Path filePath = uploadPath.resolve(storedFileName);

            //実際にファイルを保存
            Files.copy(file.getInputStream(),filePath,StandardCopyOption.REPLACE_EXISTING);

            //DBに保存するファイルを保存する
            Document document = new Document(
                    originalFileName,storedFileName,filePath.toString(),file.getContentType(),file.getSize(),user
            );

            //documents テーブルに保存
            documentRepository.save(document);

            // アップロード成功ログ
            log.info("Document upload success: userId={}, originalFileName={}, storedFileName={}, fileSize={}, fileType={}",
                    user.getId(),
                    document.getOriginalFileName(),
                    document.getStoredFileName(),
                    document.getFileSize(),
                    document.getFileType());

        }catch (IOException e){
            throw new RuntimeException("ファイルを保存に失敗した。", e);
        }
    }

    //アップロード成されたファイルが有効かどうかを確認する
    private void validateUploadFile(MultipartFile file){

        //ファイルが空の場合はエラーにする
        if (file == null || file.isEmpty()){
            throw new IllegalArgumentException("アップロードするファイルを選択してください。");
        }

        //元のファイル名を取得する
        String originalFileName = file.getOriginalFilename();

        // ファイル名が存在しない場合はエラーにする
        if (originalFileName == null || originalFileName.isBlank()){
            throw new IllegalArgumentException("ファイル名が正しくありません。");
        }

        //ファイルサイズの上限を設定する
        long maxFileSize = 10 * 1024 * 1024; //10 mb

        //ファイルサイズが上限を超えた場合はエラーにする
        if (file.getSize() > maxFileSize){
            throw new IllegalArgumentException("ファイルサイズは10MB以下にしてください。");
        }

        // ファイル拡張子を小文字に変換する
        String lowerFileName = originalFileName.toLowerCase();

        //許可するファイル形式だけアップロードできるようにする
        if (!(lowerFileName.endsWith(".pdf") || lowerFileName.endsWith(".doc")
                || lowerFileName.endsWith(".docx") || lowerFileName.endsWith(".txt")
                || lowerFileName.endsWith(".png") || lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg"))) {

            throw new IllegalArgumentException("アップロードできるファイル形式は PDF, DOC, DOCX, TXT, PNG, JPG のみです。");
        }
    }
}
