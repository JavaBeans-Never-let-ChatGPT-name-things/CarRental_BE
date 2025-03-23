package com.example.backend.controller;

import com.example.backend.service.AuthenticationService;
import com.example.backend.service.dto.request.ForgotPasswordRequest;
import com.example.backend.service.dto.request.VerifyUserDTO;
import com.example.backend.service.dto.request.LoginRequest;
import com.example.backend.service.dto.request.RegisterRequest;
import com.example.backend.service.dto.response.TokenResponse;
import com.example.backend.service.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final AccountMapper accountMapper;
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
                    .token(null)
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
                    .token(null)
                    .role(null)
                    .build());
        }
    }

    @PostMapping("/resend")
    public String resend(@RequestBody String email)
    {
        try{
            authenticationService.resendVerificationCode(email);
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
        return "Verification code resent";
    }

    @PostMapping("/forgot")
    public String forgot(@RequestBody String email)
    {
        try{
            authenticationService.sendForgotPasswordEmail(email);
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
    public String resendForgot(@RequestBody String email)
    {
        try{
            authenticationService.resendForgotPasswordEmail(email);
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
        return "Forgot password email resent";
    }

}
