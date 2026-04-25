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
		String normalized = normalizeQuery(query);
		List<Document> documents = documentRepository.search(normalized);
		
		if(documents.isEmpty()) {
			return "No relevant information found.";
		}
		
		return documents.stream()
				.limit(3)
				.map(Document::getContent)
				.collect(Collectors.joining("\n"));
		
	}

	private String normalizeQuery(String query) {
		String q = query.toLowerCase();
		
		//leave related
		if(q.contains("leave") || q.contains("vacation") || q.contains("time off")) {
			return "leave";
		}
		
		//related to wfh
		if(q.contains("work from home") || q.contains("remote") || q.contains("wfh") || q.contains("remote work")) {
			return "wfh";
		}
		
		// holidays
	    if (q.contains("holiday") || q.contains("festival")) {
	        return "holiday";
	    }
	    
	    return query;
		
		
	}
}
