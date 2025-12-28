package io.openfeature.sdk;

public class Value {
    private Object internalValue;

    public Value(Object v) {
        this.internalValue = v;
    }

    public String asString() {
        return String.valueOf(internalValue);
    }
}
