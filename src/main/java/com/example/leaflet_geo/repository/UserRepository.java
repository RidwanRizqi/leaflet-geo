package com.example.leaflet_geo.repository;

import com.example.leaflet_geo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);

    Optional<User> findByToken(String token);

    boolean existsByUsername(String username);
}