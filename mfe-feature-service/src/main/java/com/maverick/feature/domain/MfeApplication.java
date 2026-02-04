package com.maverick.feature.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "MFE_APPLICATIONS")
public class MfeApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "APP_ID")
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GROUP_ID", nullable = false)
    private MfeApplicationGroup group;

    private String tags;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MfeApplicationVersion> versions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "MFE_APP_BEHAVIORS", joinColumns = @JoinColumn(name = "APP_ID"))
    @Column(name = "BEHAVIOR")
    private List<String> behaviors = new ArrayList<>();

    public MfeApplication() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MfeApplicationGroup getGroup() {
        return group;
    }

    public void setGroup(MfeApplicationGroup group) {
        this.group = group;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<MfeApplicationVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<MfeApplicationVersion> versions) {
        this.versions = versions;
    }

    public List<String> getBehaviors() {
        return behaviors;
    }

    public void setBehaviors(List<String> behaviors) {
        this.behaviors = behaviors;
    }
}
