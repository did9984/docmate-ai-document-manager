package com.park.docmate.review.repository;

import com.park.docmate.document.Document;
import com.park.docmate.review.DocumentReview;
import com.park.docmate.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentReviewRepository extends JpaRepository<DocumentReview, Long> {

    // ログインユーザー本人のレビュー結果だけを取得する
    List<DocumentReview> findByUserOrderByIdDesc(User user);

    // 指定された書類に紐づくレビュー結果を削除する
    void deleteByDocument(Document document);
}
