package com.maverick.feature.dto;

import java.util.Map;

public class ResolutionResponse {
    private String remoteName;
    private String environment;
    private RemoteVersion selected;
    private RemoteVersion fallback;
    private long cacheTtlSeconds;
    private ResolutionContext resolutionContext;

    public ResolutionResponse(String remoteName, String environment, RemoteVersion selected, RemoteVersion fallback,
            long cacheTtlSeconds,
            ResolutionContext resolutionContext) {
        this.remoteName = remoteName;
        this.environment = environment;
        this.selected = selected;
        this.fallback = fallback;
        this.cacheTtlSeconds = cacheTtlSeconds;
        this.resolutionContext = resolutionContext;
    }

    public static ResolutionResponseBuilder builder() {
        return new ResolutionResponseBuilder();
    }

    public String getRemoteName() {
        return remoteName;
    }

    public String getEnvironment() {
        return environment;
    }

    public RemoteVersion getSelected() {
        return selected;
    }

    public RemoteVersion getFallback() {
        return fallback;
    }

    public long getCacheTtlSeconds() {
        return cacheTtlSeconds;
    }

    public ResolutionContext getResolutionContext() {
        return resolutionContext;
    }

    public static class ResolutionResponseBuilder {
        private String remoteName;
        private String environment;
        private RemoteVersion selected;
        private RemoteVersion fallback;
        private long cacheTtlSeconds;
        private ResolutionContext resolutionContext;

        public ResolutionResponseBuilder remoteName(String remoteName) {
            this.remoteName = remoteName;
            return this;
        }

        public ResolutionResponseBuilder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public ResolutionResponseBuilder selected(RemoteVersion selected) {
            this.selected = selected;
            return this;
        }

        public ResolutionResponseBuilder fallback(RemoteVersion fallback) {
            this.fallback = fallback;
            return this;
        }

        public ResolutionResponseBuilder cacheTtlSeconds(long cacheTtlSeconds) {
            this.cacheTtlSeconds = cacheTtlSeconds;
            return this;
        }

        public ResolutionResponseBuilder resolutionContext(ResolutionContext resolutionContext) {
            this.resolutionContext = resolutionContext;
            return this;
        }

        public ResolutionResponse build() {
            return new ResolutionResponse(remoteName, environment, selected, fallback, cacheTtlSeconds,
                    resolutionContext);
        }
    }

    public static class RemoteVersion {
        private String version;
        private String remoteEntry;
        private String integrity;

        public RemoteVersion(String version, String remoteEntry, String integrity) {
            this.version = version;
            this.remoteEntry = remoteEntry;
            this.integrity = integrity;
        }

        public static RemoteVersionBuilder builder() {
            return new RemoteVersionBuilder();
        }

        public String getRemoteEntry() {
            return remoteEntry;
        }

        public String getVersion() {
            return version;
        }

        public String getIntegrity() {
            return integrity;
        }

        public static class RemoteVersionBuilder {
            private String version;
            private String remoteEntry;
            private String integrity;

            public RemoteVersionBuilder version(String version) {
                this.version = version;
                return this;
            }

            public RemoteVersionBuilder remoteEntry(String remoteEntry) {
                this.remoteEntry = remoteEntry;
                return this;
            }

            public RemoteVersionBuilder integrity(String integrity) {
                this.integrity = integrity;
                return this;
            }

            public RemoteVersion build() {
                return new RemoteVersion(version, remoteEntry, integrity);
            }
        }
    }

    public static class ResolutionContext {
        private Map<String, Object> flags;
        private Variant variant;
        private String governanceReason; // Optional: Reason for policy enforcement

        public ResolutionContext(Map<String, Object> flags, Variant variant, String governanceReason) {
            this.flags = flags;
            this.variant = variant;
            this.governanceReason = governanceReason;
        }

        public static ResolutionContextBuilder builder() {
            return new ResolutionContextBuilder();
        }

        public Map<String, Object> getFlags() {
            return flags;
        }

        public Variant getVariant() {
            return variant;
        }

        public String getGovernanceReason() {
            return governanceReason;
        }

        public static class ResolutionContextBuilder {
            private Map<String, Object> flags;
            private Variant variant;
            private String governanceReason;

            public ResolutionContextBuilder flags(Map<String, Object> flags) {
                this.flags = flags;
                return this;
            }

            public ResolutionContextBuilder variant(Variant variant) {
                this.variant = variant;
                return this;
            }

            public ResolutionContextBuilder governanceReason(String governanceReason) {
                this.governanceReason = governanceReason;
                return this;
            }

            public ResolutionContext build() {
                return new ResolutionContext(flags, variant, governanceReason);
            }
        }
    }

    public static class Variant {
        private String name;
        private com.maverick.feature.domain.VariantType type;

        public Variant(String name, com.maverick.feature.domain.VariantType type) {
            this.name = name;
            this.type = type;
        }

        public static VariantBuilder builder() {
            return new VariantBuilder();
        }

        public String getName() {
            return name;
        }

        public com.maverick.feature.domain.VariantType getType() {
            return type;
        }

        public static class VariantBuilder {
            private String name;
            private com.maverick.feature.domain.VariantType type;

            public VariantBuilder name(String name) {
                this.name = name;
                return this;
            }

            public VariantBuilder type(com.maverick.feature.domain.VariantType type) {
                this.type = type;
                return this;
            }

            public Variant build() {
                return new Variant(name, type);
            }
        }
    }
}
