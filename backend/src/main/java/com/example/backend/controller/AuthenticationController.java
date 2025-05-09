package com.example.backend.controller;

import com.example.backend.service.AuthenticationService;
import com.example.backend.service.dto.request.*;
import com.example.backend.service.dto.response.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request)
    {
        try
        {
            return ResponseEntity.ok().body(authenticationService.verify(request.username(), request.password()));
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(TokenResponse.builder()
                    .accessToken(null)
                    .refreshToken(null)
                    .role(null)
                    .build());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request)
    {
        try
        {
            ResponseEntity.ok(authenticationService.register(request));
            return ResponseEntity.ok().body(Map.of("message", "User registered successfully"));
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to register due to"  + e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<TokenResponse> verify(@RequestBody VerifyUserDTO request)
    {
        try{
            return ResponseEntity.ok().body(authenticationService.verifyUser(request));
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(TokenResponse.builder()
                    .accessToken(null)
                    .refreshToken(null)
                    .role(null)
                    .build());
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resend(@Valid @RequestBody EmailRequest email)
    {
        try{
            authenticationService.resendVerificationCode(email.getEmail());
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));
        }
        return ResponseEntity.ok(Map.of("message", "Verification code resent"));
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgot(@Valid @RequestBody EmailRequest email)
    {
        try{
            authenticationService.sendForgotPasswordEmail(email.getEmail());
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));
        }
        return ResponseEntity.ok(Map.of("message","Forgot password email sent"));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ForgotPasswordRequest request)
    {
        try{
            authenticationService.resetPassword(request);
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));
        }
        return ResponseEntity.ok(Map.of("message", "Password Reset"));
    }

    @PostMapping("/resendForgot")
    public ResponseEntity<?> resendForgot(@Valid @RequestBody EmailRequest email)
    {
        try{
            authenticationService.resendForgotPasswordEmail(email.getEmail());
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));
        }
        return ResponseEntity.ok(Map.of("message", "Forgot password email resent"));

    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request)
    {
        try
        {
            String token = request.getHeader("Authorization").substring(7);
            authenticationService.logout(token);
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        }
        catch (Exception e)
        {
            return ResponseEntity.ok(Map.of("message", "Invalid Token"));
        }
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request)
    {
        try{
            String refreshToken = request.getHeader("Authorization").substring(7);
            return ResponseEntity.ok(authenticationService.refreshAccessToken(refreshToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(TokenResponse.builder()
                    .accessToken(null)
                    .refreshToken(null)
                    .role(null)
                    .build());
        }
    }
}
