package io.openfeature.sdk;

public class ProviderEvaluation<T> {
    private T value;
    private String reason;
    private String errorMessage;

    public ProviderEvaluation() {
    }

    public ProviderEvaluation(T value, String reason, String errorMessage) {
        this.value = value;
        this.reason = reason;
        this.errorMessage = errorMessage;
    }

    public static <T> ProviderEvaluationBuilder<T> builder() {
        return new ProviderEvaluationBuilder<>();
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static class ProviderEvaluationBuilder<T> {
        private T value;
        private String reason;
        private String errorMessage;

        public ProviderEvaluationBuilder<T> value(T value) {
            this.value = value;
            return this;
        }

        public ProviderEvaluationBuilder<T> reason(String reason) {
            this.reason = reason;
            return this;
        }

        public ProviderEvaluationBuilder<T> errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public ProviderEvaluation<T> build() {
            return new ProviderEvaluation<>(value, reason, errorMessage);
        }
    }
}
