package io.openfeature.sdk;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProviderEvaluation<T> {
    private T value;
    private String reason;
    private String errorMessage;
}
