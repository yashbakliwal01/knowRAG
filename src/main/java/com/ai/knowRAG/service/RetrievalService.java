package com.ai.knowRAG.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.knowRAG.entity.Document;
import com.ai.knowRAG.repository.DocumentRepository;

@Service
public class RetrievalService {

	@Autowired
	private DocumentRepository documentRepository;
	
	public RetrievalService(DocumentRepository documentRepository) {
		this.documentRepository = documentRepository;
	}
	
	public String retrieveContext(String query) {
		List<Document> documents = documentRepository.search(query);
		
		if(documents.isEmpty()) {
			return "No relevant information found.";
		}
		
		return documents.stream()
				.limit(3)
				.map(Document::getContent)
				.collect(Collectors.joining("\n"));
		
	}
}
