package com.example.leaflet_geo.config;

import com.example.leaflet_geo.security.TokenAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private TokenAuthenticationFilter tokenAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no auth required
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/createadmin").permitAll()
                        .requestMatchers("/api/database-test/**").permitAll()
                        .requestMatchers("/api/bidang/health").permitAll()
                        .requestMatchers("/api/bidang/test").permitAll()
                        .requestMatchers("/api/bidang/setup").permitAll()
                        // Map-related endpoints (public for map display)
                        .requestMatchers("/api/bidang/**").permitAll()
                        .requestMatchers("/api/kecamatan/**").permitAll()
                        .requestMatchers("/api/kelurahan/**").permitAll()
                        .requestMatchers("/api/blok/**").permitAll()
                        .requestMatchers("/api/pemda/**").permitAll()
                        .requestMatchers("/api/map/**").permitAll()
                        .requestMatchers("/api/bprd/**").permitAll()
                        .requestMatchers("/api/shapefile/**").permitAll()
                        .requestMatchers("/api/ref-kecamatan/**").permitAll()
                        .requestMatchers("/api/ref-kelurahan/**").permitAll()
                        .requestMatchers("/api/dat-objek-pajak/**").permitAll()
                        .requestMatchers("/api/dat-subjek-pajak/**").permitAll()
                        // Dashboard endpoints (public for dashboards)
                        .requestMatchers("/api/pendapatan/**").permitAll()
                        .requestMatchers("/api/dashboard/**").permitAll()
                        .requestMatchers("/api/pajak/**").permitAll()
                        // All other endpoints require authentication
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(tokenAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
