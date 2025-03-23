package com.example.backend.service.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class EmailRequest {
    @Pattern(regexp = ".*@(gmail|gm)\\..*", message = "Invalid Email")
    private String email;
}
