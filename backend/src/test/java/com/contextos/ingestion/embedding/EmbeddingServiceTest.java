package com.contextos.ingestion.embedding;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddingServiceTest {

    private final EmbeddingService embeddingService = new MockEmbeddingService();

    @Test
    void embed_validText_returnsNormalizedVectorOf1536() {
        float[] vector = embeddingService.embed("ContextOS Enterprise Architecture");

        assertNotNull(vector);
        assertEquals(1536, vector.length);
        assertEquals(1536, embeddingService.getDimension());
    }

    @Test
    void embed_sameText_returnsSameVector() {
        float[] v1 = embeddingService.embed("Deterministic Check");
        float[] v2 = embeddingService.embed("Deterministic Check");

        assertArrayEquals(v1, v2);
    }
}
