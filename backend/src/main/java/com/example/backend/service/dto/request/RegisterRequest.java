package com.example.backend.service.dto.request;

import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class RegisterRequest {
    String username;
    String password;
    String email;
    String displayName;
}
