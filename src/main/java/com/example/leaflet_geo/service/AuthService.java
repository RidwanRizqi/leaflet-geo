package com.example.leaflet_geo.service;

import com.example.leaflet_geo.dto.auth.ChangePasswordRequest;
import com.example.leaflet_geo.dto.auth.LoginRequest;
import com.example.leaflet_geo.dto.auth.LoginResponse;
import com.example.leaflet_geo.model.User;
import com.example.leaflet_geo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Authentication Service
 * 
 * Handles login, logout, password change, and admin creation.
 * Matches legacy Yii 2 UserController behavior.
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Login user and generate token
     * Matches legacy: POST /user/login
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new RuntimeException("User tidak aktif");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password salah");
        }

        // Generate new token (64 char random string like Yii)
        String token = UUID.randomUUID().toString().replace("-", "") +
                UUID.randomUUID().toString().replace("-", "");
        user.setToken(token);
        userRepository.save(user);

        return LoginResponse.builder()
                .nama(user.getNama())
                .idUnit(user.getIdUnit())
                .role(user.getRole())
                .token(token)
                .build();
    }

    /**
     * Logout user by clearing token
     * Matches legacy: POST /user/logout
     */
    public void logout(User currentUser) {
        currentUser.setToken(null);
        userRepository.save(currentUser);
    }

    /**
     * Change password
     * Matches legacy: POST /user/changepass
     */
    public void changePassword(User currentUser, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getOldPass(), currentUser.getPassword())) {
            throw new RuntimeException("Password lama tidak valid");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPass()));
        currentUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(currentUser);
    }

    /**
     * Create admin user
     * Matches legacy: POST /user/createadmin
     */
    public User createAdmin(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username sudah digunakan");
        }

        User admin = new User();
        admin.setId(UUID.randomUUID().toString());
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setNama("Administrator");
        admin.setIsAdmin(true);
        admin.setRole("ADMIN");
        admin.setIsActive(true);
        admin.setCreatedAt(LocalDateTime.now());

        return userRepository.save(admin);
    }

    /**
     * Get current user by token
     */
    public User getCurrentUser(String token) {
        return userRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token tidak valid"));
    }
}
