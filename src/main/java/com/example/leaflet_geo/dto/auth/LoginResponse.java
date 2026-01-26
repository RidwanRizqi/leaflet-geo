package com.example.leaflet_geo.dto.auth;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String nama;
    private String idUnit;
    private String role;
    private String token;

    // Explicit builder method in case Lombok's @Builder doesn't work properly
    public static LoginResponseBuilder builder() {
        return new LoginResponseBuilder();
    }

    public static class LoginResponseBuilder {
        private String nama;
        private String idUnit;
        private String role;
        private String token;

        public LoginResponseBuilder nama(String nama) {
            this.nama = nama;
            return this;
        }

        public LoginResponseBuilder idUnit(String idUnit) {
            this.idUnit = idUnit;
            return this;
        }

        public LoginResponseBuilder role(String role) {
            this.role = role;
            return this;
        }

        public LoginResponseBuilder token(String token) {
            this.token = token;
            return this;
        }

        public LoginResponse build() {
            LoginResponse response = new LoginResponse();
            response.nama = this.nama;
            response.idUnit = this.idUnit;
            response.role = this.role;
            response.token = this.token;
            return response;
        }
    }

    // Explicit getters for fields
    public String getNama() {
        return nama;
    }

    public String getIdUnit() {
        return idUnit;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}
