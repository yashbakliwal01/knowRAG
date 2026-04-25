package com.ai.knowRAG.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OllamaClient implements LLMClient {
	
	private final RestTemplate restTemplate = new RestTemplate();
	
	private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
	
	@Override
	public String chat(String prompt) {
		Map<String, Object> body = new HashMap<>();
		body.put("model", "phi3");
		body.put("prompt", prompt);
		body.put("stream", false);
		try {
            @SuppressWarnings("rawtypes")
			Map response = restTemplate.postForObject(OLLAMA_URL, body, Map.class);
            return response.get("response").toString();
        } catch (Exception e) {
        	e.printStackTrace();
            return "Local AI not available";
        }
	}
	
}
