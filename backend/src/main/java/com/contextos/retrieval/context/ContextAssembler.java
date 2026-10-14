package com.contextos.retrieval.context;

import com.contextos.common.ContextPackage;
import com.contextos.ingestion.chunker.StructureChunker;
import com.contextos.retrieval.reranker.RankedChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ContextAssembler {

    private final StructureChunker structureChunker;

    public ContextPackage assembleContext(String query, List<RankedChunk> rankedChunks, int tokenBudget, long latencyMs) {
        String requestId = "ctx_" + UUID.randomUUID().toString().substring(0, 8);
        List<ContextPackage.CitationSource> sources = new ArrayList<>();
        StringBuilder contextText = new StringBuilder();
        int totalTokens = 0;

        for (RankedChunk item : rankedChunks) {
            String excerpt = item.chunk().content();
            int chunkTokens = structureChunker.estimateTokens(excerpt);

            if (totalTokens + chunkTokens > tokenBudget) {
                break; // Stop assembling once token budget is reached
            }

            totalTokens += chunkTokens;

            sources.add(new ContextPackage.CitationSource(
                    item.chunk().chunkId(),
                    item.chunk().documentId(),
                    item.chunk().documentTitle(),
                    item.chunk().documentUrl(),
                    item.chunk().sourceType(),
                    excerpt.length() > 150 ? excerpt.substring(0, 150) + "..." : excerpt,
                    item.finalScore(),
                    item.chunk().updatedAt()
            ));

            contextText.append(String.format("""
                <retrieved_document source="%s" id="%s" title="%s">
                %s
                </retrieved_document>
                
                """, item.chunk().sourceType(), item.chunk().chunkId(), item.chunk().documentTitle(), excerpt));
        }

        ContextPackage.RetrievalMetrics metrics = new ContextPackage.RetrievalMetrics(
                rankedChunks.size(),
                rankedChunks.size(),
                sources.size(),
                latencyMs
        );

        return new ContextPackage(
                requestId,
                query,
                OffsetDateTime.now(),
                sources,
                contextText.toString().trim(),
                totalTokens,
                metrics
        );
    }
}
