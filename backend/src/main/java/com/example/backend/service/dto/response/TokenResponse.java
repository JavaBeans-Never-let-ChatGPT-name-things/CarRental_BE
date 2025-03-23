package com.example.backend.service.dto.response;


import com.example.backend.entity.enums.AccountRole;
import lombok.*;

@Builder
public record TokenResponse(String accessToken, String refreshToken, AccountRole role) {
}
