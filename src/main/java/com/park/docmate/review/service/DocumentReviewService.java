package com.park.docmate.review.service;

import com.park.docmate.document.Document;
import com.park.docmate.document.repository.DocumentRepository;
import com.park.docmate.review.DocumentReview;
import com.park.docmate.review.repository.DocumentReviewRepository;
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
public class DocumentReviewService {

    private static final Logger log = LoggerFactory.getLogger(DocumentReviewService.class);

    private final DocumentReviewRepository documentReviewRepository;
    private final DocumentRepository documentRepository;
    private final AiReviewClient aiReviewClient;

    public DocumentReviewService(DocumentReviewRepository documentReviewRepository, DocumentRepository documentRepository, AiReviewClient aiReviewClient) {
        this.documentReviewRepository = documentReviewRepository;
        this.documentRepository = documentRepository;
        this.aiReviewClient = aiReviewClient;
    }

    // 書類IDとログインユーザー情報をもとに、本人の書類だけAIレビューする
    @Transactional
    public DocumentReview createReview(Long documentId, User user){

        //本人の書類だけを取得する
        Document document = findUserDocument(documentId, user);

        // MVP段階ではtxtファイルだけレビュー対象にする
        validateTextFile(document);

        // 実ファイルの内容を読み込む
        String content = readDocumentContent(document);

        // AIレビュークライアントを使ってレビュー結果を作成する
        String reviewText = aiReviewClient.review(content);

        // レビュー結果をDBに保存する
        DocumentReview documentReview = new DocumentReview(document, user, reviewText);
        DocumentReview savedReview = documentReviewRepository.save(documentReview);

        log.info("Document review created: userId={}, documentId={}, reviewId={}, originalFileName={}",
                user.getId(),
                document.getId(),
                savedReview.getId(),
                document.getOriginalFileName());

        return savedReview;
    }

    // 書類IDとユーザー情報をもとに、本人の書類だけを取得する
    private Document findUserDocument(Long documentId, User user) {
        return documentRepository.findByIdAndUser(documentId, user)
                .orElseThrow(() -> {
                    log.warn("Review failed. Document not found or access denied: userId={}, documentId={}", user.getId(), documentId);

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
            log.warn("Review failed. Unsupported file type: documentId={}, originalFileName={}, fileType={}",
                    document.getId(),
                    document.getOriginalFileName(),
                    document.getFileType());

            throw new IllegalArgumentException("現在、AI書類レビューはtxtファイルのみ対応しています。");
        }

        Path filePath = Path.of(document.getFilePath());

        // 実ファイルが存在するか確認する
        if (!Files.exists(filePath)) {
            log.warn("Review failed. File does not exist: documentId={}, originalFileName={}, filePath={}",
                    document.getId(),
                    document.getOriginalFileName(),
                    document.getFilePath());

            throw new IllegalArgumentException("レビュー対象のファイルが存在しません。");
        }

        // 通常のファイルか確認する
        if (!Files.isRegularFile(filePath)) {
            log.warn("Review failed. Path is not a regular file: documentId={}, originalFileName={}, filePath={}",
                    document.getId(),
                    document.getOriginalFileName(),
                    document.getFilePath());

            throw new IllegalArgumentException("レビュー対象が正しいファイルではありません。");
        }

        // 読み取り可能か確認する
        if (!Files.isReadable(filePath)) {
            log.warn("Review failed. File is not readable: documentId={}, originalFileName={}, filePath={}",
                    document.getId(),
                    document.getOriginalFileName(),
                    document.getFilePath());

            throw new IllegalArgumentException("レビュー対象のファイルを読み取ることができません。");
        }
    }

    // 実ファイルの内容を文字列として読み込む
    private String readDocumentContent(Document document) {
        try {
            return Files.readString(Path.of(document.getFilePath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Review failed. File read error: documentId={}, originalFileName={}, filePath={}",
                    document.getId(),
                    document.getOriginalFileName(),
                    document.getFilePath(),
                    e);

            throw new IllegalStateException("ファイル内容の読み取りに失敗しました。");
        }
    }
}
