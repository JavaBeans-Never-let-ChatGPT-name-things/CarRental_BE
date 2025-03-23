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
            return ResponseEntity.ok(authenticationService.register(request));
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body("Failed to register due to " + e.getMessage());
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
    public String resend(@Valid @RequestBody EmailRequest email)
    {
        try{
            authenticationService.resendVerificationCode(email.getEmail());
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
        return "Verification code resent";
    }

    @PostMapping("/forgot")
    public String forgot(@Valid @RequestBody EmailRequest email)
    {
        try{
            authenticationService.sendForgotPasswordEmail(email.getEmail());
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
        return "Forgot password email sent";
    }

    @PostMapping("/reset")
    public String resetPassword(@RequestBody ForgotPasswordRequest request)
    {
        try{
            authenticationService.resetPassword(request);
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
        return "Password reset";
    }

    @PostMapping("/resendForgot")
    public String resendForgot(@Valid @RequestBody EmailRequest email)
    {
        try{
            authenticationService.resendForgotPasswordEmail(email.getEmail());
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
        return "Forgot password email resent";
    }
    @GetMapping("/logout")
    public String logout(HttpServletRequest request)
    {
        try
        {
            String token = request.getHeader("Authorization").substring(7);
            authenticationService.logout(token);
            return "User has logged out";
        }
        catch (Exception e)
        {
            return "Invalid token";
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
