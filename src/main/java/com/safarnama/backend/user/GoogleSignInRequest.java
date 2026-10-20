package com.safarnama.backend.user;

public class GoogleSignInRequest {
    // The signed JWT Google hands the iOS app after a successful Google Sign-In.
    private String idToken;

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
}