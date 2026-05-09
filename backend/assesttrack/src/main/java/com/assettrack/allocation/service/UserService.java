package com.assettrack.allocation.service;

import com.assettrack.allocation.entity.Role;
import com.assettrack.allocation.entity.User;
import com.assettrack.allocation.dto.UserDTO.*;
import com.assettrack.allocation.exception.BadRequestException;
import com.assettrack.allocation.exception.ResourceNotFoundException;
import com.assettrack.allocation.repository.AllocationRepository;
import com.assettrack.allocation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AllocationRepository allocationRepository;

    // ─── Helpers ────────────────────────────────────────────────────────

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private UserResponse toResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setEmail(user.getEmail());
        res.setRole(user.getRole());
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
            throw new BadRequestException("Email already in use: " + req.getEmail());
        }

        User user = User.builder()
            .name(req.getName())
                .email(req.getEmail())
                .role(req.getRole())
                .build();

        return toResponse(userRepository.save(user));
    }

    // ─── Update User Info ────────────────────────────────────────────────

    public UserResponse updateUser(Long id, UpdateUserRequest req) {
        User user = findOrThrow(id);

        if (req.getName() != null && !req.getName().isBlank()) {
            user.setName(req.getName());
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            if (!req.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(req.getEmail())) {
                throw new BadRequestException("Email already in use: " + req.getEmail());
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
        if (allocationRepository.existsByAssignedToIdAndActiveTrue(user.getId())) {
            throw new BadRequestException("Cannot delete user with active asset allocations");
        }
        userRepository.delete(user);
    }

    // ─── Get Users by Role ────────────────────────────────────────────────

    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(this::toResponse)
                .toList();
    }
}
