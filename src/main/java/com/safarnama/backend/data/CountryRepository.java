package com.safarnama.backend.data;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CountryRepository extends JpaRepository<CountryData, Long> {
    java.util.List<CountryData> findByOwner(com.safarnama.backend.user.User owner);
}