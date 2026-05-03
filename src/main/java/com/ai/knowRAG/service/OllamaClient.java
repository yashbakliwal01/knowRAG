package com.ai.knowRAG.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OllamaClient implements LLMClient {
	
	private final RestTemplate restTemplate;
	
	private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
	private static final int READ_TIMEOUT_MS = 120000; // 2 minutes for model inference
	private static final int CONNECT_TIMEOUT_MS = 10000; // 10 seconds to connect
	
	public OllamaClient() {
		// Configure HTTP client factory with timeouts
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setReadTimeout(READ_TIMEOUT_MS);
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT_MS);
		
		// Use buffering factory for better performance
		ClientHttpRequestFactory bufferingFactory = new BufferingClientHttpRequestFactory(requestFactory);
		
		this.restTemplate = new RestTemplate(bufferingFactory);
	}
	
	@Override
	public String chat(String prompt) {
		Map<String, Object> body = new HashMap<>();
		body.put("model", "phi3");
		body.put("prompt", prompt);
		body.put("stream", false);
		body.put("temperature", 0.7); // Controls randomness; lower = more deterministic
		
		try {
            @SuppressWarnings("rawtypes")
			Map response = restTemplate.postForObject(OLLAMA_URL, body, Map.class);
            
            if (response != null && response.containsKey("response")) {
                return response.get("response").toString().trim();
            } else {
                return "No response from Ollama";
            }
            
        } catch (Exception e) {
        	System.err.println("Ollama error: " + e.getMessage());
            return "Local AI (Ollama) not available. Make sure Ollama service is running on localhost:11434";
        }
	}
	
}