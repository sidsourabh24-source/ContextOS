package com.contextos.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContextSearchRequest {

    @NotBlank(message = "query must not be blank")
    private String query;

    @Builder.Default
    private int topK = 8;

    @Builder.Default
    private int tokenBudget = 2000;

    private Set<String> userGroups;
}
