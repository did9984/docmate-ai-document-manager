package com.park.docmate.summary.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "ai.summary.provider",
        havingValue = "openai"
)
public class OpenAiSummaryClient implements AiSummaryClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiSummaryClient.class);

    private final String apiKey;
    private final String model;
    private final OpenAIClient client;

    public OpenAiSummaryClient(
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.model:gpt-4.1-mini}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;

        // OpenAI APIキーを使ってクライアントを作成する
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    // OpenAI APIを呼び出して、書類内容を要約する
    @Override
    public String summarize(String content) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI APIキーが設定されていません。");
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("要約対象のファイル内容が空です。");
        }

        try {
            // OpenAIに渡す指示文を作成する
            String prompt = createPrompt(content);

            // Chat Completions APIに送信するリクエストを作成する
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(ChatModel.of(model))
                    .addSystemMessage("""
                            あなたは日本IT転職向けポートフォリオサービスのAI要約アシスタントです。
                            アップロードされた応募書類やポートフォリオ資料の内容を、
                            分かりやすく、短く、実務的に要約してください。
                            出力は日本語で行ってください。
                            """)
                    .addUserMessage(prompt)
                    .build();

            // OpenAI APIを呼び出す
            ChatCompletion chatCompletion = client.chat().completions().create(params);

            // 生成された要約文を取得する
            String summaryText = chatCompletion.choices().get(0).message().content()
                    .orElseThrow(() -> new IllegalStateException("AI要約結果が空です。"));

            log.info("OpenAI summary created successfully. model={}", model);

            return summaryText;

        } catch (Exception e) {
            log.error("OpenAI summary failed. model={}", model, e);
            throw new IllegalStateException("AI要約の作成に失敗しました。");
        }
    }

    // AIに渡すプロンプトを作成する
    private String createPrompt(String content) {

        String limitedContent = content;

        // 長すぎるテキストをそのまま送ると料金が増えるため、MVPでは文字数を制限する
        if (limitedContent.length() > 3000) {
            limitedContent = limitedContent.substring(0, 3000);
        }

        return """
                以下の書類内容を要約してください。

                要約ルール：
                ・日本語で出力する
                ・3〜5行で要約する
                ・転職活動で使える観点を意識する
                ・技術スキル、経験、アピールポイントがあれば含める
                ・内容を勝手に大きく盛らない

                書類内容：
                %s
                """.formatted(limitedContent);
    }
}