package com.maverick.feature.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "runtime_instances")
@EntityListeners(AuditingEntityListener.class)
public class RuntimeInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String appName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Environment environment;

    @Column(nullable = false)
    private String url;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime lastHeartbeat;

    public RuntimeInstance() {
    }

    public RuntimeInstance(Long id, String appName, Environment environment, String url, LocalDateTime createdAt,
            LocalDateTime lastHeartbeat) {
        this.id = id;
        this.appName = appName;
        this.environment = environment;
        this.url = url;
        this.createdAt = createdAt;
        this.lastHeartbeat = lastHeartbeat;
    }

    public static RuntimeInstanceBuilder builder() {
        return new RuntimeInstanceBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public static class RuntimeInstanceBuilder {
        private Long id;
        private String appName;
        private Environment environment;
        private String url;
        private LocalDateTime createdAt;
        private LocalDateTime lastHeartbeat;

        public RuntimeInstanceBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RuntimeInstanceBuilder appName(String appName) {
            this.appName = appName;
            return this;
        }

        public RuntimeInstanceBuilder environment(Environment environment) {
            this.environment = environment;
            return this;
        }

        public RuntimeInstanceBuilder url(String url) {
            this.url = url;
            return this;
        }

        public RuntimeInstanceBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public RuntimeInstanceBuilder lastHeartbeat(LocalDateTime lastHeartbeat) {
            this.lastHeartbeat = lastHeartbeat;
            return this;
        }

        public RuntimeInstance build() {
            return new RuntimeInstance(id, appName, environment, url, createdAt, lastHeartbeat);
        }
    }
}
