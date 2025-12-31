package com.maverick.feature.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Version {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String version; // 1.2.0

    @Column(nullable = false)
    private String remoteEntry; // URL

    // Artifact metadata
    private String integrity; // SRI hash

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "microfrontend_id")
    private Microfrontend microfrontend;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Microfrontend getMicrofrontend() {
        return microfrontend;
    }

    public void setMicrofrontend(Microfrontend microfrontend) {
        this.microfrontend = microfrontend;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Version{" +
                "id=" + id +
                ", version='" + version + '\'' +
                ", remoteEntry='" + remoteEntry + '\'' +
                ", integrity='" + integrity + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
