package com.maverick.feature.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class ResolutionResponse {
    private String remoteName;
    private RemoteVersion selected;
    private RemoteVersion fallback;
    private long cacheTtlSeconds;
    private ResolutionContext resolutionContext;

    @Data
    @Builder
    public static class RemoteVersion {
        private String version;
        private String remoteEntry;
        private String integrity;
    }

    @Data
    @Builder
    public static class ResolutionContext {
        private Map<String, Object> flags;
        private Variant variant;
    }

    @Data
    @Builder
    public static class Variant {
        private String name;
        private String type; // canary, experiment, standard
    }
}
