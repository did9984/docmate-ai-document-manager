package com.park.docmate.summary;

import com.park.docmate.document.Document;
import com.park.docmate.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "summaries")
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 要約対象の書類
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    // 要約を作成したユーザー
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //AIが作成した要約内容
    @Column(columnDefinition = "TEXT",nullable = false)
    private String summaryText;

    // 要約作成日時
    private LocalDateTime createdAt;

    public Summary() {
    }

    public Summary(Document document, User user, String summaryText) {
        this.document = document;
        this.user = user;
        this.summaryText = summaryText;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Document getDocument() {
        return document;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
