package com.example.backend.controller;

import com.example.backend.service.AuthenticationService;
import com.example.backend.service.dto.AccountDTO;
import com.example.backend.service.dto.request.ForgotPasswordRequest;
import com.example.backend.service.dto.request.VerifyUserDTO;
import com.example.backend.service.dto.request.LoginRequest;
import com.example.backend.service.dto.request.RegisterRequest;
import com.example.backend.service.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final AccountMapper accountMapper;
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request)
    {
        try
        {
            return authenticationService.verify(request.username(), request.password());
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AccountDTO> register(@RequestBody RegisterRequest request)
    {
        return ResponseEntity.ok(accountMapper.toDto(authenticationService.register(request)));
    }

    @PostMapping("/verify")
    public String verify(@RequestBody VerifyUserDTO request)
    {
        try{
            authenticationService.verifyUser(request);
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
        return "Account verified";
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
