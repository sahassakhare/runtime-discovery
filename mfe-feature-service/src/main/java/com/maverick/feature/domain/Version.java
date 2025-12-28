package com.maverick.feature.domain;

import javax.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Data
@ToString(exclude = "microfrontend")
public class Version {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String version; // 1.2.0

    @Column(nullable = false)
    private String remoteEntry; // URL

    private String integrity; // SRI hash

    private boolean active; // Is this the currently selected one?

    // For track-based rollouts (e.g., STABLE, CANARY, BETA)
    private String releaseTrack;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "microfrontend_id")
    private Microfrontend microfrontend;

    private LocalDateTime createdAt = LocalDateTime.now();
}
