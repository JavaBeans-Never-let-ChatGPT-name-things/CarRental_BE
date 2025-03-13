package com.example.backend.service;

import com.example.backend.entity.AccountEntity;
import com.example.backend.service.dto.request.ForgotPasswordRequest;
import com.example.backend.service.dto.request.VerifyUserDTO;
import com.example.backend.service.dto.request.RegisterRequest;

public interface AuthenticationService {
    String verify(String username, String password);
    AccountEntity register(RegisterRequest accountDTO);
    void verifyUser(VerifyUserDTO input);
    void resendVerificationCode(String email);
    void sendForgotPasswordEmail(String email);
    void resetPassword(ForgotPasswordRequest request);
    void resendForgotPasswordEmail(String email);
}
