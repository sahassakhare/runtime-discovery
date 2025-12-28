package io.openfeature.sdk;

public interface FeatureProvider {
    Metadata getMetadata();

    ProviderEvaluation<Boolean> getBooleanEvaluation(String key, boolean defaultValue, EvaluationContext ctx);

    ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx);

    ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx);

    ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx);

    ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx);

    void initialize(EvaluationContext evaluationContext) throws Exception;

    void shutdown();
}
