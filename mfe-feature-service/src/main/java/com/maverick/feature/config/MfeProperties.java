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
}
