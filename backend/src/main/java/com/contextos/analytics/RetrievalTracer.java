package com.contextos.analytics;

import com.contextos.analytics.model.RetrievalRequestLog;
import com.contextos.analytics.repository.RetrievalRequestLogRepository;
import com.contextos.tenant.model.Tenant;
import com.contextos.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetrievalTracer {

    private final RetrievalRequestLogRepository logRepository;
    private final TenantRepository tenantRepository;

    @Async
    public void recordTrace(UUID tenantId, String query, int topK, long latencyMs) {
        if (tenantId == null || query == null) {
            return;
        }

        try {
            Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
            if (tenant == null) {
                return;
            }

            RetrievalRequestLog logEntry = RetrievalRequestLog.builder()
                    .tenant(tenant)
                    .query(query)
                    .topK(topK)
                    .latencyMs(latencyMs)
                    .createdAt(OffsetDateTime.now())
                    .build();

            logRepository.save(logEntry);
            log.info("Recorded retrieval trace for tenant {} query='{}' latency={}ms", tenantId, query, latencyMs);
        } catch (Exception e) {
            log.warn("Failed to record retrieval trace: {}", e.getMessage());
        }
    }
}
