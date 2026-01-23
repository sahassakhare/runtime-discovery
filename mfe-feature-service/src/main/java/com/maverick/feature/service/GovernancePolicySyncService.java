package com.maverick.feature.service;

import java.util.List;

public interface GovernancePolicySyncService {
    void syncPolicy(String policyId, String regoSource);

    void syncAll(List<com.maverick.feature.domain.Policy> policies);
}
