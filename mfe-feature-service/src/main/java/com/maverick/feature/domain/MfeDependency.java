package com.maverick.feature.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "MFE_DEPENDENCIES")
public class MfeDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_VERSION_ID", nullable = false)
    private MfeApplicationVersion applicationVersion;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String version;

    @Column(name = "DEP_TYPE", nullable = false)
    private String dependencyType;

    public MfeDependency() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MfeApplicationVersion getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(MfeApplicationVersion applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

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

    public String getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(String dependencyType) {
        this.dependencyType = dependencyType;
    }
}
