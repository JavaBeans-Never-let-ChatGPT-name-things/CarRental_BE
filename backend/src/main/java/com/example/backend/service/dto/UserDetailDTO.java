package com.example.backend.service.dto;

import com.example.backend.entity.enums.AccountRole;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserDetailDTO {
    String avatarUrl;
    String displayName;
    String username;
    AccountRole role;
    String email;
    String phoneNumber;
    String address;
    float totalPenalty;
    int gender;
    List<RentalContractDTO> rentalContracts;
}
