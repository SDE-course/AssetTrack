package com.assettrack.usermanagement.dto;

import com.assettrack.usermanagement.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// ─── Request DTOs ───────────────────────────────────────────────

public class UserDTOs {

    /** Used by Admin to create a user manually (not via signup) */
    @Data
    public static class CreateUserRequest {
        @NotBlank
        private String fullName;

        @Email
        @NotBlank
        private String email;

        @NotBlank
        private String password;

        @NotNull
        private Role role;
    }

    /** Used by Admin to update user info */
    @Data
    public static class UpdateUserRequest {
        private String fullName;
        private String email;
    }

    /** Used by Admin to change a user's role */
    @Data
    public static class ChangeRoleRequest {
        @NotNull
        private Role role;
    }

    // ─── Response DTOs ───────────────────────────────────────────────

    /** Returned in lists and detail views — no password */
    @Data
    public static class UserResponse {
        private Long id;
        private String fullName;
        private String email;
        private Role role;
        private boolean active;
    }
}
