package com.example.backend.dto;

public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String specialty;

    public LoginResponse(
            String token,
            Long userId,
            String username,
            String email,
            String role,
            String specialty) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.specialty = specialty;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

}
