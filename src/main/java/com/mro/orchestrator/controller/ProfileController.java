package com.mro.orchestrator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {
        // Get the authenticated user's details from the Security Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        return ResponseEntity.ok(Map.of(
                "username", currentPrincipalName,
                "message", "Welcome to your protected profile!",
                "status", "Authenticated via JWT"
        ));
    }
}