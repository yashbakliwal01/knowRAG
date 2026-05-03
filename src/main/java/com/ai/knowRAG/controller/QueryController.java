package com.ai.knowRAG.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai.knowRAG.dto.QueryRequest;
import com.ai.knowRAG.dto.QueryResponse;
import com.ai.knowRAG.dto.SuccessResponse;
import com.ai.knowRAG.exception.ValidationException;
import com.ai.knowRAG.service.AiService;
import com.ai.knowRAG.service.VectorSearchService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/ai")
public class QueryController {
	
	private static final Logger log = LoggerFactory.getLogger(QueryController.class);
	
	@Autowired
	private AiService aiService;

	@Autowired
	private VectorSearchService vectorSearchService;
	
	/**
	 * Ask AI a question and get answer from knowledge base
	 */
	@PostMapping("/ask")
	public ResponseEntity<SuccessResponse<QueryResponse>> ask(@Valid @RequestBody QueryRequest request) {
		log.info("Received query: {}", request.getQuery());
		
		if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
			throw new ValidationException("Query cannot be empty");
		}
		
		String answer = aiService.generateAnswer(request.getQuery());
		QueryResponse response = new QueryResponse(request.getQuery(), answer);
		
		SuccessResponse<QueryResponse> successResponse = new SuccessResponse<>(
			HttpStatus.OK.value(),
			"Answer generated successfully",
			response
		);
		
		return ResponseEntity.ok(successResponse);
	}

	/**
	 * Health check endpoint
	 */
	@GetMapping("/health")
	public ResponseEntity<SuccessResponse<String>> health() {
		SuccessResponse<String> response = new SuccessResponse<>(
			HttpStatus.OK.value(),
			"AI Service is healthy",
			"HEALTHY"
		);
		return ResponseEntity.ok(response);
	}

	/**
	 * Initialize embeddings for all documents in the knowledge base
	 * This should be called once after loading documents
	 */
	@PostMapping("/init-embeddings")
	public ResponseEntity<SuccessResponse<String>> initializeEmbeddings() {
		log.info("Initializing embeddings for all documents");
		
		try {
			vectorSearchService.initializeEmbeddings();
			
			SuccessResponse<String> response = new SuccessResponse<>(
				HttpStatus.OK.value(),
				"Embeddings initialized successfully",
				"EMBEDDINGS_INITIALIZED"
			);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Failed to initialize embeddings: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to initialize embeddings: " + e.getMessage(), e);
		}
	}
}