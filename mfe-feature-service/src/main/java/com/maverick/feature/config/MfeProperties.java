package com.maverick.feature.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "mfe")
public class MfeProperties {

    private Map<String, String> remoteUrls = new HashMap<>();
    private List<TenantConfig> tenants = new ArrayList<>();

    public Map<String, String> getRemoteUrls() {
        return remoteUrls;
    }

    public void setRemoteUrls(Map<String, String> remoteUrls) {
        this.remoteUrls = remoteUrls;
    }

    public List<TenantConfig> getTenants() {
        return tenants;
    }

    public void setTenants(List<TenantConfig> tenants) {
        this.tenants = tenants;
    }

    private ContextConfig context = new ContextConfig();

    public ContextConfig getContext() {
        return context;
    }

    public void setContext(ContextConfig context) {
        this.context = context;
    }

    public static class TenantConfig {
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class ContextConfig {
        private Keys keys = new Keys();
        private Defaults defaults = new Defaults();

        public Keys getKeys() {
            return keys;
        }

        public void setKeys(Keys keys) {
            this.keys = keys;
        }

        public Defaults getDefaults() {
            return defaults;
        }

        public void setDefaults(Defaults defaults) {
            this.defaults = defaults;
        }

        public static class Keys {
            private String roles = "user.roles";
            private String department = "user.department";
            private String internal = "user.internal";
            private String authenticated = "user.authenticated";
            private String userId = "user.id";

            public String getRoles() {
                return roles;
            }

            public void setRoles(String roles) {
                this.roles = roles;
            }

            public String getDepartment() {
                return department;
            }

            public void setDepartment(String department) {
                this.department = department;
            }

            public String getInternal() {
                return internal;
            }

            public void setInternal(String internal) {
                this.internal = internal;
            }

            public String getAuthenticated() {
                return authenticated;
            }

            public void setAuthenticated(String authenticated) {
                this.authenticated = authenticated;
            }

            public String getUserId() {
                return userId;
            }

            public void setUserId(String userId) {
                this.userId = userId;
            }
        }

        public static class Defaults {
            private String department = "UNASSIGNED";
            private boolean internal = false;
            private boolean authenticated = false;
            private String userId = "anonymous";

            public String getDepartment() {
                return department;
            }

            public void setDepartment(String department) {
                this.department = department;
            }

            public boolean isInternal() {
                return internal;
            }

            public void setInternal(boolean internal) {
                this.internal = internal;
            }

            public boolean isAuthenticated() {
                return authenticated;
            }

            public void setAuthenticated(boolean authenticated) {
                this.authenticated = authenticated;
            }

            public String getUserId() {
                return userId;
            }

            public void setUserId(String userId) {
                this.userId = userId;
            }
        }
    }
}
