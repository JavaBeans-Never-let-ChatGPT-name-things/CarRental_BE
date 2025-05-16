package com.example.backend.controller;

import com.example.backend.service.AccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AdminAccountController {
    AccountService accountService;
    @GetMapping("/top-3-best-user")
    public ResponseEntity<?> getTop3BestUser(){
        return ResponseEntity.ok(accountService.top3BestUser());
    }
    @GetMapping("/top-3-worst-user")
    public ResponseEntity<?> getTop3WorstUser() {
        return ResponseEntity.ok(accountService.top3WordUser());
    }
}
