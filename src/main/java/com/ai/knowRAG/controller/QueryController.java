package com.ai.knowRAG.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai.knowRAG.service.AiService;

@RestController
@RequestMapping("/api/ask")
public class QueryController {
	
	private final AiService aiService;
	
	public QueryController(AiService aiService) {
		this.aiService = aiService;
	}
	
	@PostMapping
	public ResponseEntity<Map<String, String>> ask(@RequestBody Map<String, String> request) {
		String query = request.get("query");
		String answer = aiService.generateAnswer(query);
		
//		Map<String, String> response = new HashMap<>();
//		response.put("answer", answer);
		
		return ResponseEntity.ok(Map.of(
				"query", query,
				"answer", answer));
	}
}
