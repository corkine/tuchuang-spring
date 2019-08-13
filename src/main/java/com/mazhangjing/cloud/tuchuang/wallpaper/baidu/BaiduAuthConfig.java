package com.mazhangjing.cloud.tuchuang.wallpaper.baidu;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "baidu")
public class BaiduAuthConfig {

    private String appId;
    private String apiKey;
    private String secureKey;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecureKey() {
        return secureKey;
    }

    public void setSecureKey(String secureKey) {
        this.secureKey = secureKey;
    }

    public BaiduAuthConfig(String appId, String apiKey, String secureKey) {
        this.appId = appId;
        this.apiKey = apiKey;
        this.secureKey = secureKey;
    }

    public BaiduAuthConfig() {
    }
}
