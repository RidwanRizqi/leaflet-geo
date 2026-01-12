package com.example.leaflet_geo.security;

import com.example.leaflet_geo.model.User;
import com.example.leaflet_geo.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Token Authentication Filter
 * 
 * Validates Bearer token from Authorization header against database.
 * Matches legacy Yii 2 HttpBearerAuth behavior.
 */
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Optional<User> userOpt = userRepository.findByToken(token);

            if (userOpt.isPresent() && Boolean.TRUE.equals(userOpt.get().getIsActive())) {
                User user = userOpt.get();

                // Build authorities based on role
                List<GrantedAuthority> authorities = new ArrayList<>();
                if (Boolean.TRUE.equals(user.getIsAdmin())) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                }
                if (user.getRole() != null && !user.getRole().isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));
                }
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

                // Set authentication in context
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
                        authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
