package com.contextos.common.cache;

import com.contextos.common.ContextPackage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisCacheManagerTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisCacheManager redisCacheManager;

    @Test
    void buildCacheKey_formatsTenantAndUserKey() {
        UUID tenantId = UUID.randomUUID();
        String key = redisCacheManager.buildCacheKey(tenantId, "user-42", "Why did Apollo fail?");

        assertTrue(key.startsWith("context:" + tenantId + ":user-42:"));
    }

    @Test
    void getCachedContext_cacheMiss_returnsEmpty() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        Optional<ContextPackage> result = redisCacheManager.getCachedContext(UUID.randomUUID(), "user-1", "query");

        assertTrue(result.isEmpty());
    }
}
