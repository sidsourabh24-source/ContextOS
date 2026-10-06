package com.contextos.common.cache;

import com.contextos.common.ContextPackage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCacheManager {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(15);

    public Optional<ContextPackage> getCachedContext(UUID tenantId, String userId, String query) {
        if (tenantId == null || userId == null || query == null) {
            return Optional.empty();
        }

        try {
            String cacheKey = buildCacheKey(tenantId, userId, query);
            String cachedJson = redisTemplate.opsForValue().get(cacheKey);

            if (cachedJson != null && !cachedJson.isBlank()) {
                ContextPackage pkg = objectMapper.readValue(cachedJson, ContextPackage.class);
                return Optional.of(pkg);
            }
        } catch (Exception e) {
            log.warn("Redis cache read failed or Redis unavailable: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public void cacheContext(UUID tenantId, String userId, String query, ContextPackage contextPackage) {
        if (tenantId == null || userId == null || query == null || contextPackage == null) {
            return;
        }

        try {
            String cacheKey = buildCacheKey(tenantId, userId, query);
            String json = objectMapper.writeValueAsString(contextPackage);
            redisTemplate.opsForValue().set(cacheKey, json, DEFAULT_TTL);
        } catch (Exception e) {
            log.warn("Redis cache write failed: {}", e.getMessage());
        }
    }

    public String buildCacheKey(UUID tenantId, String userId, String query) {
        String queryHash = hashQuery(query);
        return String.format("context:%s:%s:%s", tenantId, userId, queryHash);
    }

    private String hashQuery(String query) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(query.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm missing", e);
        }
    }
}
