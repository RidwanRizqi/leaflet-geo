package com.example.leaflet_geo.dto.auth;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
