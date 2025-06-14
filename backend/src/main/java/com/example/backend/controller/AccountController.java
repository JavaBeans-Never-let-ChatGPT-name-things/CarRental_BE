package com.example.backend.controller;

import com.example.backend.entity.ReviewEntity;
import com.example.backend.service.AccountService;
import com.example.backend.service.ContractService;
import com.example.backend.service.dto.CarDTO;
import com.example.backend.service.dto.RentalContractDTO;
import com.example.backend.service.dto.request.ContractRequestDTO;
import com.example.backend.service.dto.request.UpdateUserRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;
    private final ContractService contractService;
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
    public List<CarDTO> getFavouriteCars(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            return null;
        }
        return accountService.getFavouriteCars(token);
    }

    @PostMapping(path =  "/update/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(HttpServletRequest request, @ModelAttribute UpdateUserRequestDTO updateProfile)
    {
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message","Token is missing or invalid"));
        }
        try {
            accountService.updateProfile(updateProfile, token);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }

    @PostMapping ("/rentCar/{carId}")
    public ResponseEntity<?> rentCar(HttpServletRequest request, @RequestBody ContractRequestDTO contract, @PathVariable("carId") String carId)
    {
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message","Token is missing or invalid"));
        }
        try {
            return ResponseEntity.ok().body(accountService.rentCar(contract, token, carId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/rentalContracts")
    public List<RentalContractDTO> getRentalContracts(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            return null;
        }
        return accountService.getRentalContracts(token);
    }

    @PostMapping("/rentalContracts/review/{contractId}")
    public ResponseEntity<?> reviewRentalContract(@PathVariable("contractId") Long contractId,
                                                  @RequestBody ReviewEntity review) {
        try {
            accountService.reviewRentalContract(contractId, review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
        return ResponseEntity.ok(Map.of("message", "Review added successfully"));
    }

    @PostMapping("/rentalContracts/retry/{contractId}")
    public ResponseEntity<?> retryRentalContract(@PathVariable("contractId") Long contractId, HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message","Token is missing or invalid"));
        }
        try {
            return ResponseEntity.ok().body(Map.of("message", contractService.retryContract(contractId, token)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/rentalContracts/retrySuccess/{contractId}")
    public ResponseEntity<?> retrySuccess(@PathVariable("contractId") Long contractId, HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.ok().body(Map.of("message","Token is missing or invalid"));
        }
        try {
            return ResponseEntity.ok().body(Map.of("message", contractService.retryContractSuccess(contractId, token)));
        } catch (RuntimeException e) {
            return ResponseEntity.ok().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/rentalContracts/confirmLost/{contractId}")
    public ResponseEntity<?> reportLost(@PathVariable("contractId") Long contractId, HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message","Token is missing or invalid"));
        }
        try {
            return ResponseEntity.ok().body(Map.of("message", contractService.reportLostContract(contractId, token)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    @PostMapping("/rentalContracts/extend/{contractId}/{extraDays}")
    public ResponseEntity<?> extendRentalContract(@PathVariable("contractId") Long contractId,
                                                  @PathVariable("extraDays") int extraDays,
                                                  HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message","Token is missing or invalid"));
        }
        try {
            return ResponseEntity.ok().body(Map.of("message", contractService.extendContract(contractId, token, extraDays)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    @GetMapping("/isQualified")
    public ResponseEntity<?> isQualifiedToRentCar(HttpServletRequest request){
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message","Token is missing or invalid"));
        }
        try{
            accountService.verifyAccount(token);
            return ResponseEntity.ok().body(true);
        }
        catch (Exception e){
            return ResponseEntity.ok().body(false);
        }
    }
}
