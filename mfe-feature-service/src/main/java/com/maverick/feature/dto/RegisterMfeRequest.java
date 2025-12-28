package com.maverick.feature.dto;

import lombok.Data;

@Data
public class RegisterMfeRequest {
    private String name; // e.g. "payment-remote"
    private String version; // e.g. "1.2.0"
    private String remoteEntry; // e.g. "http://cdn.../remoteEntry.js"
    private String integrity; // SRI Hash (optional)

    // Additional metadata from plugin?
    private String type; // "var", "module", etc.
}
