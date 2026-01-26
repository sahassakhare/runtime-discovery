package com.maverick.feature.dto;

public class RegisterMfeRequest {
    private String name; // e.g. "payment-remote"
    private String version; // e.g. "1.2.0"
    private String remoteEntry; // e.g. "http://cdn.../remoteEntry.js"
    private String integrity; // SRI Hash (optional)

    // Additional metadata from plugin?
    private String type; // "var", "module", etc.

    private String tenantId; // New field for multi-tenancy support
    private String groupId; // Optional Group ID

    private java.util.List<ConsumedRemoteMetadata> consumedRemotes;

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

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public java.util.List<ConsumedRemoteMetadata> getConsumedRemotes() {
        return consumedRemotes;
    }

    public void setConsumedRemotes(java.util.List<ConsumedRemoteMetadata> consumedRemotes) {
        this.consumedRemotes = consumedRemotes;
    }

    public static class ConsumedRemoteMetadata {
        private String remoteName;
        private String modules; // comma separated
        private boolean dynamic;

        public String getRemoteName() {
            return remoteName;
        }

        public void setRemoteName(String remoteName) {
            this.remoteName = remoteName;
        }

        public String getModules() {
            return modules;
        }

        public void setModules(String modules) {
            this.modules = modules;
        }

        public boolean isDynamic() {
            return dynamic;
        }

        public void setDynamic(boolean dynamic) {
            this.dynamic = dynamic;
        }
    }
}
