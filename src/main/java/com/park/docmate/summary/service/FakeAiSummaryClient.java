package com.park.docmate.summary.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "ai.summary.provider",
        havingValue = "fake",
        matchIfMissing = true
)
public class FakeAiSummaryClient implements AiSummaryClient {

    // 本物のAI API連携前に使うテスト用の要約作成処理
    @Override
    public String summarize(String content){

        if (content == null || content.isBlank()){
            throw new IllegalArgumentException("要約対象のファイル内容が空です。");
        }

        String cleanedCount = content
                .replaceAll("\\s+", " ")
                .trim();

        String previewText = cleanedCount;

        if (previewText.length() > 200){
            previewText = previewText.substring(0,200) + "...";
        }

        return "【AI要約テスト】\n"
                + "この書類は、アップロードされたテキスト文書の内容をもとに作成された要約です。\n\n"
                + "要約対象の一部内容：\n"
                + previewText;
    }
}
