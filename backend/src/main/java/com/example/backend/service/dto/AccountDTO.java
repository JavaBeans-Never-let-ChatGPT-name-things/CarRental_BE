package com.example.backend.service.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountDTO {
    String username;
    String password;
    String email;
    String displayName;
}
