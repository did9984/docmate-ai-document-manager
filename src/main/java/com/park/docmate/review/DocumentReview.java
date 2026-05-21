package com.park.docmate.review;

import com.park.docmate.document.Document;
import com.park.docmate.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_reviews")
public class DocumentReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // レビュー対象の書類
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    // レビューを作成したユーザー
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // AIが作成したレビュー内容
    @Column(columnDefinition = "TEXT", nullable = false)
    private String reviewText;

    // レビュー作成日時
    private LocalDateTime createdAt;

    public DocumentReview() {
    }

    public DocumentReview(Document document, User user, String reviewText) {
        this.document = document;
        this.user = user;
        this.reviewText = reviewText;
    }

    // 初回保存前にレビュー作成日時を自動で設定する
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Document getDocument() {
        return document;
    }

    public User getUser() {
        return user;
    }

    public String getReviewText() {
        return reviewText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}