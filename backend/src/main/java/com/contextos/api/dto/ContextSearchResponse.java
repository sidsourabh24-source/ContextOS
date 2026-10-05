package com.contextos.api.dto;

import com.contextos.common.ContextPackage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContextSearchResponse {
    private boolean success;
    private ContextPackage contextPackage;
    private String error;
}
