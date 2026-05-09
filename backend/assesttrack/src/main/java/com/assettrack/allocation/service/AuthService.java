package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.AuthResponse;
import com.assettrack.allocation.dto.LoginRequest;
import com.assettrack.allocation.dto.RegisterRequest;
import com.assettrack.allocation.entity.Role;
import com.assettrack.allocation.entity.User;
import com.assettrack.allocation.exception.BadRequestException;
import com.assettrack.allocation.repository.UserRepository;
import com.assettrack.allocation.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // ── Register ─────────────────────────────────────────────────────────

    /**
     * Creates a new user account.
     * The first registered user receives the ADMIN role; all subsequent
     * users are assigned DEVELOPER by default.
     * Returns a JWT so the user is immediately logged in after sign-up.
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use: " + request.getEmail());
        }

        // First user becomes admin; everyone else is a developer
        Role role = userRepository.count() == 0 ? Role.ADMIN : Role.DEVELOPER;

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return buildResponse(user, token);
    }

    // ── Login ─────────────────────────────────────────────────────────────

    /**
     * Authenticates a user by email + password.
     * Spring's AuthenticationManager handles credential verification and
     * throws BadCredentialsException on failure (mapped to 401 by the
     * global exception handler).
     */
    public AuthResponse login(LoginRequest request) {
        // This throws AuthenticationException if credentials are wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        String token = jwtService.generateToken(user);
        return buildResponse(user, token);
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private AuthResponse buildResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }
}
