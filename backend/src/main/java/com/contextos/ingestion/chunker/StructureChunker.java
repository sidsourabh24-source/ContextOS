package com.contextos.ingestion.chunker;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class StructureChunker {

    private final int targetChunkSize;
    private final int chunkOverlap;

    public StructureChunker() {
        this(512, 64);
    }

    public StructureChunker(int targetChunkSize, int chunkOverlap) {
        this.targetChunkSize = targetChunkSize;
        this.chunkOverlap = chunkOverlap;
    }

    public record ChunkResult(int index, String content, int tokenCount) {}

    public List<ChunkResult> chunkDocument(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }

        List<String> sections = splitByHeadingsAndCodeBlocks(content);
        List<ChunkResult> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        int chunkIndex = 0;

        for (String section : sections) {
            if (estimateTokens(currentChunk.toString() + "\n" + section) > targetChunkSize && currentChunk.length() > 0) {
                String chunkText = currentChunk.toString().trim();
                chunks.add(new ChunkResult(chunkIndex++, chunkText, estimateTokens(chunkText)));

                // Apply overlap
                currentChunk = new StringBuilder(getOverlapText(chunkText, chunkOverlap));
            }
            if (currentChunk.length() > 0) {
                currentChunk.append("\n");
            }
            currentChunk.append(section);
        }

        if (currentChunk.length() > 0 && !currentChunk.toString().trim().isEmpty()) {
            String chunkText = currentChunk.toString().trim();
            chunks.add(new ChunkResult(chunkIndex, chunkText, estimateTokens(chunkText)));
        }

        return chunks;
    }

    private List<String> splitByHeadingsAndCodeBlocks(String content) {
        List<String> sections = new ArrayList<>();
        String[] lines = content.split("\r?\n");
        StringBuilder currentSection = new StringBuilder();
        boolean inCodeBlock = false;

        for (String line : lines) {
            if (line.trim().startsWith("```")) {
                inCodeBlock = !inCodeBlock;
            }

            if (!inCodeBlock && (line.startsWith("# ") || line.startsWith("## ") || line.startsWith("### "))) {
                if (currentSection.length() > 0) {
                    sections.add(currentSection.toString().trim());
                    currentSection = new StringBuilder();
                }
            }

            if (currentSection.length() > 0) {
                currentSection.append("\n");
            }
            currentSection.append(line);
        }

        if (currentSection.length() > 0) {
            sections.add(currentSection.toString().trim());
        }

        return sections;
    }

    private String getOverlapText(String text, int overlapTokens) {
        String[] words = text.split("\\s+");
        if (words.length <= overlapTokens) {
            return text;
        }
        int startIndex = Math.max(0, words.length - overlapTokens);
        return String.join(" ", Arrays.copyOfRange(words, startIndex, words.length));
    }

    public int estimateTokens(String text) {
        if (text == null || text.isBlank()) return 0;
        return (int) Math.ceil(text.length() / 4.0);
    }
}
