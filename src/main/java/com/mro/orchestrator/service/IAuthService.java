package com.mro.orchestrator.service;

import com.mro.orchestrator.dto.AuthResponseDTO;
import com.mro.orchestrator.dto.RegisterRequestDTO;

public interface IAuthService {
    AuthResponseDTO registerUser(RegisterRequestDTO request);
}