package com.safarnama.backend.user;

public class AppleSignInRequest {
    // The signed JWT Apple hands the iOS app (ASAuthorizationAppleIDCredential.identityToken).
    private String identityToken;

    // Apple only includes these in the very first authorization for a given
    // user; the iOS app must capture and forward them that one time, since
    // they won't come back on later sign-ins.
    private String email;
    private String name;

    public String getIdentityToken() { return identityToken; }
    public void setIdentityToken(String identityToken) { this.identityToken = identityToken; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}