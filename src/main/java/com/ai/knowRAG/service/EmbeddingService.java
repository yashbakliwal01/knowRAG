package com.ai.knowRAG.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.knowRAG.exception.EmbeddingException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);
    private static final String OLLAMA_EMBEDDINGS_URL = "http://localhost:11434/api/embed";
    private static final String OLLAMA_MODEL = "nomic-embed-text";
    private static final String OPENAI_EMBEDDINGS_URL = "https://api.openai.com/v1/embeddings";
    private static final String OPENAI_MODEL = "text-embedding-3-small";
    
    private final OkHttpClient httpClient;
    private final Gson gson;
    private String provider;
    private String openaiApiKey;

    @Autowired
    public EmbeddingService(OkHttpClient httpClient, Gson gson) {
        this.httpClient = httpClient;
        this.gson = gson;
        this.provider = "ollama"; // Default to ollama
    }

    /**
     * Generate embedding for a given text
     */
    public List<Double> generateEmbedding(String text) throws EmbeddingException {
        if (text == null || text.trim().isEmpty()) {
            throw new EmbeddingException("Text cannot be empty");
        }

        try {
            if ("openai".equalsIgnoreCase(provider)) {
                return generateOpenAIEmbedding(text);
            } else {
                return generateOllamaEmbedding(text);
            }
        } catch (Exception e) {
            log.error("Failed to generate embedding: {}", e.getMessage(), e);
            throw new EmbeddingException("Failed to generate embedding: " + e.getMessage(), e);
        }
    }

    /**
     * Generate embedding using Ollama
     */
    private List<Double> generateOllamaEmbedding(String text) throws Exception {
        log.debug("Generating Ollama embedding for text: {}", text.substring(0, Math.min(50, text.length())));
        
        String jsonPayload = gson.toJson(new EmbeddingRequest(OLLAMA_MODEL, text));
        
        RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(OLLAMA_EMBEDDINGS_URL)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new EmbeddingException("Ollama embedding request failed with status: " + response.code());
            }

            String responseBody = response.body().string();
            EmbeddingResponse embeddingResponse = gson.fromJson(responseBody, EmbeddingResponse.class);
            
            if (embeddingResponse.getEmbedding() == null || embeddingResponse.getEmbedding().isEmpty()) {
                throw new EmbeddingException("Ollama returned empty embedding");
            }

            log.debug("Successfully generated Ollama embedding with dimension: {}", embeddingResponse.getEmbedding().size());
            return embeddingResponse.getEmbedding();
        }
    }

    /**
     * Generate embedding using OpenAI
     */
    private List<Double> generateOpenAIEmbedding(String text) throws Exception {
        log.debug("Generating OpenAI embedding for text: {}", text.substring(0, Math.min(50, text.length())));
        
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            throw new EmbeddingException("OpenAI API key not configured");
        }

        String jsonPayload = gson.toJson(new OpenAIEmbeddingRequest(OPENAI_MODEL, text));
        
        RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(OPENAI_EMBEDDINGS_URL)
                .addHeader("Authorization", "Bearer " + openaiApiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new EmbeddingException("OpenAI embedding request failed with status: " + response.code());
            }

            String responseBody = response.body().string();
            OpenAIEmbeddingResponse embeddingResponse = gson.fromJson(responseBody, OpenAIEmbeddingResponse.class);
            
            if (embeddingResponse.getData() == null || embeddingResponse.getData().isEmpty()) {
                throw new EmbeddingException("OpenAI returned empty embedding");
            }

            List<Double> embedding = embeddingResponse.getData().get(0).getEmbedding();
            log.debug("Successfully generated OpenAI embedding with dimension: {}", embedding.size());
            return embedding;
        }
    }

    /**
     * Calculate cosine similarity between two embeddings
     */
    public double cosineSimilarity(List<Double> embedding1, List<Double> embedding2) {
        if (embedding1 == null || embedding2 == null || embedding1.size() != embedding2.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < embedding1.size(); i++) {
            dotProduct += embedding1.get(i) * embedding2.get(i);
            normA += Math.pow(embedding1.get(i), 2);
            normB += Math.pow(embedding2.get(i), 2);
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // Inner classes for API requests/responses
    public static class EmbeddingRequest {
        public String model;
        public String prompt;

        public EmbeddingRequest(String model, String prompt) {
            this.model = model;
            this.prompt = prompt;
        }
    }

    public static class EmbeddingResponse {
        private List<Double> embedding;

        public List<Double> getEmbedding() {
            return embedding;
        }

        public void setEmbedding(List<Double> embedding) {
            this.embedding = embedding;
        }
    }

    public static class OpenAIEmbeddingRequest {
        public String model;
        public String input;

        public OpenAIEmbeddingRequest(String model, String input) {
            this.model = model;
            this.input = input;
        }
    }

    public static class OpenAIEmbeddingResponse {
        private List<EmbeddingData> data;

        public List<EmbeddingData> getData() {
            return data;
        }

        public void setData(List<EmbeddingData> data) {
            this.data = data;
        }
    }

    public static class EmbeddingData {
        private List<Double> embedding;
        private int index;

        public List<Double> getEmbedding() {
            return embedding;
        }

        public void setEmbedding(List<Double> embedding) {
            this.embedding = embedding;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }
}
