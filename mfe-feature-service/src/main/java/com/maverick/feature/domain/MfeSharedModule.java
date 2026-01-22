package com.maverick.feature.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "MFE_SHARED_MODULES")
public class MfeSharedModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_VERSION_ID", nullable = false)
    private MfeApplicationVersion applicationVersion;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    private String version;

    public MfeSharedModule() {
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
