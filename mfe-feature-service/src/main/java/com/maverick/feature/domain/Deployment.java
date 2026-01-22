package com.maverick.feature.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Deployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private MfeApplicationVersion version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Environment environment; // "PRODUCTION", "STAGING"

    @Column(nullable = true)
    private String tenantId; // NULL = Global

    @Column(columnDefinition = "boolean default false")
    private boolean active;

    private int weight; // 0-100

    @Column(columnDefinition = "boolean default false")
    private boolean isLocked;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Deployment(MfeApplicationVersion version, Environment environment, boolean active) {
        this.version = version;
        this.environment = environment;
        this.active = active;
        this.weight = 100;
    }

    public Deployment() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MfeApplicationVersion getVersion() {
        return version;
    }

    public void setVersion(MfeApplicationVersion version) {
        this.version = version;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
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
}
