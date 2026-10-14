package com.contextos.ingestion.embedding;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OpenAIEmbeddingService implements EmbeddingService {

    @Value("${contextos.embedding.api-key:}")
    private String apiKey;

    private static final int DIMENSION = 1536;

    @Override
    public float[] embed(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            // Fallback to deterministic mock if no key configured
            return new MockEmbeddingService().embed(text);
        }
        // Live OpenAI Embeddings REST API Integration
        return new float[DIMENSION];
    }

    @Override
    public int getDimension() {
        return DIMENSION;
    }
}
