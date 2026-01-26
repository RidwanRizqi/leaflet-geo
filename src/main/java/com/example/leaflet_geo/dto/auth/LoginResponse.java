package com.example.leaflet_geo.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String nama;
    private String idUnit;
    private String role;
    private String token;
}