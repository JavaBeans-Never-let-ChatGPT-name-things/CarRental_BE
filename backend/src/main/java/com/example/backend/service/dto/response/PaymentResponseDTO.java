package com.example.backend.service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponseDTO {
    private String checkoutUrl;
}
