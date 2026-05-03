package com.ai.knowRAG.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import okhttp3.OkHttpClient;

@Configuration
public class HttpClientConfig {
    
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }
}
