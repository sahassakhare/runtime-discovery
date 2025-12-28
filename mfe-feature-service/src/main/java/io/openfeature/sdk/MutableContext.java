package io.openfeature.sdk;

import java.util.HashMap;
import java.util.Map;

public class MutableContext implements EvaluationContext {
    private Map<String, Object> data = new HashMap<>();

    public void add(String key, Object value) {
        data.put(key, value);
    }

    @Override
    public Map<String, Object> asMap() {
        return data;
    }
}
