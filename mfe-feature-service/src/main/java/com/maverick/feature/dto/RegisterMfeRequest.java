package com.maverick.feature.dto;

public class RegisterMfeRequest {
    private String name; // e.g. "payment-remote"
    private String version; // e.g. "1.2.0"
    private String remoteEntry; // e.g. "http://cdn.../remoteEntry.js"
    private String integrity; // SRI Hash (optional)

    // Additional metadata from plugin?
    private String type; // "var", "module", etc.

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRemoteEntry() {
        return remoteEntry;
    }

    public void setRemoteEntry(String remoteEntry) {
        this.remoteEntry = remoteEntry;
    }

    public String getIntegrity() {
        return integrity;
    }

    public void setIntegrity(String integrity) {
        this.integrity = integrity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
