package com.maverick.feature.controller;

import com.maverick.feature.dto.governance.PolicyDefinition;
import com.maverick.feature.service.GovernanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin; // Added
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/governance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow Dashboard from localhost:4200
public class PolicyController {

    private final GovernanceService governanceService;
    private final org.ff4j.FF4j ff4j;

    @GetMapping("/catalog")
    public List<PolicyDefinition> getCatalog() {
        return governanceService.getCatalog();
    }

    @GetMapping("/features")
    public java.util.Map<String, Boolean> getFeatures() {
        java.util.Map<String, Boolean> features = new java.util.HashMap<>();
        ff4j.getFeatures().forEach((k, v) -> features.put(k, v.isEnable()));
        return features;
    }

    @org.springframework.web.bind.annotation.PostMapping("/features/{uid}/toggle")
    public boolean toggleFeature(@org.springframework.web.bind.annotation.PathVariable String uid) {
        if (!ff4j.exist(uid))
            return false;

        boolean current = ff4j.check(uid);
        if (current)
            ff4j.disable(uid);
        else
            ff4j.enable(uid);

        return !current; // Return new state
    }

    @org.springframework.web.bind.annotation.PostMapping("/features/toggle-all")
    public void toggleAllFeatures(@org.springframework.web.bind.annotation.RequestParam boolean active) {
        ff4j.getFeatures().keySet().forEach(uid -> {
            if (active)
                ff4j.enable(uid);
            else
                ff4j.disable(uid);
        });
    }

    @org.springframework.web.bind.annotation.PutMapping("/policies/{id}")
    public org.springframework.http.ResponseEntity<Void> updatePolicy(
            @org.springframework.web.bind.annotation.PathVariable String id,
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> updates) {

        boolean success = governanceService.updatePolicy(id, updates.get("enforcementLevel"),
                updates.get("description"), updates.get("configuration"));
        if (success)
            return org.springframework.http.ResponseEntity.ok().build();
        else
            return org.springframework.http.ResponseEntity.notFound().build();
    }

    @org.springframework.web.bind.annotation.PatchMapping("/policies/{id}/toggle")
    public org.springframework.http.ResponseEntity<Void> togglePolicy(
            @org.springframework.web.bind.annotation.PathVariable String id,
            @org.springframework.web.bind.annotation.RequestParam boolean active) {
        boolean success = governanceService.togglePolicy(id, active);
        if (success)
            return org.springframework.http.ResponseEntity.ok().build();
        else
            return org.springframework.http.ResponseEntity.notFound().build();
    }

    @org.springframework.web.bind.annotation.PostMapping("/policies/toggle-all")
    public void toggleAllPolicies(@org.springframework.web.bind.annotation.RequestParam boolean active) {
        governanceService.toggleAllPolicies(active);
    }

    @org.springframework.web.bind.annotation.PostMapping("/policies")
    public com.maverick.feature.domain.Policy createPolicy(
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> payload) {
        return governanceService.createPolicy(
                payload.get("name"),
                payload.get("type"),
                payload.get("category"),
                payload.get("enforcementLevel"),
                payload.get("description"),
                payload.get("configuration"));
    }
}
