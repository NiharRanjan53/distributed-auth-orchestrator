package com.mro.orchestrator.service.impl;

import com.mro.orchestrator.dto.AuthResponseDTO;
import com.mro.orchestrator.dto.LoginRequestDTO;
import com.mro.orchestrator.dto.RegisterRequestDTO;
import com.mro.orchestrator.exception.UserAlreadyExistsException;
import com.mro.orchestrator.models.User;
import com.mro.orchestrator.repositories.UserRepository;
import com.mro.orchestrator.service.IAuthService;
import com.mro.orchestrator.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public AuthResponseDTO registerUser(RegisterRequestDTO request) {

        log.info("User registration initiated. username={}", request.getEmail());

        if (userRepository.existsByUsername(request.getUsername())) {

            log.warn("Registration failed. Username already exists. username={}", request.getUsername());

            throw new UserAlreadyExistsException(
                    "Username is already taken!"
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {

            log.warn("Registration failed. Email already exists.");

            throw new UserAlreadyExistsException(
                    "Email is already taken!"
            );
        }

        log.debug("Validation successful. Creating user entity.");

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(request.getRoles())
                .build();

        User savedUser = userRepository.save(user);

        log.info(
                "User registered successfully. userId={}, username={}",
                savedUser.getId(),
                savedUser.getUsername()
        );

        return AuthResponseDTO.builder()
                .message("User registered successfully")
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    @Override
    public AuthResponseDTO loginUser(LoginRequestDTO request) {

        log.info("Login attempt initiated. username={}", request.getUsername());

        // 1. Find the user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(()->{
                    log.warn("Login failed. User not found. username={}", request.getUsername());
                    return new UserAlreadyExistsException("Invalid Username or Password");
                });

        // 2. Verify the password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed. Invalid credentials. userId={}", user.getId());
            throw new UserAlreadyExistsException("Invalid Username or Password");
        }

        log.debug(
                "User authenticated successfully. userId={}",
                user.getId()
        );

        // 3. GENERATE THE TOKEN
        String token = jwtUtils.generateToken(user.getUsername());

        log.info(
                "Login successful. userId={}, username={}",
                user.getId(),
                user.getUsername()
        );

        // 4. Map to Response (We will add the JWT token here in the next step)
        return AuthResponseDTO.builder()
                .message("Login successful")
                .token(token)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }

}