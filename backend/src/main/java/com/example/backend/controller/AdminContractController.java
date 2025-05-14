package com.example.backend.controller;

import com.example.backend.service.ContractService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin-contract-management")
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AdminContractController {
    ContractService contractService;
    @PostMapping("/assignContract/{contractId}/{employeeName}")
    public ResponseEntity<?> assignContract(@PathVariable Long contractId, @PathVariable String employeeName) {
        try {
            String result = contractService.assignContract(contractId, employeeName);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/pending-contracts")
    public ResponseEntity<?> getPendingContracts() {
        try {
            return ResponseEntity.ok(contractService.getPendingContracts());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
