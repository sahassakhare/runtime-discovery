package com.maverick.feature.dto.governance;

import lombok.Builder;
import lombok.Data;

@Data
public class PolicyDefinition {
    private String id;
    private String name;
    private String category; // Discovery, Routing, Security, Compatibility, Operational
    private String description;
    private String enforcementLevel; // BLOCK, WARNING, LOG, ALLOW
    private String type; // Strategy Key
    private String configuration;
    private boolean isActive;

    public PolicyDefinition() {
    }

    public PolicyDefinition(String id, String name, String category, String description, String enforcementLevel,
            String type, String configuration, boolean isActive) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.enforcementLevel = enforcementLevel;
        this.type = type;
        this.configuration = configuration;
        this.isActive = isActive;
    }

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEnforcementLevel() {
        return enforcementLevel;
    }

    public void setEnforcementLevel(String enforcementLevel) {
        this.enforcementLevel = enforcementLevel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public static PolicyDefinitionBuilder builder() {
        return new PolicyDefinitionBuilder();
    }

    public static class PolicyDefinitionBuilder {
        private String id;
        private String name;
        private String category;
        private String description;
        private String enforcementLevel;
        private String type;
        private String configuration;
        private boolean isActive;

        public PolicyDefinitionBuilder id(String id) {
            this.id = id;
            return this;
        }

        public PolicyDefinitionBuilder name(String name) {
            this.name = name;
            return this;
        }

        public PolicyDefinitionBuilder category(String category) {
            this.category = category;
            return this;
        }

        public PolicyDefinitionBuilder description(String description) {
            this.description = description;
            return this;
        }

        public PolicyDefinitionBuilder enforcementLevel(String enforcementLevel) {
            this.enforcementLevel = enforcementLevel;
            return this;
        }

        public PolicyDefinitionBuilder type(String type) {
            this.type = type;
            return this;
        }

        public PolicyDefinitionBuilder configuration(String configuration) {
            this.configuration = configuration;
            return this;
        }

        public PolicyDefinitionBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public PolicyDefinition build() {
            return new PolicyDefinition(id, name, category, description, enforcementLevel, type, configuration,
                    isActive);
        }
    }
}
