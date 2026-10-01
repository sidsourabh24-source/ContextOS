package com.contextos.ingestion.embedding;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
@Primary
public class MockEmbeddingService implements EmbeddingService {

    private static final int DIMENSION = 1536;

    @Override
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            return new float[DIMENSION];
        }

        float[] vector = new float[DIMENSION];
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));

            for (int i = 0; i < DIMENSION; i++) {
                int byteVal = Byte.toUnsignedInt(hash[i % hash.length]);
                vector[i] = (float) Math.sin((i + 1) * (byteVal + 1) * 0.01);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not found", e);
        }

        return normalize(vector);
    }

    @Override
    public int getDimension() {
        return DIMENSION;
    }

    private float[] normalize(float[] vector) {
        double norm = 0.0;
        for (float v : vector) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] = (float) (vector[i] / norm);
            }
        }
        return vector;
    }
}
