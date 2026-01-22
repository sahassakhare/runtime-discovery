package com.maverick.feature.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "MFE_APPLICATION_VERSIONS")
public class MfeApplicationVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_ID", nullable = false)
    private MfeApplication application;

    @Column(length = 50)
    private String version;

    @Column(name = "REGISTERED_AT")
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column(length = 50)
    private String environment = "Development";

    private boolean latest = false;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "applicationVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MfeSharedModule> sharedModules = new ArrayList<>();

    @OneToMany(mappedBy = "applicationVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MfeExposedModule> exposedModules = new ArrayList<>();

    @OneToMany(mappedBy = "applicationVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MfeDependency> dependencies = new ArrayList<>();

    @OneToMany(mappedBy = "applicationVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MfeMetadata> metadata = new ArrayList<>();

    @OneToOne(mappedBy = "applicationVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    private MfeHealth health;

    public MfeApplicationVersion() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MfeApplication getApplication() {
        return application;
    }

    public void setApplication(MfeApplication application) {
        this.application = application;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public boolean isLatest() {
        return latest;
    }

    public void setLatest(boolean latest) {
        this.latest = latest;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<MfeSharedModule> getSharedModules() {
        return sharedModules;
    }

    public void setSharedModules(List<MfeSharedModule> sharedModules) {
        this.sharedModules = sharedModules;
    }

    public List<MfeExposedModule> getExposedModules() {
        return exposedModules;
    }

    public void setExposedModules(List<MfeExposedModule> exposedModules) {
        this.exposedModules = exposedModules;
    }

    public List<MfeDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<MfeDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public List<MfeMetadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<MfeMetadata> metadata) {
        this.metadata = metadata;
    }

    public MfeHealth getHealth() {
        return health;
    }

    public void setHealth(MfeHealth health) {
        this.health = health;
    }

    public String getMetadataValue(String name) {
        return metadata.stream()
                .filter(m -> m.getName().equals(name))
                .map(MfeMetadata::getValue)
                .findFirst()
                .orElse(null);
    }
}
