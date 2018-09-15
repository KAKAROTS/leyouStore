package com.leyou.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "ly.upload")
public class UploadProperties {
private String baseUrl;
private String localPath;
private List<String> allowContentTypes;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public List<String> getAllowContentTypes() {
        return allowContentTypes;
    }

    public void setAllowContentTypes(List<String> allowContentTypes) {
        this.allowContentTypes = allowContentTypes;
    }
}
