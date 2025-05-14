package com.example.backend.controller;

import com.example.backend.entity.enums.ReturnCarStatus;
import com.example.backend.service.ContractService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/employee-contract-management")
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class EmployeeContractController {
    ContractService contractService;

    @GetMapping("/get-pending-contracts")
    public ResponseEntity<?> getPendingContracts(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token is missing or invalid"));
        }
        try {
            return ResponseEntity.ok(contractService.getEmployeePendingContracts(token));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/reject-assignment/{contractId}")
    public ResponseEntity<?> rejectAssignmentContract(@PathVariable Long contractId, HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token is missing or invalid"));
        }
        try {
            String result = contractService.rejectAssignContract(contractId, token);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/confirm-assignment/{contractId}")
    public ResponseEntity<?> confirmAssignmentContract(@PathVariable Long contractId, HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token is missing or invalid"));
        }
        try {
            String result = contractService.confirmAssignContract(contractId, token);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/confirm-pickup/{contractId}")
    public ResponseEntity<?> confirmPickupContract(@PathVariable Long contractId, HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token is missing or invalid"));
        }
        try {
            String result = contractService.comfirmPickupContract(contractId, token);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/confirm-return/{contractId}")
    public ResponseEntity<?> confirmReturnContract(@PathVariable Long contractId, HttpServletRequest request,
                                                   @RequestBody ReturnCarStatus returnCarStatus) {
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token is missing or invalid"));
        }
        try {
            String result = contractService.confirmReturnContract(contractId, token, returnCarStatus);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    @GetMapping("/get-employee-contracts")
    public ResponseEntity<?> getEmployeeContracts(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token is missing or invalid"));
        }
        try {
            return ResponseEntity.ok(contractService.getEmployeeContracts(token));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }
}
