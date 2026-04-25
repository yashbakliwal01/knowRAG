package com.ai.knowRAG.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.ai.knowRAG.config.LLMClientFactory;

@Service
public class AiService {
	
	private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final RetrievalService retrievalService;
    private final LLMClientFactory factory;
    
    public AiService(RetrievalService retrievalService,
                     LLMClientFactory factory) {
        this.retrievalService = retrievalService;
        this.factory = factory;
    }

    @Cacheable(value = "ai-cache", key = "#userQuery.toLowerCase().trim()")
    public String generateAnswer(String userQuery) {
    	
    	long startTime = System.currentTimeMillis();
    	
    	String normalized = normalize(userQuery);
    	log.info("Incoming query: {}", userQuery);
        log.info("Normalized query: {}", normalized);

    	String context = retrievalService.retrieveContext(normalized);
    	
    	
        if (context == null || context.isBlank() || context.startsWith("No relevant")) {
        	log.warn("No context found for query: {}", normalized);
        	return "Information not available";
        }
        
        log.info("Context fetched successfully");
        String prompt = """
        		You are an enterprise HR assistant.

        		Rules:
        		- Answer ONLY from the given context
        		- If not found, say "Information not available"
        		- Be clear and concise

        		Context:
        		%s

        		User Question:
        		%s

        		Answer:
        		""".formatted(context, userQuery);
        
        log.info("Calling AI model...");
        String response = factory.getClient().chat(prompt);

        long endTime = System.currentTimeMillis();
        log.info("Response generated in {} ms", (endTime - startTime));

        return response;
//        return factory.getClient().chat(prompt);
    }

	private String normalize(String userQuery) {
		return userQuery.toLowerCase().trim().replaceAll("\\s+", " ");
	}
}