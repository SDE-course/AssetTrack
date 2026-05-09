package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.UserDTO;
import com.assettrack.allocation.entity.Role;
import com.assettrack.allocation.entity.User;
import com.assettrack.allocation.exception.BadRequestException;
import com.assettrack.allocation.exception.ResourceNotFoundException;
import com.assettrack.allocation.repository.AllocationRepository;
import com.assettrack.allocation.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    AllocationRepository allocationRepository;

    @InjectMocks
    UserService userService;

    @Test
    @DisplayName("Delete user with active allocations -> bad request")
    void deleteUser_withActiveAllocations_throws() {
        User user = User.builder()
                .id(5L)
                .name("Developer")
                .email("dev@assettrack.com")
                .role(Role.DEVELOPER)
                .build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(allocationRepository.existsByAssignedToIdAndActiveTrue(5L)).thenReturn(true);

        assertThatThrownBy(() -> userService.deleteUser(5L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("active asset allocations");
    }

    @Test
    @DisplayName("Delete user without active allocations -> success")
    void deleteUser_withoutActiveAllocations_success() {
        User user = User.builder()
                .id(6L)
                .name("QA")
                .email("qa@assettrack.com")
                .role(Role.DEVELOPER)
                .build();

        when(userRepository.findById(6L)).thenReturn(Optional.of(user));
        when(allocationRepository.existsByAssignedToIdAndActiveTrue(6L)).thenReturn(false);

        userService.deleteUser(6L);

        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("Get missing user -> not found")
    void getUserById_missing_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Create user with duplicate email -> bad request")
    void createUser_duplicateEmail_throws() {
        UserDTO.CreateUserRequest req = new UserDTO.CreateUserRequest();
        req.setName("Alice");
        req.setEmail("alice@assettrack.com");
        req.setRole(Role.DEVELOPER);

        when(userRepository.existsByEmail("alice@assettrack.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    @DisplayName("Create user returns mapped response")
    void createUser_success() {
        UserDTO.CreateUserRequest req = new UserDTO.CreateUserRequest();
        req.setName("Alice");
        req.setEmail("alice@assettrack.com");
        req.setRole(Role.DEVELOPER);

        User saved = User.builder()
                .id(7L)
                .name("Alice")
                .email("alice@assettrack.com")
                .role(Role.DEVELOPER)
                .build();

        when(userRepository.existsByEmail("alice@assettrack.com")).thenReturn(false);
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(saved);

        UserDTO.UserResponse response = userService.createUser(req);

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getName()).isEqualTo("Alice");
        assertThat(response.getEmail()).isEqualTo("alice@assettrack.com");
    }
}
