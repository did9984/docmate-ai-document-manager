package com.park.docmate.summary.service;

// AI要約処理を抽象化するためのインターフェース
public interface AiSummaryClient {

    // 書類内容を受け取り、要約文を返す
    String summarize(String content);
}
