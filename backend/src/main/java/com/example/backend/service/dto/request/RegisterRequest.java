package com.example.backend.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class RegisterRequest {
    @NotBlank(message = "Username cannot be blank")
    String username;
    @NotBlank(message = "Password cannot be blank")
    String password;
    @Pattern(regexp = ".*@(gmail|gm)\\..*", message = "Invalid Email")
    String email;
    @NotBlank(message = "Display name cannot be blank")
    String displayName;
}
