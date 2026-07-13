package com.example.backend.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

@Service
public class GoogleTokenVerifierImpl implements GoogleTokenVerifier {
    private final String googleClientId;
    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifierImpl(@Value("${google.client.id:}") String googleClientId) {
        this.googleClientId = googleClientId;
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(googleClientId == null || googleClientId.isBlank()
                        ? Collections.emptyList()
                        : Collections.singletonList(googleClientId))
                .build();
    }

    @Override
    public String verifyEmail(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idToken is required");
        }
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Google client id is not configured");
        }

        try {
            GoogleIdToken verifiedToken = verifier.verify(idToken.trim());
            if (verifiedToken == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = verifiedToken.getPayload();
            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google email is not verified");
            }
            String email = payload.getEmail();
            if (email == null || email.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google ID token does not contain email");
            }
            return email.trim();
        } catch (GeneralSecurityException | IOException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unable to verify Google ID token", exception);
        }
    }
}
