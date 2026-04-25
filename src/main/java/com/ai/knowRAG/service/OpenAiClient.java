package com.ai.knowRAG.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Component
public class OpenAiClient implements LLMClient {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);
    private static final String OPENAI_URL =
            "https://api.openai.com/v1/responses";

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public String chat(String prompt) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }

        String json = """
        {
          "model": "%s",
          "input": "%s"
        }
        """.formatted(
                model,
                prompt.replace("\"", "\\\"").replace("\n", "\\n")
        );

        Request request = new Request.Builder()
                .url(OPENAI_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                if (response.code() == 429) {
                    log.warn("OpenAI rate limit hit (429)");
                    return "AI service is busy. Please try again.";
                }
                throw new IOException("OpenAI API failed: " + response.code());
            }

            ResponseBody body = response.body();
            String raw = body != null ? body.string() : null;

            return extractText(raw); // ✅ FIX

        } catch (Exception e) {
            log.error("OpenAI error", e);
            return "AI service unavailable";
        }
    }

    // Extract actual answer from JSON
    private String extractText(String json) {
        if (json == null) return "No response";

        try {
            // simple parsing (no extra libs)
            int start = json.indexOf("\"text\":\"");
            if (start == -1) return json;

            start += 8;
            int end = json.indexOf("\"", start);

            return json.substring(start, end);

        } catch (Exception e) {
            return json;
        }
    }
}