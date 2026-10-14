package com.contextos.api.dto;

import com.contextos.common.ContextPackage;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContextSearchResponse {
    private boolean success;
    private ContextPackage contextPackage;
    private String error;
    private TraceDetails traceDetails;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TraceDetails {
        private int vectorCandidatesCount;
        private int keywordCandidatesCount;
        private int totalCandidatesCount;
        private int aclAuthorizedCount;
        private int aclFilteredCount;
        private boolean fromCache;
        private List<String> strippedTitles;
    }
}
