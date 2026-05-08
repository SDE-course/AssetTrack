package com.assettrack.usermanagement.service;

import com.assettrack.allocation.entity.User;
import com.assettrack.allocation.repository.UserRepository;
import com.assettrack.usermanagement.domain.Role;
import com.assettrack.usermanagement.dto.UserDTOs.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public UserResponse createUser(CreateUserRequest req) {

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already in use");
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

    public UserResponse updateUser(Long id, UpdateUserRequest req) {
        User user = findOrThrow(id);

        if (req.getFullName() != null) {
            user.setFullName(req.getFullName());
        }

        if (req.getEmail() != null &&
            !req.getEmail().equals(user.getEmail()) &&
            userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        user.setEmail(req.getEmail());

        return toResponse(userRepository.save(user));
    }

    public UserResponse changeRole(Long id, ChangeRoleRequest req) {
        User user = findOrThrow(id);
        user.setRole(req.getRole());
        return toResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        User user = findOrThrow(id);
        user.setActive(false);
        userRepository.save(user);
    }

    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(this::toResponse)
                .toList();
    }
}