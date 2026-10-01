package com.contextos.ingestion.embedding;

public interface EmbeddingService {
    float[] embed(String text);
    int getDimension();
}
