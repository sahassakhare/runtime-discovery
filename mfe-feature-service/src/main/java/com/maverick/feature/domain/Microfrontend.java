package com.maverick.feature.domain;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Microfrontend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // e.g., "payment-remote"

    private String description;

    // To support "Standard", "Module Federation", etc.
    private String type;

    // The name of the FF4j group for dynamic flag association
    private String featureGroupName;

    @OneToMany(mappedBy = "microfrontend", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Version> versions = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Microfrontend(String name) {
        this.name = name;
    }
}
