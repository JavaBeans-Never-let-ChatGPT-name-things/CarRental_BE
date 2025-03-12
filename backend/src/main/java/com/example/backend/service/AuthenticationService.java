package com.example.backend.service;

import com.example.backend.entity.AccountEntity;
import com.example.backend.service.dto.VerifyUserDTO;
import com.example.backend.service.dto.request.RegisterRequest;

public interface AuthenticationService {
    String verify(String username, String password);
    AccountEntity register(RegisterRequest accountDTO);
    void verifyUser(VerifyUserDTO input);
    void resendVerificationCode(String email);
}
