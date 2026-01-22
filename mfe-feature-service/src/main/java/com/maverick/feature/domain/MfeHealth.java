package com.maverick.feature.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "MFE_MICROFRONTEND_HEALTH")
public class MfeHealth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_VERSION_ID", nullable = false)
    private MfeApplicationVersion applicationVersion;

    private String url;

    @Column(nullable = false)
    private Integer status;

    @Column(name = "LAST_UPTIME", nullable = false)
    private LocalDateTime lastUptime;

    @Column(name = "LAST_DOWNTIME")
    private LocalDateTime lastDowntime;

    public MfeHealth() {
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getLastUptime() {
        return lastUptime;
    }

    public void setLastUptime(LocalDateTime lastUptime) {
        this.lastUptime = lastUptime;
    }

    public LocalDateTime getLastDowntime() {
        return lastDowntime;
    }

    public void setLastDowntime(LocalDateTime lastDowntime) {
        this.lastDowntime = lastDowntime;
    }
}
