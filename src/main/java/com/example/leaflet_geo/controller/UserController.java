package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.ApiResponse;
import com.example.leaflet_geo.model.User;
import com.example.leaflet_geo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        // Clear sensitive info
        for (User user : users) {
            user.setPassword(null);
            user.setToken(null);
        }
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User request) {
        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Username already exists"));
            }

            User user = new User();
            user.setId(UUID.randomUUID().toString());
            user.setUsername(request.getUsername().trim().toLowerCase());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(request.getRole());
            user.setNama(request.getNama() != null && !request.getNama().trim().isEmpty() 
                    ? request.getNama().trim() : request.getUsername());
            user.setIsAdmin("ADMIN".equalsIgnoreCase(request.getRole()));
            user.setIsActive(true);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            User savedUser = userRepository.save(user);
            savedUser.setPassword(null); 
            savedUser.setToken(null);
            return ResponseEntity.ok(ApiResponse.success("User created successfully", savedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String username) {
        try {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                userRepository.delete(user);
                return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete user: " + e.getMessage()));
        }
    }
}
