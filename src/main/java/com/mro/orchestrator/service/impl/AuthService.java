package com.mro.orchestrator.service.impl;

import com.mro.orchestrator.dto.AuthResponseDTO;
import com.mro.orchestrator.dto.RegisterRequestDTO;
import com.mro.orchestrator.exception.UserAlreadyExistsException;
import com.mro.orchestrator.models.User;
import com.mro.orchestrator.repositories.UserRepository;
import com.mro.orchestrator.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponseDTO registerUser(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken!");
        }
        // FIX: Use existsByEmail here
        else if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already taken!");
        }

        // 2. Map Request -> Entity
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))     // ENCODE the password
                .roles(request.getRoles())
                .build();

        // 3. Save to DB
        User savedUser = userRepository.save(user);

        // 4. Map Entity -> Response DTO
        return AuthResponseDTO.builder()
                .message("User registered successfully")
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles())
                .createdAt(savedUser.getCreatedAt())
                .build();

    }
}