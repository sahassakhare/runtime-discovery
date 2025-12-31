package com.maverick.feature.dto.governance;

import java.util.Map;
import java.util.List;

public class PolicyInput {
    private String mfeName;
    private String environment; // PROD, STAGE, DEV
    private String channel; // STABLE, CANARY
    private String version; // 1.0.0
    private UserContext user;
    private Map<String, Object> metadata;
    private Map<String, Boolean> features; // Feature Flags

    public PolicyInput() {
    }

    public PolicyInput(String mfeName, String environment, String channel, String version, UserContext user,
            Map<String, Object> metadata, Map<String, Boolean> features) {
        this.mfeName = mfeName;
        this.environment = environment;
        this.channel = channel;
        this.version = version;
        this.user = user;
        this.metadata = metadata;
        this.features = features;
    }

    public static PolicyInputBuilder builder() {
        return new PolicyInputBuilder();
    }

    public String getMfeName() {
        return mfeName;
    }

    public void setMfeName(String mfeName) {
        this.mfeName = mfeName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public UserContext getUser() {
        return user;
    }

    public void setUser(UserContext user) {
        this.user = user;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Map<String, Boolean> getFeatures() {
        return features;
    }

    public void setFeatures(Map<String, Boolean> features) {
        this.features = features;
    }

    public static class PolicyInputBuilder {
        private String mfeName;
        private String environment;
        private String channel;
        private String version;
        private UserContext user;
        private Map<String, Object> metadata;
        private Map<String, Boolean> features;

        public PolicyInputBuilder mfeName(String mfeName) {
            this.mfeName = mfeName;
            return this;
        }

        public PolicyInputBuilder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public PolicyInputBuilder channel(String channel) {
            this.channel = channel;
            return this;
        }

        public PolicyInputBuilder version(String version) {
            this.version = version;
            return this;
        }

        public PolicyInputBuilder user(UserContext user) {
            this.user = user;
            return this;
        }

        public PolicyInputBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public PolicyInputBuilder features(Map<String, Boolean> features) {
            this.features = features;
            return this;
        }

        public PolicyInput build() {
            return new PolicyInput(mfeName, environment, channel, version, user, metadata, features);
        }
    }

    public static class UserContext {
        private String userId;
        private List<String> roles;
        private String tenantId;
        private boolean isInternal;

        public UserContext() {
        }

        public UserContext(String userId, List<String> roles, String tenantId, boolean isInternal) {
            this.userId = userId;
            this.roles = roles;
            this.tenantId = tenantId;
            this.isInternal = isInternal;
        }

        public static UserContextBuilder builder() {
            return new UserContextBuilder();
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public boolean isInternal() {
            return isInternal;
        }

        public void setInternal(boolean internal) {
            isInternal = internal;
        }

        public static class UserContextBuilder {
            private String userId;
            private List<String> roles;
            private String tenantId;
            private boolean isInternal;

            public UserContextBuilder userId(String userId) {
                this.userId = userId;
                return this;
            }

            public UserContextBuilder roles(List<String> roles) {
                this.roles = roles;
                return this;
            }

            public UserContextBuilder tenantId(String tenantId) {
                this.tenantId = tenantId;
                return this;
            }

            public UserContextBuilder isInternal(boolean isInternal) {
                this.isInternal = isInternal;
                return this;
            }

            public UserContext build() {
                return new UserContext(userId, roles, tenantId, isInternal);
            }
        }
    }
}
