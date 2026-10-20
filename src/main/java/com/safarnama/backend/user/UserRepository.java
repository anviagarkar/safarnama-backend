package com.safarnama.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    User findByAppleUserId(String appleUserId);

    User findByGoogleUserId(String googleUserId);
}