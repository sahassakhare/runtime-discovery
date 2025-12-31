package com.maverick.feature.dto;

public class RegisterInstanceRequest {
    private String appName;
    private String environment;
    private String url;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "RegisterInstanceRequest{" +
                "appName='" + appName + '\'' +
                ", environment='" + environment + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
