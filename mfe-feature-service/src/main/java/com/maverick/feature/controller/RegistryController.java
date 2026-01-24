package com.maverick.feature.controller;

import com.maverick.feature.domain.RuntimeInstance;
import com.maverick.feature.dto.RegisterInstanceRequest;
import com.maverick.feature.repository.RuntimeInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/registry")
@RequiredArgsConstructor
public class RegistryController {

    private final com.maverick.feature.service.RegistryService registryService;

    @PostMapping("/instances")
    public ResponseEntity<RuntimeInstance> registerInstance(@RequestBody RegisterInstanceRequest request) {
        return ResponseEntity.ok(registryService.registerInstance(request));
    }

    @PostMapping("/consumption")
    public ResponseEntity<String> reportConsumption(@RequestBody java.util.Map<String, Object> report) {
        try {
            return ResponseEntity.ok(registryService.reportConsumption(report));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
