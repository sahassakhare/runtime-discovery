package com.maverick.feature.domain;

import jakarta.persistence.*;
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

    // Artifact metadata
    private String integrity; // SRI hash

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "microfrontend_id")
    private Microfrontend microfrontend;

    private LocalDateTime createdAt = LocalDateTime.now();
}
