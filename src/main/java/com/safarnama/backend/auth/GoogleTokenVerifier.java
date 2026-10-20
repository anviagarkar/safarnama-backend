package com.safarnama.backend.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

// Verifies Google Sign-In ID tokens server-side. Unlike Apple, Google ships
// an official verifier (GoogleIdTokenVerifier) that handles fetching and
// caching Google's signing keys and checking signature/issuer/audience/expiry
// itself, so there's much less to do by hand here than in AppleTokenVerifier.
@Component
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    // Returns Google's verified claims (subject/email/name) once the token
    // is confirmed genuine. Throws IllegalArgumentException on any failure
    // to verify — callers should treat that as "reject this sign-in".
    public GoogleIdToken.Payload verifyAndExtractPayload(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new IllegalArgumentException("Google ID token signature/claims are invalid");
            }
            return idToken.getPayload();
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalArgumentException("Could not verify Google ID token", e);
        }
    }
}