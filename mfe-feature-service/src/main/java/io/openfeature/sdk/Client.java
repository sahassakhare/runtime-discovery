package io.openfeature.sdk;

public class Client {
    private final FeatureProvider provider;

    public Client(FeatureProvider provider) {
        this.provider = provider;
    }

    public boolean getBooleanValue(String key, boolean defaultValue, EvaluationContext ctx) {
        if (provider == null) return defaultValue;
        return provider.getBooleanEvaluation(key, defaultValue, ctx).getValue();
    }
}
