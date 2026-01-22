package com.maverick.feature.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "MFE_CONSUMED_REMOTES")
public class MfeConsumedRemote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONSUMER_VERSION_ID", nullable = false)
    private MfeApplicationVersion consumerVersion;

    @Column(name = "REMOTE_NAME", nullable = false)
    private String remoteName;

    @Column(name = "USED_MODULES")
    private String usedModules;

    @Column(name = "IS_DYNAMIC")
    private Boolean isDynamic = false;

    @Column(name = "ENVIRONMENT")
    private String environment;

    public MfeConsumedRemote() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MfeApplicationVersion getConsumerVersion() {
        return consumerVersion;
    }

    public void setConsumerVersion(MfeApplicationVersion consumerVersion) {
        this.consumerVersion = consumerVersion;
    }

    public String getRemoteName() {
        return remoteName;
    }

    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

    public String getUsedModules() {
        return usedModules;
    }

    public void setUsedModules(String usedModules) {
        this.usedModules = usedModules;
    }

    public Boolean getDynamic() {
        return isDynamic;
    }

    public void setDynamic(Boolean dynamic) {
        isDynamic = dynamic;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}
