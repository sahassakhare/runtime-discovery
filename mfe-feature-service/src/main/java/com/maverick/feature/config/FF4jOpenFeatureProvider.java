package com.maverick.feature.config;

import io.openfeature.sdk.FeatureProvider;
import io.openfeature.sdk.Metadata;
import io.openfeature.sdk.ProviderEvaluation;
import io.openfeature.sdk.Value;
import lombok.RequiredArgsConstructor;
import org.ff4j.FF4j;
import org.ff4j.core.Feature;

@RequiredArgsConstructor
public class FF4jOpenFeatureProvider implements FeatureProvider {

    private final FF4j ff4j;

    @Override
    public Metadata getMetadata() {
        return () -> "FF4j Provider";
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, boolean defaultValue,
            io.openfeature.sdk.EvaluationContext ctx) {
        if (!ff4j.getFeatureStore().exist(key)) {
            return ProviderEvaluation.<Boolean>builder()
                    .value(defaultValue)
                    .build();
        }
        boolean enabled = ff4j.check(key);
        return ProviderEvaluation.<Boolean>builder()
                .value(enabled)
                .build();
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue,
            io.openfeature.sdk.EvaluationContext ctx) {
        if (!ff4j.getFeatureStore().exist(key)) {
            return ProviderEvaluation.<String>builder()
                    .value(defaultValue)
                    .build();
        }
        // Basic implementation: if feature has properties, we could try to look them
        // up.
        // For now, simple check -> "true"/"false" or custom property logic.
        Feature f = ff4j.getFeatureStore().read(key);
        if (f.getCustomProperties() != null && !f.getCustomProperties().isEmpty()) {
            // Basic support: return first property value or specific logic
            // Ideally we'd map context keys to properties.
            // For strict string eval, we might expect a property named 'value'
            if (f.getCustomProperties().containsKey("value")) {
                return ProviderEvaluation.<String>builder()
                        .value(f.getCustomProperties().get("value").asString())
                        .build();
            }
        }
        return ProviderEvaluation.<String>builder()
                .value(String.valueOf(ff4j.check(key)))
                .build();
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue,
            io.openfeature.sdk.EvaluationContext ctx) {
        return ProviderEvaluation.<Integer>builder().value(defaultValue).build();
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue,
            io.openfeature.sdk.EvaluationContext ctx) {
        return ProviderEvaluation.<Double>builder().value(defaultValue).build();
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue,
            io.openfeature.sdk.EvaluationContext ctx) {
        return ProviderEvaluation.<Value>builder().value(defaultValue).build();
    }

    @Override
    public void initialize(io.openfeature.sdk.EvaluationContext evaluationContext) throws Exception {
        // No-op
    }

    @Override
    public void shutdown() {
        // No-op
    }
}
