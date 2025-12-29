package com.maverick.feature.domain;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Tenant {

    @Id
    @Column(nullable = false, unique = true)
    private String id; // e.g., "acme-corp"

    @Column(nullable = false)
    private String name; // e.g., "Acme Corporation"

    private String contactEmail;

    @Column(columnDefinition = "boolean default true")
    private boolean active;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Tenant(String id, String name) {
        this.id = id;
        this.name = name;
        this.active = true;
    }
}
