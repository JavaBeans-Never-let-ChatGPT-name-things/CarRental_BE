package com.example.backend.controller;

import com.example.backend.service.ContractService;
import com.example.backend.service.dto.request.ReportRequestDTO;
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

    @GetMapping("/total-revenue/{year}")
    public ResponseEntity<?> getTotalRevenueByMonth(@PathVariable int year) {
        try {
            return ResponseEntity.ok(contractService.totalRevenueByMonth(year));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/total-penalty/{year}")
    public ResponseEntity<?> getTotalPenaltyByMonth(@PathVariable int year) {
        try {
            return ResponseEntity.ok(contractService.totalPenaltyByMonth(year));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/total-revenue-from-date-to-date")
    public ResponseEntity<?> getTotalRevenueFromDateToDate(@RequestBody ReportRequestDTO reportRequest) {
        try {
            return ResponseEntity.ok(contractService.totalRevenueFromDateToDate(reportRequest.getStartDate(), reportRequest.getEndDate()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/total-penalty-from-date-to-date")
    public ResponseEntity<?> getTotalPenaltyFromDateToDate(@RequestBody ReportRequestDTO reportRequest) {
        try {
            return ResponseEntity.ok(contractService.totalPenaltyFromDateToDate(reportRequest.getStartDate(), reportRequest.getEndDate()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/total-revenue")
    public ResponseEntity<?> getTotalRevenue() {
        try {
            return ResponseEntity.ok(contractService.totalRevenue());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/total-penalty")
    public ResponseEntity<?> getTotalPenalty() {
        try {
            return ResponseEntity.ok(contractService.totalPenalty());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/contract-summary-from-date-to-date")
    public ResponseEntity<?> getContractSummaryFromDateToDate(@RequestBody ReportRequestDTO reportRequest) {
        try {
            return ResponseEntity.ok(contractService.getContractSummaryFromDateToDate(reportRequest.getStartDate(), reportRequest.getEndDate()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/contract-summary")
    public ResponseEntity<?> getContractSummary() {
        try {
            return ResponseEntity.ok(contractService.getContractSummary());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/returned-status")
    public ResponseEntity<?> getReturnedStatus() {
        try {
            return ResponseEntity.ok(contractService.getReturnedStatus());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    @PostMapping("/returned-status-from-date-to-date")
    public ResponseEntity<?> getReturnedStatusFromDateToDate(@RequestBody ReportRequestDTO reportRequest) {
        try {
            return ResponseEntity.ok(contractService.getReturnedStatusFromDateToDate(reportRequest.getStartDate(), reportRequest.getEndDate()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
