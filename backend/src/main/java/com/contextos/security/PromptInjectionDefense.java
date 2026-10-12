package com.contextos.security;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PromptInjectionDefense {

    private static final Pattern INJECTION_PATTERNS = Pattern.compile(
            "(?i)(ignore\\s+(all\\s+)?(previous|prior)\\s+instructions|system\\s*:|assistant\\s*:|<\\s*/?system\\s*>|<\\s*/?instruction\\s*>|disregard\\s+all\\s+rules)"
    );

    public record SanitizedContent(String cleanContent, boolean injectionDetected) {}

    public SanitizedContent sanitizeRetrievedContent(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            return new SanitizedContent("", false);
        }

        boolean detected = INJECTION_PATTERNS.matcher(rawContent).find();

        // 1. Defang override tokens
        String sanitized = INJECTION_PATTERNS.matcher(rawContent)
                .replaceAll("[FILTERED_OVERRIDE_TOKEN]");

        // 2. Escape XML boundaries inside content to prevent tag breaking
        sanitized = sanitized.replace("<retrieved_document", "&lt;retrieved_document")
                             .replace("</retrieved_document>", "&lt;/retrieved_document&gt;");

        return new SanitizedContent(sanitized, detected);
    }

    public String wrapWithDefensiveBoundaries(String sourceId, String sourceType, String sanitizedContent) {
        return String.format("""
            <retrieved_document source="%s" id="%s" role="UNTRUSTED_EXTERNAL_DATA">
            %s
            </retrieved_document>
            """, sourceType, sourceId, sanitizedContent);
    }
}
