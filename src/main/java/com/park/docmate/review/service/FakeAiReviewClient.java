package com.park.docmate.review.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "ai.review.provider",
        havingValue = "fake",
        matchIfMissing = true
)
public class FakeAiReviewClient implements AiReviewClient{

    // 本物のAI API連携前に使うテスト用の書類レビュー処理
    @Override
    public String review(String content){

        if (content == null || content.isBlank()){
            throw new IllegalArgumentException("レビュー対象のファイル内容が空です。");
        }

        String cleanedContent = content
                .replaceAll("\\s+", " ")
                .trim();

        String previewText = cleanedContent;

        if (previewText.length() > 300){
            previewText = previewText.substring(0,300) + "...";
        }

        return "【AI書類レビューテスト】\n\n"
                + "良い点：\n"
                + "・書類の目的が比較的分かりやすく書かれています。\n"
                + "・Java、Spring Boot、AI機能など、技術的なアピールにつながる要素があります。\n\n"
                + "改善点：\n"
                + "・実装した機能をもう少し具体的に書くと、実務経験に近い印象になります。\n"
                + "・DB設計、ログ、例外処理、セキュリティ面の工夫も書くと評価されやすくなります。\n\n"
                + "面接で使えるアピール例：\n"
                + "・単なるCRUDではなく、ファイルアップロード、ユーザー別管理、AI要約、AIレビューまで拡張できる構成で実装しています。\n\n"
                + "レビュー対象の一部内容：\n"
                + previewText;
    }
}
