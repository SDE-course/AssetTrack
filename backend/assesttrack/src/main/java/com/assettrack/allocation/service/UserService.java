package com.assettrack.usermanagement.service;

import com.assettrack.usermanagement.domain.Role;
import com.assettrack.usermanagement.domain.User;
import com.assettrack.usermanagement.dto.UserDTOs.*;
import com.assettrack.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Bean provided by Member 1 (Security config)

    // ─── Helpers ────────────────────────────────────────────────────────

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    private UserResponse toResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setFullName(user.getFullName());
        res.setEmail(user.getEmail());
        res.setRole(user.getRole());
        res.setActive(user.isActive());
        return res;
    }

    // ─── Get All Users ───────────────────────────────────────────────────

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ─── Get User By ID ──────────────────────────────────────────────────

    public UserResponse getUserById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // ─── Create User (Admin only) ─────────────────────────────────────────

    public UserResponse createUser(CreateUserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already in use: " + req.getEmail());
        }

        User user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .active(true)
                .build();

        return toResponse(userRepository.save(user));
    }

    // ─── Update User Info ────────────────────────────────────────────────

    public UserResponse updateUser(Long id, UpdateUserRequest req) {
        User user = findOrThrow(id);

        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            user.setFullName(req.getFullName());
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            if (!req.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(req.getEmail())) {
                throw new RuntimeException("Email already in use: " + req.getEmail());
            }
            user.setEmail(req.getEmail());
        }

        return toResponse(userRepository.save(user));
    }

    // ─── Change Role ─────────────────────────────────────────────────────

    public UserResponse changeRole(Long id, ChangeRoleRequest req) {
        User user = findOrThrow(id);
        user.setRole(req.getRole());
        return toResponse(userRepository.save(user));
    }

    // ─── Soft Delete (deactivate) ─────────────────────────────────────────

    public void deleteUser(Long id) {
        User user = findOrThrow(id);
        user.setActive(false);
        userRepository.save(user);
    }

    // ─── Get Users by Role ────────────────────────────────────────────────

    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(this::toResponse)
                .toList();
    }
}
