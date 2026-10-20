package com.safarnama.backend.user;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.safarnama.backend.data.CountryData;
import java.util.List;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    @JsonIgnore
    private String passwordHash;
    private String name;

    @Column(unique = true)
    private String appleUserId;

    @Column(unique = true)
    private String googleUserId;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CountryData> countries;

    public User() {
    }

    public User(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public String getAppleUserId() { return appleUserId; }
    public void setAppleUserId(String appleUserId) { this.appleUserId = appleUserId; }

    public String getGoogleUserId() { return googleUserId; }
    public void setGoogleUserId(String googleUserId) { this.googleUserId = googleUserId; }
}