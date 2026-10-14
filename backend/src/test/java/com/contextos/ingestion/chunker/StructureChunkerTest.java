package com.contextos.ingestion.chunker;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StructureChunkerTest {

    private final StructureChunker chunker = new StructureChunker(100, 20);

    @Test
    void chunkDocument_markdownWithHeadings_splitsPreservingHeadings() {
        String markdown = """
                # Introduction
                This is the intro section of the document with some sample text.
                
                ## Section 1
                Here is details for section one. It describes feature architecture.
                
                ## Section 2
                ```java
                public class Demo {
                    public static void main(String[] args) {}
                }
                ```
                """;

        List<StructureChunker.ChunkResult> chunks = chunker.chunkDocument(markdown);

        assertFalse(chunks.isEmpty());
        assertTrue(chunks.get(0).tokenCount() > 0);
        assertTrue(chunks.stream().anyMatch(c -> c.content().contains("# Introduction")));
    }
}
