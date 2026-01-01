package com.maverick.feature.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GovernancePolicySyncService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GovernancePolicySyncService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String OPA_POLICIES_URL = "http://localhost:8181/v1/policies/";

    /**
     * Pushes a Rego policy to the OPA Management API.
     * 
     * @param policyId   The unique ID of the policy (e.g., POL-MFE-01)
     * @param regoSource The actual Rego code
     */
    public void syncPolicy(String policyId, String regoSource) {
        if (regoSource == null || regoSource.isBlank()) {
            log.warn("Skipping sync for policy {}: No Rego source provided", policyId);
            return;
        }

        try {
            log.info("Hot-Syncing Policy to OPA: {}...", policyId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            HttpEntity<String> entity = new HttpEntity<>(regoSource, headers);

            restTemplate.put(OPA_POLICIES_URL + policyId, entity);

            log.info("Successfully synced policy {} to OPA.", policyId);
        } catch (Exception e) {
            log.error("Failed to sync policy {} to OPA. Is OPA running on :8181? Error: {}", policyId, e.getMessage());
        }
    }

    /**
     * Syncs all active policies to OPA. Useful for startup or recovery.
     */
    public void syncAll(List<com.maverick.feature.domain.Policy> policies) {
        log.info("Triggering Full Batch Sync to OPA (Count: {})...", policies.size());
        policies.stream()
                .filter(p -> "REGO_OPA".equals(p.getType()))
                .forEach(p -> syncPolicy(p.getCode(), p.getConfiguration()));
    }
}
