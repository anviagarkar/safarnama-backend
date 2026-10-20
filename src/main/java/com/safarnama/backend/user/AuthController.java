package com.safarnama.backend.user;

import com.safarnama.backend.auth.AppleTokenVerifier;
import com.safarnama.backend.auth.GoogleTokenVerifier;
import com.safarnama.backend.auth.JwtService;
import com.safarnama.backend.email.EmailService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.security.SecureRandom;
import java.time.LocalDateTime;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final SecureRandom secureRandom = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailService emailService;
    private final AppleTokenVerifier appleTokenVerifier;
    private final GoogleTokenVerifier googleTokenVerifier;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, PasswordResetTokenRepository resetTokenRepository, EmailService emailService, AppleTokenVerifier appleTokenVerifier, GoogleTokenVerifier googleTokenVerifier) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.resetTokenRepository = resetTokenRepository;
        this.emailService = emailService;
        this.appleTokenVerifier = appleTokenVerifier;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    // Normalizes an email the same way everywhere it's read or written, so
    // "Test@x.com" and "test@x.com" are always treated as the same account
    // instead of silently becoming two different rows.
    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (userRepository.findByEmail(email) != null) {
            return ResponseEntity.badRequest().body("Email already in use");
        }

        String hashed = passwordEncoder.encode(request.getPassword());
        User user = new User(email, hashed);
        user.setName(request.getName());

        // The findByEmail check above and this save() aren't atomic, so two
        // concurrent signups for the same email can both pass the check
        // before either inserts. The DB-level unique constraint on User.email
        // is the real backstop — if it trips, this just means someone else
        // won the race a moment ago, so report it the same way as the normal
        // duplicate-email case rather than a generic 500.
        try {
            userRepository.save(user);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Email already in use");
        }

        return ResponseEntity.ok("Signup successful");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.getEmail()));

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/apple")
    public ResponseEntity<?> appleSignIn(@RequestBody AppleSignInRequest request) {
        String appleUserId;
        try {
            appleUserId = appleTokenVerifier.verifyAndExtractSubject(request.getIdentityToken());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body("Invalid Apple identity token");
        }

        User user = userRepository.findByAppleUserId(appleUserId);

        // First time we've seen this Apple user id — either link it to an
        // existing password/Google account with the same email, or create
        // a brand-new account. ASSUMPTION: same-email accounts auto-link
        // across providers rather than being rejected as a conflict — flag
        // this if you'd rather require explicit linking.
        if (user == null) {
            String email = normalizeEmail(request.getEmail());
            user = (email != null) ? userRepository.findByEmail(email) : null;

            if (user == null) {
                if (email == null) {
                    // Apple only sends the email on the very first
                    // authorization. If this user isn't already in our DB
                    // by appleUserId and didn't resend an email either,
                    // there's nothing to match or create an account from.
                    return ResponseEntity.badRequest().body("No account found for this Apple ID");
                }
                user = new User(email, null);
                if (request.getName() != null) {
                    user.setName(request.getName());
                }
            }

            user.setAppleUserId(appleUserId);
            userRepository.save(user);
        }

        String token = jwtService.generateToken(user.getEmail());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleSignIn(@RequestBody GoogleSignInRequest request) {
        GoogleIdToken.Payload payload;
        try {
            payload = googleTokenVerifier.verifyAndExtractPayload(request.getIdToken());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body("Invalid Google ID token");
        }

        String googleUserId = payload.getSubject();
        String email = normalizeEmail(payload.getEmail());

        User user = userRepository.findByGoogleUserId(googleUserId);

        // Same auto-link assumption as Apple above.
        if (user == null) {
            user = userRepository.findByEmail(email);

            if (user == null) {
                user = new User(email, null);
                Object name = payload.get("name");
                if (name != null) {
                    user.setName(name.toString());
                }
            }

            user.setGoogleUserId(googleUserId);
            userRepository.save(user);
        }

        String token = jwtService.generateToken(user.getEmail());
        return ResponseEntity.ok(token);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(Authentication authentication, @RequestBody UpdateProfileRequest request) {
        User user = userRepository.findByEmail(authentication.getName());
        user.setName(request.getName());
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteAccount(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        userRepository.delete(user);
        return ResponseEntity.ok("Account deleted");
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.getEmail()));

        if (user == null) {
            System.out.println("Forgot-password: no user found for provided email");
            return ResponseEntity.ok("If that email exists, a reset code has been sent.");
        }

        String token = String.valueOf(secureRandom.nextInt(900000) + 100000);
        PasswordResetToken resetToken = new PasswordResetToken(token, user.getEmail(), LocalDateTime.now().plusHours(1));
        resetTokenRepository.save(resetToken);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        } catch (Exception ex) {
            System.out.println("Forgot-password: email send failed - " + ex.getMessage());
        }

        return ResponseEntity.ok("If that email exists, a reset code has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(request.getToken());

        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Invalid or expired code");
        }

        User user = userRepository.findByEmail(resetToken.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        resetTokenRepository.delete(resetToken);

        return ResponseEntity.ok("Password reset successful");
    }
}