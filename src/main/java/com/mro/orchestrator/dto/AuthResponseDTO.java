package com.mro.orchestrator.dto;

import com.mro.orchestrator.enums.UserRole;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    private String message;
    private Long id;
    private String username;
    private String email;
    private Set<UserRole> roles;
    private LocalDate createdAt;
}