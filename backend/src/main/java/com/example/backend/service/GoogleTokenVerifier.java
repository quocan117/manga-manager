package com.example.backend.service;

public interface GoogleTokenVerifier {
    String verifyEmail(String idToken);
}
