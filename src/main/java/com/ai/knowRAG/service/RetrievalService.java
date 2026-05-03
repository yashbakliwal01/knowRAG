package com.ai.knowRAG.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.knowRAG.entity.Document;
import com.ai.knowRAG.exception.EmbeddingException;
import com.ai.knowRAG.repository.DocumentRepository;

@Service
public class RetrievalService {

	private static final Logger log = LoggerFactory.getLogger(RetrievalService.class);
	
	@Autowired
	private DocumentRepository documentRepository;

	@Autowired
	private VectorSearchService vectorSearchService;
	
	public RetrievalService(DocumentRepository documentRepository, VectorSearchService vectorSearchService) {
		this.documentRepository = documentRepository;
		this.vectorSearchService = vectorSearchService;
	}
	
	/**
	 * Retrieve context using semantic/vector search
	 * Falls back to keyword search if semantic search fails or returns no results
	 */
	public String retrieveContext(String query) {
		if (query == null || query.trim().isEmpty()) {
			return "NO_DATA_FOUND";
		}

		String normalized = normalizeQuery(query);
		log.info("Retrieving context for normalized query: {}", normalized);

		try {
			// Try semantic search first
			List<Document> semanticResults = vectorSearchService.semanticSearch(normalized);
			
			if (!semanticResults.isEmpty()) {
				log.info("Semantic search found {} relevant documents", semanticResults.size());
				return semanticResults.stream()
						.map(Document::getContent)
						.collect(Collectors.joining("\n---\n"));
			}
			
			log.warn("Semantic search returned no results. Falling back to keyword search...");
		} catch (EmbeddingException e) {
			log.warn("Semantic search failed: {}. Falling back to keyword search...", e.getMessage());
		}

		// Fallback to keyword search
		List<Document> keywordResults = documentRepository.search(normalized);
		
		if (keywordResults.isEmpty()) {
			log.warn("No documents found for query: {}", normalized);
			return "NO_DATA_FOUND";
		}

		log.info("Keyword search found {} documents", keywordResults.size());
		return keywordResults.stream()
				.limit(3)
				.map(Document::getContent)
				.collect(Collectors.joining("\n---\n"));
	}

	/**
	 * Normalize query by extracting keywords
	 * Maps semantic variations to standard keywords
	 */
	private String normalizeQuery(String query) {
		String q = query.toLowerCase();
		
		// Leave related queries
		if(q.contains("leave") || q.contains("vacation") || q.contains("time off") || 
		   q.contains("paid leave") || q.contains("how many leaves") || q.contains("leaves policy")) {
			return "leave";
		}
		
		// Work from home related
		if(q.contains("work from home") || q.contains("remote") || q.contains("wfh") || 
		   q.contains("remote work") || q.contains("work remotely") || q.contains("working from home")) {
			return "wfh";
		}
		
		// Holidays
	    if (q.contains("holiday") || q.contains("festival") || q.contains("public holiday") || 
	        q.contains("holidays") || q.contains("festive")) {
	        return "holiday";
	    }
	    
	    return query;
	}
}