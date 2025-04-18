package com.example.backend.service.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountDTO {
    String username;
    String email;
    String displayName;
    int gender;
    String address;
    String phoneNumber;
    String avatarUrl;
}
