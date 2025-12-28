package com.maverick.feature.config;

import io.openfeature.sdk.EvaluationContext;
import io.openfeature.sdk.FeatureProvider;
import io.openfeature.sdk.Metadata;
import io.openfeature.sdk.ProviderEvaluation;
import io.openfeature.sdk.Reason;
import io.openfeature.sdk.Value;
import lombok.RequiredArgsConstructor;
import org.ff4j.FF4j;
import org.ff4j.core.FlippingExecutionContext;

@RequiredArgsConstructor
public class FF4jOpenFeatureProvider implements FeatureProvider {

    private final FF4j ff4j;

    @Override
    public Metadata getMetadata() {
        return () -> "FF4j Provider";
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, boolean defaultValue, EvaluationContext ctx) {
        FlippingExecutionContext ff4jCtx = new FlippingExecutionContext();
        if (ctx != null) {
            ctx.asMap().forEach((k, v) -> ff4jCtx.putString(k, String.valueOf(v)));
        }

        try {
            // Check if feature exists to avoid Not Found exception handling if preferred,
            // but ff4j.check usually handles it or throws.
            // We'll let FF4j logic take precedence.
            boolean value = ff4j.check(key, ff4jCtx);
            return ProviderEvaluation.<Boolean>builder()
                    .value(value)
                    .reason(Reason.TARGETING_MATCH.toString())
                    .build();
        } catch (Exception e) {
            // Key not found or other error -> return default
            return ProviderEvaluation.<Boolean>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.toString())
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        // Minimal implementation for Properties
        try {
            if (ff4j.getPropertiesStore().existProperty(key)) {
                String val = ff4j.getPropertiesStore().readProperty(key).asString();
                return ProviderEvaluation.<String>builder()
                        .value(val)
                        .reason(Reason.STATIC.toString())
                        .build();
            }
        } catch (Exception e) {
            // ignore
        }
        return ProviderEvaluation.<String>builder()
                .value(defaultValue)
                .reason(Reason.DEFAULT.toString())
                .build();
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.<Integer>builder()
                .value(defaultValue)
                .reason(Reason.DEFAULT.toString())
                .build();
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.<Double>builder()
                .value(defaultValue)
                .reason(Reason.DEFAULT.toString())
                .build();
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.<Value>builder()
                .value(defaultValue)
                .reason(Reason.DEFAULT.toString())
                .build();
    }

    @Override
    public void initialize(EvaluationContext evaluationContext) throws Exception {
        // FF4j is already initialized by Spring
    }

    @Override
    public void shutdown() {
        // No op
    }
}
