package com.maverick.feature.service;

import com.maverick.feature.domain.RuntimeInstance;
import com.maverick.feature.dto.RegisterInstanceRequest;

public interface RegistryService {

    RuntimeInstance registerInstance(RegisterInstanceRequest request);

    String reportConsumption(java.util.Map<String, Object> report);
}
