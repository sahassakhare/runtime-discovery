package com.maverick.feature.controller;

import io.openfeature.sdk.MutableContext;
import io.openfeature.sdk.OpenFeatureAPI;
import org.ff4j.FF4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/features")
@CrossOrigin(origins = "*") // Allow consumption from MFE host
public class FeatureController {

    private final org.ff4j.FF4j ff4j;
    private final OpenFeatureAPI openFeatureAPI;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public FeatureController(org.ff4j.FF4j ff4j, OpenFeatureAPI openFeatureAPI,
            org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.ff4j = ff4j;
        this.openFeatureAPI = openFeatureAPI;
        this.jdbcTemplate = jdbcTemplate;
        System.out.println(">>> INJECTED FF4j Bean into Controller: " + System.identityHashCode(ff4j) + " <<<");
    }

    @GetMapping("/list")
    public Map<String, Object> listFeatures() {
        Map<String, Object> result = new HashMap<>();
        result.put("ff4j_bean_id", System.identityHashCode(ff4j));

        // Check FF4j Bean State
        Map<String, Boolean> beanFeatures = new HashMap<>();
        // For simple list, we can stick to FF4j check or OF getBooleanValue(id, false,
        // null)
        ff4j.getFeatures()
                .forEach((id, f) -> beanFeatures.put(id,
                        openFeatureAPI.getClient().getBooleanValue(id, false, null)));
        result.put("ff4j_bean_features", beanFeatures);
        result.put("ff4j_store_class", ff4j.getFeatureStore().getClass().getName());

        // Check Direct DB State
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM FF4J_FEATURES", Integer.class);
            result.put("db_feature_count", count);

            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM FF4J_FEATURES");
            result.put("db_rows", rows);
        } catch (Exception e) {
            result.put("db_error", e.getMessage());
        }

        return result;
    }

    @PostMapping("/evaluate")
    public Map<String, Boolean> evaluateFlags(@RequestBody Map<String, Object> context) {
        MutableContext evaluationContext = new MutableContext();
        if (context != null) {
            context.forEach((k, v) -> evaluationContext.add(k, String.valueOf(v)));
        }

        Map<String, Boolean> result = new HashMap<>();
        ff4j.getFeatures().keySet().forEach(featureId -> {
            result.put(featureId, openFeatureAPI.getClient().getBooleanValue(featureId, false, evaluationContext));
        });

        return result;
    }

    @GetMapping("/check/{featureId}")
    public boolean checkFeature(@PathVariable String featureId) {
        return openFeatureAPI.getClient().getBooleanValue(featureId, false, null);
    }

    @PostMapping("/{featureId}/enable")
    public void enableFeature(@PathVariable String featureId) {
        ff4j.enable(featureId);
    }

    @PostMapping("/{featureId}/disable")
    public void disableFeature(@PathVariable String featureId) {
        ff4j.disable(featureId);
    }

    @PostMapping("/{featureId}/toggle")
    public boolean toggleFeature(@PathVariable String featureId) {
        if (ff4j.check(featureId)) {
            ff4j.disable(featureId);
        } else {
            ff4j.enable(featureId);
        }
        return ff4j.check(featureId);
    }
}
