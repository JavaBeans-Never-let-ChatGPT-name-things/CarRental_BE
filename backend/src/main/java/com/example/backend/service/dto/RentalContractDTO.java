package com.example.backend.service.dto;

import com.example.backend.entity.enums.ContractStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalContractDTO {
    Long id;
    String carId;
    String carImageUrl;
    LocalDate startDate;
    LocalDate endDate;
    Instant contractDate;
    ContractStatus contractStatus;
    float deposit;
    float totalPrice;
}
