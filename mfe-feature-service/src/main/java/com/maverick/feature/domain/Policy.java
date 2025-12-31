package com.maverick.feature.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String code; // e.g., POL-01

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String category; // Discovery, Security, etc.

    @Column(nullable = false)
    private String type; // Strategy Key e.g., ENV_INTEGRITY

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    private String enforcementLevel; // BLOCK, WARNING

    @Column(length = 4000) // JSON Configuration
    private String configuration;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Policy() {
    }

    public Policy(String id, String code, String name, String description, String category, String type,
            boolean isActive, String enforcementLevel, String configuration, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.category = category;
        this.type = type;
        this.isActive = isActive;
        this.enforcementLevel = enforcementLevel;
        this.configuration = configuration;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PolicyBuilder builder() {
        return new PolicyBuilder();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getEnforcementLevel() {
        return enforcementLevel;
    }

    public void setEnforcementLevel(String enforcementLevel) {
        this.enforcementLevel = enforcementLevel;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class PolicyBuilder {
        private String id;
        private String code;
        private String name;
        private String description;
        private String category;
        private String type;
        private boolean isActive;
        private String enforcementLevel;
        private String configuration;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public PolicyBuilder id(String id) {
            this.id = id;
            return this;
        }

        public PolicyBuilder code(String code) {
            this.code = code;
            return this;
        }

        public PolicyBuilder name(String name) {
            this.name = name;
            return this;
        }

        public PolicyBuilder description(String description) {
            this.description = description;
            return this;
        }

        public PolicyBuilder category(String category) {
            this.category = category;
            return this;
        }

        public PolicyBuilder type(String type) {
            this.type = type;
            return this;
        }

        public PolicyBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public PolicyBuilder enforcementLevel(String enforcementLevel) {
            this.enforcementLevel = enforcementLevel;
            return this;
        }

        public PolicyBuilder configuration(String configuration) {
            this.configuration = configuration;
            return this;
        }

        public PolicyBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PolicyBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Policy build() {
            return new Policy(id, code, name, description, category, type, isActive, enforcementLevel, configuration,
                    createdAt, updatedAt);
        }
    }
}
