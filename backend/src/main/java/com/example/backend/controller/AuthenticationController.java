package com.example.backend.controller;

import com.example.backend.service.AccountService;
import com.example.backend.service.dto.request.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {
    private final AccountService accountService;
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request)
    {
        return accountService.verify(request.username(), request.password());
    }
}
