package com.maverick.feature.domain;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Deployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private Version version;

    @Column(nullable = false)
    private String environment; // "production", "staging"

    @Column(nullable = true)
    private String tenantId; // NULL = Global

    @Column(columnDefinition = "boolean default false")
    private boolean active;

    private int weight; // 0-100

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Deployment(Version version, String environment, boolean active) {
        this.version = version;
        this.environment = environment;
        this.active = active;
        this.weight = 100;
    }
}
