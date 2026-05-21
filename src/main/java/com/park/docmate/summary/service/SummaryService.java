package com.park.docmate.summary.service;

import com.park.docmate.document.Document;
import com.park.docmate.document.repository.DocumentRepository;
import com.park.docmate.summary.Summary;
import com.park.docmate.summary.repository.SummaryRepository;
import com.park.docmate.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@Service
public class SummaryService {

    private static final Logger log = LoggerFactory.getLogger(SummaryService.class);

    private final SummaryRepository summaryRepository;
    private final DocumentRepository documentRepository;
    private final AiSummaryClient aiSummaryClient;

    public SummaryService(SummaryRepository summaryRepository, DocumentRepository documentRepository, AiSummaryClient aiSummaryClient) {
        this.summaryRepository = summaryRepository;
        this.documentRepository = documentRepository;
        this.aiSummaryClient = aiSummaryClient;
    }

    //書類IDを元にAI要約を作成する
    @Transactional
    public Summary createSummary(Long documentId, User user){

        //本人の書類だけを取得する
        Document document = findUserDocument(documentId,user);

        //MVP段階ではtxtファイルだけ要約対象にする
        validateTextFile(document);

        // 実ファイルの内容を読み込む
        String content = readDocumentContent(document);

        // まずは本物のAIではなく、テスト用の要約文を作成する
        String summaryText = aiSummaryClient.summarize(content);

        //要約結果をDBに保存する
        Summary summary = new Summary(document,user,summaryText);
        Summary savedSummary = summaryRepository.save(summary);

        log.info("Document summary created: userId={}, documentId={}, summaryId={}, originalFileName={}",
                user.getId(), document.getId(), savedSummary.getId(), document.getOriginalFileName());

        return savedSummary;
    }

    // 書類IDとユーザー情報をもとに、本人の書類だけを取得する
    private Document findUserDocument(Long documentId,User user){
        return documentRepository.findByIdAndUser(documentId, user)
                .orElseThrow(() -> {
                    log.warn("Summary failed. Document not found or access denied: userId={}, documentId={}",
                            user.getId(),
                            documentId);

                    return new IllegalArgumentException("書類が見つからないか、アクセス権限がありません。");
                });
    }

    // MVP段階ではtxtファイルのみ許可する
    private void validateTextFile(Document document){

        String fileType = document.getFileType();
        String originalFileName = document.getOriginalFileName();

        boolean isTextFile =
                "text/plain".equalsIgnoreCase(fileType)
                        || originalFileName != null
                        && originalFileName.toLowerCase(Locale.ROOT).endsWith(".txt");

        if (!isTextFile) {
            log.warn("Summary failed. Unsupported file type: documentId={}, originalFileName={}, fileType={}",
                    document.getId(),
                    document.getOriginalFileName(),
                    document.getFileType());

            throw new IllegalArgumentException("現在、AI要約はtxtファイルのみ対応しています。");
        }

        Path filePath = Path.of(document.getFilePath());

        //実ファイルが存在するか確認する
        if (!Files.exists(filePath)) {
            log.warn("Summary failed. File does not exist: documentId={}, originalFileName={}, filePath={}",
                    document.getId(),
                    document.getOriginalFileName(),
                    document.getFilePath());

            throw new IllegalArgumentException("要約対象のファイルが存在しません。");
        }

        // 通常のファイルか確認する
        if (!Files.isRegularFile(filePath)) {
            log.warn("Summary failed. Path is not a regular file: documentId={}, originalFileName={}, filePath={}",
                    document.getId(),
                    document.getOriginalFileName(),
                    document.getFilePath());

            throw new IllegalArgumentException("要約対象が正しいファイルではありません。");
        }

        // 読み取り可能か確認する
        if (!Files.isReadable(filePath)) {
            log.warn("Summary failed. File is not readable: documentId={}, originalFileName={}, filePath={}",
                    document.getId(),
                    document.getOriginalFileName(),
                    document.getFilePath());

            throw new IllegalArgumentException("要約対象のファイルを読み取ることができません。");
        }
    }

    // 実ファイルの内容を文字列として読み込む
    private String readDocumentContent(Document document) {
        try{
            return Files.readString(Path.of(document.getFilePath()), StandardCharsets.UTF_8);
        }catch (IOException e) {
            log.error("Summary failed. File read error: documentId={}, originalFileName={}, filePath={}",
                    document.getId(),
                    document.getOriginalFileName(),
                    document.getFilePath(),
                    e);

            throw new IllegalStateException("ファイル内容の読み取りに失敗しました。");
        }
    }
}
