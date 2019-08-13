package com.mazhangjing.cloud.tuchuang.oss;

import com.aliyun.oss.OSSClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oss")
public class OSSConfig {
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String endpoint;
    private String fileHost;

    public OSSClient getClientInstance() {
        return new OSSClient(endpoint, accessKeyId, accessKeySecret);
    }

    public String getFileHost() {
        return fileHost;
    }

    public void setFileHost(String fileHost) {
        this.fileHost = fileHost;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return "OSSConfig{" +
                "accessKeyId='" + accessKeyId + '\'' +
                ", accessKeySecret='" + accessKeySecret + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", fileHost='" + fileHost + '\'' +
                '}';
    }
}
