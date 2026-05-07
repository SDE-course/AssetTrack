package com.assettrack.allocation.security;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * Security Integration Note
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * This module relies on JWT Authentication already implemented by the team.
 * The only requirement is that the existing SecurityFilterChain / JwtAuthFilter:
 *
 *   1. Sets the Spring Security context with a UsernamePasswordAuthenticationToken
 *      whose principal is a UserDetails whose getUsername() returns the user's
 *      database ID as a String.
 *
 *   2. Populates the GrantedAuthorities with roles prefixed "ROLE_":
 *        e.g. ROLE_ADMIN, ROLE_MANAGER, ROLE_DEVELOPER
 *
 *   3. Enables Method Security in your main config class:
 *
 *        @EnableMethodSecurity(prePostEnabled = true)
 *        public class SecurityConfig { ... }
 *
 * No additional security beans are needed in this module.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public final class SecurityIntegrationNote {
    private SecurityIntegrationNote() {}
}
