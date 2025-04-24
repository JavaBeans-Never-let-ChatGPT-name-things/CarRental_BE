package com.example.backend.service.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PaymentRequestDTO {
    int orderCode;
    int amount;
    String description;
    List<Item> items;
    String cancelUrl;
    String returnUrl;
    Long expiredAt;
    String signature;
}

