package com.safarnama.backend.auth;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

// Verifies Sign in with Apple identity tokens server-side. Apple has no
// official Java SDK (unlike Google's GoogleIdTokenVerifier), so this does by
// hand what one would do: fetch Apple's public signing keys (JWKS), find the
// key matching the token's "kid", check the RS256 signature, then check the
// standard claims (issuer, audience, expiry) ourselves.
@Component
public class AppleTokenVerifier {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";

    // This must be your Apple "Services ID" identifier (the one registered
    // for Sign in with Apple), which is what shows up as the token's "aud"
    // claim — NOT your app's bundle ID, unless you're verifying tokens that
    // came straight from a native iOS authorization (those do use the
    // bundle ID as aud). Confirm which flow your iOS app uses before
    // setting APPLE_CLIENT_ID.
    private final String expectedAudience;

    // Cached so a normal sign-in doesn't hit Apple's key endpoint every
    // time. Apple rotates these keys infrequently; if a token's "kid" isn't
    // in the cache, we refetch once in case it just rotated.
    private volatile JWKSet cachedKeySet;

    public AppleTokenVerifier(@Value("${apple.client-id}") String expectedAudience) {
        this.expectedAudience = expectedAudience;
    }

    // Returns Apple's stable per-user identifier (the "sub" claim) once the
    // token is confirmed genuine. Throws IllegalArgumentException on any
    // failure to verify — callers should treat that as "reject this sign-in".
    public String verifyAndExtractSubject(String identityToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(identityToken);
            String keyId = signedJWT.getHeader().getKeyID();

            RSAKey signingKey = findKey(keyId);
            if (signingKey == null) {
                throw new IllegalArgumentException("No matching Apple signing key for kid=" + keyId);
            }

            if (!signedJWT.verify(new RSASSAVerifier(signingKey))) {
                throw new IllegalArgumentException("Apple identity token signature is invalid");
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            if (!APPLE_ISSUER.equals(claims.getIssuer())) {
                throw new IllegalArgumentException("Unexpected issuer: " + claims.getIssuer());
            }
            if (claims.getAudience() == null || !claims.getAudience().contains(expectedAudience)) {
                throw new IllegalArgumentException("Token audience does not match this app");
            }
            if (claims.getExpirationTime() == null || claims.getExpirationTime().before(Date.from(Instant.now()))) {
                throw new IllegalArgumentException("Apple identity token has expired");
            }

            return claims.getSubject();
        } catch (ParseException | IOException e) {
            throw new IllegalArgumentException("Could not verify Apple identity token", e);
        } catch (com.nimbusds.jose.JOSEException e) {
            throw new IllegalArgumentException("Apple identity token signature check failed", e);
        }
    }

    private RSAKey findKey(String keyId) throws IOException, ParseException {
        RSAKey key = findInCachedKeySet(keyId);
        if (key != null) {
            return key;
        }
        refreshKeySet();
        return findInCachedKeySet(keyId);
    }

    private synchronized void refreshKeySet() throws IOException, ParseException {
        cachedKeySet = JWKSet.load(new URL(APPLE_KEYS_URL));
    }

    private RSAKey findInCachedKeySet(String keyId) {
        JWKSet keySet = cachedKeySet;
        if (keySet == null) {
            return null;
        }
        JWK match = keySet.getKeyByKeyId(keyId);
        return (match instanceof RSAKey) ? (RSAKey) match : null;
    }
}