package com.assettrack.usermanagement.controller;

import com.assettrack.usermanagement.domain.Role;
import com.assettrack.usermanagement.dto.UserDTOs.*;
import com.assettrack.usermanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/users  — ADMIN, MANAGER
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // GET /api/users/{id}  — ADMIN, MANAGER
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // GET /api/users?role=DEVELOPER  — ADMIN, MANAGER
    @GetMapping("/by-role")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@RequestParam Role role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    // POST /api/users  — ADMIN only
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.ok(userService.createUser(req));
    }

    // PUT /api/users/{id}  — ADMIN only
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(userService.updateUser(id, req));
    }

    // PATCH /api/users/{id}/role  — ADMIN only
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> changeRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeRoleRequest req) {
        return ResponseEntity.ok(userService.changeRole(id, req));
    }

    // DELETE /api/users/{id}  — ADMIN only (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
