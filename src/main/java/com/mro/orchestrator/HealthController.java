package com.mro.orchestrator;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HealthController {
    @GetMapping("/health")
    public String checkHealth() {
        return "Distributed Auth Orchestrator Application Service is Up and Running!";
    }
}