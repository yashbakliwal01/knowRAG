package com.ai.knowRAG.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.ai.knowRAG.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/**
	 * Handle validation exceptions from @Valid annotations
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(
			MethodArgumentNotValidException ex,
			WebRequest request) {
		
		log.error("Validation error: {}", ex.getMessage());
		
		Map<String, String> fieldErrors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			fieldErrors.put(fieldName, errorMessage);
		});

		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.BAD_REQUEST.value(),
			"Validation failed",
			"VALIDATION_ERROR",
			request.getDescription(false).replace("uri=", ""),
			fieldErrors
		);
		
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle resource not found exceptions
	 */
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
			ResourceNotFoundException ex,
			WebRequest request) {
		
		log.error("Resource not found: {}", ex.getMessage());
		
		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.NOT_FOUND.value(),
			ex.getMessage(),
			"RESOURCE_NOT_FOUND",
			request.getDescription(false).replace("uri=", "")
		);
		
		return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
	}

	/**
	 * Handle embedding service exceptions
	 */
	@ExceptionHandler(EmbeddingException.class)
	public ResponseEntity<ErrorResponse> handleEmbeddingException(
			EmbeddingException ex,
			WebRequest request) {
		
		log.error("Embedding error: {}", ex.getMessage(), ex);
		
		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.INTERNAL_SERVER_ERROR.value(),
			"Failed to process embeddings: " + ex.getMessage(),
			"EMBEDDING_ERROR",
			request.getDescription(false).replace("uri=", "")
		);
		
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Handle AI service exceptions
	 */
	@ExceptionHandler(AIServiceException.class)
	public ResponseEntity<ErrorResponse> handleAIServiceException(
			AIServiceException ex,
			WebRequest request) {
		
		log.error("AI Service error: {}", ex.getMessage(), ex);
		
		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.SERVICE_UNAVAILABLE.value(),
			"AI service error: " + ex.getMessage(),
			"AI_SERVICE_ERROR",
			request.getDescription(false).replace("uri=", "")
		);
		
		return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
	}

	/**
	 * Handle validation exceptions
	 */
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(
			ValidationException ex,
			WebRequest request) {
		
		log.error("Validation error: {}", ex.getMessage());
		
		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.BAD_REQUEST.value(),
			ex.getMessage(),
			"VALIDATION_ERROR",
			request.getDescription(false).replace("uri=", "")
		);
		
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle generic runtime exceptions
	 */
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ErrorResponse> handleRuntimeExceptions(
			RuntimeException ex,
			WebRequest request) {
		
		log.error("Runtime exception: {}", ex.getMessage(), ex);
		
		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.INTERNAL_SERVER_ERROR.value(),
			"An unexpected error occurred",
			"RUNTIME_ERROR",
			request.getDescription(false).replace("uri=", "")
		);
		
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Handle all other exceptions
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(
			Exception ex,
			WebRequest request) {
		
		log.error("Unexpected exception: {}", ex.getMessage(), ex);
		
		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.INTERNAL_SERVER_ERROR.value(),
			"Something went wrong. Please try again later.",
			"INTERNAL_SERVER_ERROR",
			request.getDescription(false).replace("uri=", "")
		);
		
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}