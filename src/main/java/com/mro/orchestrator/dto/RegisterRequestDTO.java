package com.mro.orchestrator.dto;

import com.mro.orchestrator.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    @NotBlank(message = "User Name Required")
    private String username;

    @NotBlank(message = "User Email Required")
    private String email;

    @NotBlank(message = "Password Required")
    private String password;

    // FIX: Change @NotBlank to @NotEmpty for Collections
    @NotEmpty(message = "At least one role is required")
    private Set<UserRole> roles;
}