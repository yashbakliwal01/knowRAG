package com.ai.knowRAG.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.knowRAG.entity.Document;
import com.ai.knowRAG.exception.EmbeddingException;
import com.ai.knowRAG.exception.ValidationException;
import com.ai.knowRAG.repository.DocumentRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Service
public class VectorSearchService {

    private static final Logger log = LoggerFactory.getLogger(VectorSearchService.class);
    private static final double SIMILARITY_THRESHOLD = 0.6; // 60% similarity threshold
    private static final int TOP_K = 3; // Return top 3 results

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private Gson gson;

    /**
     * Search documents using vector similarity (semantic search)
     */
    public List<Document> semanticSearch(String query, int topK) throws EmbeddingException {
        if (query == null || query.trim().isEmpty()) {
            throw new ValidationException("Query cannot be empty");
        }

        log.info("Starting semantic search for query: {}", query);
        long startTime = System.currentTimeMillis();

        try {
            // Generate embedding for the query
            List<Double> queryEmbedding = embeddingService.generateEmbedding(query);
            log.debug("Generated query embedding with dimension: {}", queryEmbedding.size());

            // Get all documents with embeddings
            List<Document> allDocuments = documentRepository.findAllWithEmbeddings();
            
            if (allDocuments.isEmpty()) {
                log.warn("No documents with embeddings found");
                return new ArrayList<>();
            }

            // Calculate similarity scores
            List<DocumentSimilarity> similarities = allDocuments.stream()
                    .map(doc -> {
                        List<Double> docEmbedding = parseEmbedding(doc.getEmbedding());
                        double similarity = embeddingService.cosineSimilarity(queryEmbedding, docEmbedding);
                        return new DocumentSimilarity(doc, similarity);
                    })
                    .filter(ds -> ds.similarity >= SIMILARITY_THRESHOLD) // Filter by threshold
                    .sorted(Comparator.comparingDouble(DocumentSimilarity::getSimilarity).reversed())
                    .limit(topK)
                    .collect(Collectors.toList());

            long endTime = System.currentTimeMillis();
            log.info("Semantic search completed in {} ms. Found {} relevant documents", 
                    (endTime - startTime), similarities.size());

            return similarities.stream()
                    .map(DocumentSimilarity::getDocument)
                    .collect(Collectors.toList());

        } catch (EmbeddingException e) {
            log.error("Embedding error during semantic search: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during semantic search: {}", e.getMessage(), e);
            throw new EmbeddingException("Semantic search failed: " + e.getMessage(), e);
        }
    }

    /**
     * Overloaded semantic search with default topK
     */
    public List<Document> semanticSearch(String query) throws EmbeddingException {
        return semanticSearch(query, TOP_K);
    }

    /**
     * Initialize embeddings for all documents that don't have them
     */
    public void initializeEmbeddings() throws EmbeddingException {
        log.info("Initializing embeddings for all documents");
        
        List<Document> documentsWithoutEmbeddings = documentRepository.findDocumentsWithoutEmbeddings();
        
        if (documentsWithoutEmbeddings.isEmpty()) {
            log.info("All documents already have embeddings");
            return;
        }

        log.info("Found {} documents without embeddings. Starting generation...", documentsWithoutEmbeddings.size());

        for (Document doc : documentsWithoutEmbeddings) {
            try {
                String textToEmbed = doc.getTitle() + " " + doc.getContent();
                List<Double> embedding = embeddingService.generateEmbedding(textToEmbed);
                
                doc.setEmbedding(gson.toJson(embedding));
                doc.setEmbeddingUpdatedAt(System.currentTimeMillis());
                documentRepository.save(doc);
                
                log.debug("Generated embedding for document: {}", doc.getId());
            } catch (Exception e) {
                log.error("Failed to generate embedding for document {}: {}", doc.getId(), e.getMessage());
            }
        }

        log.info("Embedding initialization completed");
    }

    /**
     * Parse embedding JSON string to List<Double>
     */
    private List<Double> parseEmbedding(String embeddingJson) {
        try {
            if (embeddingJson == null || embeddingJson.isEmpty()) {
                return new ArrayList<>();
            }
            return gson.fromJson(embeddingJson, new TypeToken<List<Double>>(){}.getType());
        } catch (Exception e) {
            log.error("Failed to parse embedding: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Inner class to hold document and its similarity score
     */
    private static class DocumentSimilarity {
        private Document document;
        private double similarity;

        public DocumentSimilarity(Document document, double similarity) {
            this.document = document;
            this.similarity = similarity;
        }

        public Document getDocument() {
            return document;
        }

        public double getSimilarity() {
            return similarity;
        }
    }
}
