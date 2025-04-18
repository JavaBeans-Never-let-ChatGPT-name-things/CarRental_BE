package com.example.backend.service.dto.request;

import com.example.backend.entity.enums.PaymentStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractRequestDTO {
    LocalDate startDate;
    LocalDate endDate;
    float deposit;
    String paymentMethod;
    PaymentStatus paymentStatus;
    float totalPrice;
}
