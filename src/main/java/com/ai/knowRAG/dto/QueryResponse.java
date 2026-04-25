package com.ai.knowRAG.dto;

public class QueryResponse {

    private String query;
    private String answer;

    public QueryResponse(String query, String answer) {
        this.query = query;
        this.answer = answer;
    }

    public String getQuery() {
        return query;
    }

    public String getAnswer() {
        return answer;
    }
}