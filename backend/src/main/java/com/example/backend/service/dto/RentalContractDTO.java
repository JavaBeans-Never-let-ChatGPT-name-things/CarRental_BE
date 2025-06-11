package com.example.backend.service.dto;

import com.example.backend.entity.enums.ContractStatus;
import com.example.backend.entity.enums.PaymentStatus;
import com.example.backend.entity.enums.ReturnCarStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalContractDTO {
    Long id;
    String customerName;
    String carId;
    String carImageUrl;
    LocalDate startDate;
    LocalDate endDate;
    Instant contractDate;
    boolean pending;
    ContractStatus contractStatus;
    PaymentStatus paymentStatus;
    int retryCountLeft;
    ReturnCarStatus returnCarStatus;
    String employeeName;
    float deposit;
    float totalPrice;
}
