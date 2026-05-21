package com.park.docmate.review.service;

// AI書類レビュー処理を抽象化するためのインターフェース
public interface AiReviewClient {

    // 書類内容を受け取り、レビュー結果を返す
    String review(String content);
}
