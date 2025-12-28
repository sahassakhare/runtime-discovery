package com.maverick.feature.dto;

import lombok.Data;

@Data
public class RegisterInstanceRequest {
    private String appName;
    private String environment;
    private String url;
}
