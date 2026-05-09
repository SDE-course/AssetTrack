package com.assettrack.allocation.dto;

import com.assettrack.allocation.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// ─── Request DTOs ───────────────────────────────────────────────

public class UserDTO {

    /** Used by Admin to create a user manually (not via signup) */
    @Data
    public static class CreateUserRequest {
        @NotBlank
        private String name;

        @Email
        @NotBlank
        private String email;

        @NotNull
        private Role role;
    }

    /** Used by Admin to update user info */
    @Data
    public static class UpdateUserRequest {
        private String name;
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
        private String name;
        private String email;
        private Role role;
    }
}
