package com.maverick.feature.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "MFE_EXPOSED_MODULES")
public class MfeExposedModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_VERSION_ID", nullable = false)
    private MfeApplicationVersion applicationVersion;

    @Column(nullable = false)
    private String name;

    @Column(name = "FILE_PATH", nullable = false)
    private String filePath;

    public MfeExposedModule() {
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
