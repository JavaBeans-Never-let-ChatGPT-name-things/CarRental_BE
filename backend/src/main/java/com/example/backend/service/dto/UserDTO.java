package com.example.backend.service.dto;

import com.example.backend.entity.enums.AccountRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
    String displayName;
    String phoneNumber;
    String email;
    AccountRole role;
    String avatarUrl;
    int gender;
    Boolean enabled;
    Long countractCount;
}
