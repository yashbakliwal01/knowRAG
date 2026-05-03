package com.ai.knowRAG.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.ai.knowRAG.config.LLMClientFactory;
import com.ai.knowRAG.exception.AIServiceException;

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

        try {
        	String context = retrievalService.retrieveContext(normalized);
        	
        	// Check if no data was found in the knowledge base
            if (context == null || "NO_DATA_FOUND".equals(context)) {
            	log.warn("No relevant documents found in knowledge base for query: {}", normalized);
            	return """
            			I don't have information about this topic in the knowledge base.
            			
            			Supported topics:
            			- Leave/Vacation/Time Off policies
            			- Work From Home (WFH) policies
            			- Holiday/Festival information
            			
            			Please try rephrasing your question or ask about the available topics.""";
            }
            
            if (context.isBlank()) {
            	log.warn("Empty context for query: {}", normalized);
            	return "Information not available. Please rephrase your question.";
            }
            
            log.info("Context fetched successfully. Context length: {}", context.length());
            String prompt = buildPrompt(context, userQuery);
            
            log.info("Calling AI model...");
            String response = factory.getClient().chat(prompt);

            long endTime = System.currentTimeMillis();
            log.info("Response generated in {} ms", (endTime - startTime));

            return response;
            
        } catch (AIServiceException e) {
        	log.error("AI Service error: {}", e.getMessage(), e);
        	throw e;
        } catch (Exception e) {
        	log.error("Unexpected error in AiService: {}", e.getMessage(), e);
        	throw new AIServiceException("Failed to generate answer: " + e.getMessage(), e);
        }
    }

    /**
     * Build the prompt for the AI model
     */
    private String buildPrompt(String context, String userQuery) {
        return """
        		You are an enterprise HR assistant.

        		Rules:
        		- Answer ONLY from the given context
        		- If information is not in the context, say "Information not available"
        		- Be clear, concise, and professional
        		- Provide helpful information based on company policies

        		Context:
        		%s

        		User Question:
        		%s

        		Answer:
        		""".formatted(context, userQuery);
    }

	private String normalize(String userQuery) {
		return userQuery.toLowerCase().trim().replaceAll("\\s+", " ");
	}
}