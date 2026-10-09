package com.safarnama.backend.data;

import com.safarnama.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<TripData, Long> {
    List<TripData> findByOwner(User owner);
}