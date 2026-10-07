package com.contextos.analytics;

import com.contextos.analytics.model.RetrievalRequestLog;
import com.contextos.analytics.repository.RetrievalRequestLogRepository;
import com.contextos.tenant.model.Tenant;
import com.contextos.tenant.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetrievalTracerTest {

    @Mock
    private RetrievalRequestLogRepository logRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private RetrievalTracer retrievalTracer;

    @Test
    void recordTrace_validParams_savesLogEntry() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder().id(tenantId).name("Acme").build();

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        retrievalTracer.recordTrace(tenantId, "Why did Apollo fail?", 8, 120L);

        verify(logRepository).save(any(RetrievalRequestLog.class));
    }
}
