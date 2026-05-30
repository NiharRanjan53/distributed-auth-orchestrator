package com.mro.orchestrator.controller;

import com.mro.orchestrator.dto.AuthResponseDTO;
import com.mro.orchestrator.dto.LoginRequestDTO;
import com.mro.orchestrator.dto.RegisterRequestDTO;
import com.mro.orchestrator.service.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    // Logger log = LoggerFactory.getLogger(AuthController.class);
    private final IAuthService authService;

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED) // Tells Spring the default success is 201
    @Operation(summary = "Register a new user", description = "Creates a new user and returns their profile details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
    })
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {

        log.info("Registration request received for email={}", request.getEmail());

        AuthResponseDTO response = authService.registerUser(request);

        log.info("User registered successfully. userId={}", response.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {

        log.info("Login request received");

        AuthResponseDTO response = authService.loginUser(request);

        log.info("Login successful");

        return ResponseEntity.ok(response);
    }


}
