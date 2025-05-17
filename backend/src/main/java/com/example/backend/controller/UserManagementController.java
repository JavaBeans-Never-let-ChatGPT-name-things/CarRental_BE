package com.example.backend.controller;

import com.example.backend.service.AccountService;
import com.example.backend.service.dto.UserDTO;
import com.example.backend.service.dto.request.ReportRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user-management")
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UserManagementController {
    AccountService accountService;
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getUsers(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "none") String sort,
            @RequestParam(defaultValue = "Total User") String status
    ) {
        return ResponseEntity.ok().body(accountService.searchAndSortUsers(query, sort, status));
    }

    @PutMapping("/users/promote/{displayName}")
    public ResponseEntity<?> promoteUser(@PathVariable String displayName) {
        try{
            accountService.upgradeUserRole(displayName);
            return ResponseEntity.ok().body(Map.of("message", "Successfully promoted user"));
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    @PutMapping("/users/demote/{displayName}")
    public ResponseEntity<?> demoteUser(@PathVariable String displayName) {
        try{
            accountService.downgradeUserRole(displayName);
            return ResponseEntity.ok().body(Map.of("message", "Successfully demoted user"));
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    @GetMapping("/users/{displayName}")
    public ResponseEntity<?> getUserDetail(@PathVariable String displayName) {
        try{
            return ResponseEntity.ok().body(accountService.getUserDetail(displayName));
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/employees/available/{contractId}")
    public ResponseEntity<?> getAvailableEmployees(@PathVariable Long contractId) {
        try{
            return ResponseEntity.ok().body(accountService.getAvailableEmployees(contractId));
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/top-3-best-user")
    public ResponseEntity<?> getTop3BestUser(){
        return ResponseEntity.ok(accountService.top3BestUser());
    }
    @GetMapping("/top-3-worst-user")
    public ResponseEntity<?> getTop3WorstUser() {
        return ResponseEntity.ok(accountService.top3WorstUser());
    }

    @PostMapping("/top-3-best-user-from-date-to-date")
    public ResponseEntity<?> getTop3BestUserFromDateToDate(@RequestBody ReportRequestDTO requestDTO) {
        return ResponseEntity.ok(accountService.top3BestUserFromDateToDate(requestDTO.getStartDate(), requestDTO.getEndDate()));
    }

    @PostMapping("/top-3-worst-user-from-date-to-date")
    public ResponseEntity<?> getTop3WorstUserFromDateToDate(@RequestBody ReportRequestDTO requestDTO) {
        return ResponseEntity.ok(accountService.top3WorstUserFromDateToDate(requestDTO.getStartDate(), requestDTO.getEndDate()));
    }
}
