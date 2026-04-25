package com.ai.knowRAG.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ai.knowRAG.service.LLMClient;
import com.ai.knowRAG.service.OllamaClient;
import com.ai.knowRAG.service.OpenAiClient;

@Component
public class LLMClientFactory {

    private final OpenAiClient openAiClient;
    private final OllamaClient ollamaClient;

    @Value("${ai.provider}")
    private String provider;

    public LLMClientFactory(OpenAiClient openAiClient,
                            OllamaClient ollamaClient) {
        this.openAiClient = openAiClient;
        this.ollamaClient = ollamaClient;
    }

    public LLMClient getClient() {

        if ("ollama".equalsIgnoreCase(provider)) {
            return ollamaClient;
        }

        return openAiClient;
    }
}