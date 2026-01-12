package com.example.leaflet_geo.dto.auth;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String oldPass;
    private String newPass;
}