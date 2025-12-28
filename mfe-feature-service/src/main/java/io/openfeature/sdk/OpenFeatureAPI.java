package io.openfeature.sdk;

public class OpenFeatureAPI {
    private static final OpenFeatureAPI INSTANCE = new OpenFeatureAPI();
    private FeatureProvider provider;

    private OpenFeatureAPI() {
    }

    public static OpenFeatureAPI getInstance() {
        return INSTANCE;
    }

    public void setProvider(FeatureProvider provider) {
        this.provider = provider;
    }

    public FeatureProvider getProvider() {
        return provider;
    }

    public Client getClient() {
        return new Client(provider);
    }
}
