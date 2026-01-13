package com.ai.knowRAG.service;

import org.springframework.stereotype.Service;

@Service
public class AiService {

	private final RetrievalService retrievalService;
//	private final LlmService llmService;
	
	public AiService(RetrievalService retrievalService) {
		this.retrievalService = retrievalService;
	}
	
	public String generateAnswer(String userQuery) {
		String context = retrievalService.retrieveContext(userQuery);
		
		if (context == null || context.isBlank() || "NO_DATA_FOUND".equals(context)) {
            return "Information not available";
        }
		
		String prompt = """
                You are an enterprise knowledge assistant.
                Answer ONLY from the context below.
                Do not guess or add extra information.

                Context:
                %s

                Question:
                %s
                """.formatted(context, userQuery);

        // Simulated LLM response
        return context;
	}
}
