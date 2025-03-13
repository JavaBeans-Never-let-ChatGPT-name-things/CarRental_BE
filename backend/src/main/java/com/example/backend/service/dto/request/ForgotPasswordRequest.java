package com.example.backend.service.dto.request;

public record ForgotPasswordRequest(String email, String verificationCode, String newPassword) {
}
