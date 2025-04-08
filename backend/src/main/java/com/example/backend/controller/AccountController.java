package com.example.backend.controller;

import com.example.backend.service.AccountService;
import com.example.backend.service.dto.CarDTO;
import com.example.backend.service.dto.request.CarPageRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;
    @GetMapping
    public ResponseEntity<?> getAccountInfoByToken(HttpServletRequest request){
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message","Token is missing or invalid"));
        }
        var account = accountService.findByUsername(token);
        if (account.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message","Account not found"));
        }
        return ResponseEntity.ok(account.get());
    }

    @PutMapping("/favourite/{carId}")
    public ResponseEntity<?> updateFavouriteCar(HttpServletRequest request, @PathVariable("carId") String carId){
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message","Token is missing or invalid"));
        }
        try {
            accountService.updateFavouriteCar(carId, token);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
        return ResponseEntity.ok(Map.of("message", "Favourite car updated successfully"));
    }
    public String extractToken (HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

    @GetMapping("/favourite/")
    public Page<CarDTO> getFavouriteCars(HttpServletRequest request, @RequestBody CarPageRequestDTO carPageRequestDTO) {
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            return null;
        }
        return accountService.getFavouriteCars(token, carPageRequestDTO);
    }
}
