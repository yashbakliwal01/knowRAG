package com.ai.knowRAG.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai.knowRAG.dto.QueryRequest;
import com.ai.knowRAG.dto.QueryResponse;
import com.ai.knowRAG.service.AiService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/ai")
public class QueryController {
	
	private static final Logger log = LoggerFactory.getLogger(QueryController.class);
	
	@Autowired
	public AiService aiService;
	
//	public QueryController(AiService aiService) {
//		this.aiService = aiService;
//	}
	
	@PostMapping("/ask")
	public ResponseEntity<QueryResponse> ask(@Valid @RequestBody QueryRequest  request) {
		String answer = aiService.generateAnswer(request.getQuery());
		return ResponseEntity.ok(new QueryResponse(request.getQuery(), answer));
	}
	
	@GetMapping("/health")
	public ResponseEntity<String> health(){
		return ResponseEntity.ok("AI Service is healthy");
	}
	
//	@DeleteMapping("/clear/cache")
//	public ResponseEntity<String> clearCache() {
//		
//		log.info("Cache cleared");
//		return ResponseEntity.ok("Cache cleared successfully");
//	}
}
