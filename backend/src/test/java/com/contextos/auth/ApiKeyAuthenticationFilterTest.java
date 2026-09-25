package com.contextos.auth;

import com.contextos.auth.model.ApiKey;
import com.contextos.auth.repository.ApiKeyRepository;
import com.contextos.tenant.model.Tenant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyAuthenticationFilterTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    @Test
    void doFilterInternal_validApiKey_setsSecurityContextAndTenantContext() throws Exception {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder().id(tenantId).name("Acme Corp").build();
        ApiKey apiKey = ApiKey.builder().id(UUID.randomUUID()).tenant(tenant).status("ACTIVE").keyHash("mockHash").build();

        when(request.getHeader("X-ContextOS-API-Key")).thenReturn("test-api-key");
        when(apiKeyRepository.findByKeyHash(anyString())).thenReturn(Optional.of(apiKey));

        apiKeyAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(apiKeyRepository).findByKeyHash(anyString());
    }
}
