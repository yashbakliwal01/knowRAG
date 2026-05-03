package com.ai.knowRAG.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ai.knowRAG.service.LLMClient;
import com.ai.knowRAG.service.OllamaClient;

@Component
public class LLMClientFactory {

    private final OllamaClient ollamaClient;

    @Value("${ai.provider}")
    private String provider;

    public LLMClientFactory(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    public LLMClient getClient() {
        return ollamaClient;
    }
}