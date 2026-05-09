package com.assettrack.allocation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * =====================================================================
 * DEV SECURITY CONFIG — Authentication bypassed for local testing.
 *
 * ⚠️  NOTE FOR TEAM:
 *   This config disables JWT auth so you can test all Allocation
 *   endpoints freely with Postman / curl / browser without a token.
 *
 *   When the Authentication member finishes their work, replace this
 *   file with the real JWT SecurityFilterChain and remove the
 *   in-memory users below.
 *
 *   The @PreAuthorize annotations on the controller ARE still active
 *   (EnableMethodSecurity is ON), so you can test role checks using
 *   the 3 built-in users:
 *     - admin   / admin123   → ROLE_ADMIN
 *     - manager / manager123 → ROLE_MANAGER
 *     - dev     / dev123     → ROLE_DEVELOPER
 *
 *   Use Basic Auth in Postman to pick which role you test with.
 * =====================================================================
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * Permit all requests — no JWT filter needed yet.
     * CSRF disabled for REST API testing.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
            )
            // Use HTTP Basic Auth so you can switch roles in Postman easily
            .httpBasic(basic -> {});

        return http.build();
    }

    /**
     * Three in-memory users mirroring the real role structure.
     * Remove this bean when real JWT auth is integrated.
     */
    @Bean
    public UserDetailsService inMemoryUsers() {
        var admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("admin123")
                .roles("ADMIN")
                .build();

        var manager = User.withDefaultPasswordEncoder()
                .username("manager")
                .password("manager123")
                .roles("MANAGER")
                .build();

        var developer = User.withDefaultPasswordEncoder()
                .username("dev")
                .password("dev123")
                .roles("DEVELOPER")
                .build();

        return new InMemoryUserDetailsManager(admin, manager, developer);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
