package com.maverick.feature.dto.governance;

import java.util.Map;
import java.util.List;

public class PolicyInput {
    private String mfeName;
    private String environment; // PROD, STAGE, DEV
    private String env; // for rego compatibility
    private String channel; // STABLE, CANARY
    private String version; // 1.0.0
    private String selectedVersion; // for rego compatibility
    private String remoteEntry;
    private String exposedModule;
    private String fallback;
    private String route;
    private UserContext user;
    private Map<String, Object> metadata;
    private Map<String, Boolean> features; // Feature Flags
    private List<String> availableVersions;
    private Map<String, String> requires;
    private Map<String, String> host;
    private Map<String, Double> metrics;
    private Map<String, Object> designTokens;
    private Map<String, Object> accessibility;

    public PolicyInput() {
    }

    public PolicyInput(String mfeName, String environment, String env, String channel, String version,
            String selectedVersion, String remoteEntry, String exposedModule, String fallback, String route,
            UserContext user, Map<String, Object> metadata, Map<String, Boolean> features,
            List<String> availableVersions, Map<String, String> requires, Map<String, String> host,
            Map<String, Double> metrics, Map<String, Object> designTokens, Map<String, Object> accessibility) {
        this.mfeName = mfeName;
        this.environment = environment;
        this.env = env;
        this.channel = channel;
        this.version = version;
        this.selectedVersion = selectedVersion;
        this.remoteEntry = remoteEntry;
        this.exposedModule = exposedModule;
        this.fallback = fallback;
        this.route = route;
        this.user = user;
        this.metadata = metadata;
        this.features = features;
        this.availableVersions = availableVersions;
        this.requires = requires;
        this.host = host;
        this.metrics = metrics;
        this.designTokens = designTokens;
        this.accessibility = accessibility;
    }

    public static PolicyInputBuilder builder() {
        return new PolicyInputBuilder();
    }

    // Getters and Setters
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

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
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

    public String getSelectedVersion() {
        return selectedVersion;
    }

    public void setSelectedVersion(String selectedVersion) {
        this.selectedVersion = selectedVersion;
    }

    public String getRemoteEntry() {
        return remoteEntry;
    }

    public void setRemoteEntry(String remoteEntry) {
        this.remoteEntry = remoteEntry;
    }

    public String getExposedModule() {
        return exposedModule;
    }

    public void setExposedModule(String exposedModule) {
        this.exposedModule = exposedModule;
    }

    public String getFallback() {
        return fallback;
    }

    public void setFallback(String fallback) {
        this.fallback = fallback;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
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

    public List<String> getAvailableVersions() {
        return availableVersions;
    }

    public void setAvailableVersions(List<String> availableVersions) {
        this.availableVersions = availableVersions;
    }

    public Map<String, String> getRequires() {
        return requires;
    }

    public void setRequires(Map<String, String> requires) {
        this.requires = requires;
    }

    public Map<String, String> getHost() {
        return host;
    }

    public void setHost(Map<String, String> host) {
        this.host = host;
    }

    public Map<String, Double> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Double> metrics) {
        this.metrics = metrics;
    }

    public Map<String, Object> getDesignTokens() {
        return designTokens;
    }

    public void setDesignTokens(Map<String, Object> designTokens) {
        this.designTokens = designTokens;
    }

    public Map<String, Object> getAccessibility() {
        return accessibility;
    }

    public void setAccessibility(Map<String, Object> accessibility) {
        this.accessibility = accessibility;
    }

    public static class PolicyInputBuilder {
        private String mfeName;
        private String environment;
        private String env;
        private String channel;
        private String version;
        private String selectedVersion;
        private String remoteEntry;
        private String exposedModule;
        private String fallback;
        private String route;
        private UserContext user;
        private Map<String, Object> metadata;
        private Map<String, Boolean> features;
        private List<String> availableVersions;
        private Map<String, String> requires;
        private Map<String, String> host;
        private Map<String, Double> metrics;
        private Map<String, Object> designTokens;
        private Map<String, Object> accessibility;

        public PolicyInputBuilder mfeName(String mfeName) {
            this.mfeName = mfeName;
            return this;
        }

        public PolicyInputBuilder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public PolicyInputBuilder env(String env) {
            this.env = env;
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

        public PolicyInputBuilder selectedVersion(String selectedVersion) {
            this.selectedVersion = selectedVersion;
            return this;
        }

        public PolicyInputBuilder remoteEntry(String remoteEntry) {
            this.remoteEntry = remoteEntry;
            return this;
        }

        public PolicyInputBuilder exposedModule(String exposedModule) {
            this.exposedModule = exposedModule;
            return this;
        }

        public PolicyInputBuilder fallback(String fallback) {
            this.fallback = fallback;
            return this;
        }

        public PolicyInputBuilder route(String route) {
            this.route = route;
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

        public PolicyInputBuilder availableVersions(List<String> availableVersions) {
            this.availableVersions = availableVersions;
            return this;
        }

        public PolicyInputBuilder requires(Map<String, String> requires) {
            this.requires = requires;
            return this;
        }

        public PolicyInputBuilder host(Map<String, String> host) {
            this.host = host;
            return this;
        }

        public PolicyInputBuilder metrics(Map<String, Double> metrics) {
            this.metrics = metrics;
            return this;
        }

        public PolicyInputBuilder designTokens(Map<String, Object> designTokens) {
            this.designTokens = designTokens;
            return this;
        }

        public PolicyInputBuilder accessibility(Map<String, Object> accessibility) {
            this.accessibility = accessibility;
            return this;
        }

        public PolicyInput build() {
            return new PolicyInput(mfeName, environment, env, channel, version, selectedVersion, remoteEntry,
                    exposedModule, fallback, route, user, metadata, features, availableVersions, requires,
                    host, metrics, designTokens, accessibility);
        }
    }

    public static class UserContext {
        private String userId;
        private List<String> roles;
        private String tenantId;
        private boolean isInternal;
        private String department; // "FINANCE", "HR", etc.
        private boolean authenticated;

        public UserContext() {
        }

        public UserContext(String userId, List<String> roles, String tenantId, boolean isInternal, String department,
                boolean authenticated) {
            this.userId = userId;
            this.roles = roles;
            this.tenantId = tenantId;
            this.isInternal = isInternal;
            this.department = department;
            this.authenticated = authenticated;
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

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public boolean isAuthenticated() {
            return authenticated;
        }

        public void setAuthenticated(boolean authenticated) {
            this.authenticated = authenticated;
        }

        public static class UserContextBuilder {
            private String userId;
            private List<String> roles;
            private String tenantId;
            private boolean isInternal;
            private String department;
            private boolean authenticated;

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

            public UserContextBuilder department(String department) {
                this.department = department;
                return this;
            }

            public UserContextBuilder authenticated(boolean authenticated) {
                this.authenticated = authenticated;
                return this;
            }

            public UserContext build() {
                return new UserContext(userId, roles, tenantId, isInternal, department, authenticated);
            }
        }
    }
}
