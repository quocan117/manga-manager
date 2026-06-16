package com.example.backend.dto;

public class GuestAccessRequest {
    private String sessionToken;
    private String ipAddress;
    private String userAgent;
    public String getSessionToken() {
        return sessionToken;
    }
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public String getUserAgent() {
        return userAgent;
    }
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    
}
