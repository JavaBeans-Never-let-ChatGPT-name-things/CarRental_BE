package com.example.backend.controller;

import com.example.backend.service.PayOsService;
import com.example.backend.service.dto.request.Item;
import com.example.backend.service.dto.response.PaymentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PayOsService payOsService;
    @PostMapping("/create")
    public PaymentResponseDTO createPayment(@RequestBody Item item) {
        return PaymentResponseDTO
                .builder()
                .checkoutUrl(payOsService.getCheckoutUrl(item))
                .build();
    }
    @PostMapping("/success/{orderId}")
    public ResponseEntity<?> paymentSuccess(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(Map.of("message", payOsService.paymentSuccess(orderId)));
    }
    @PostMapping("/failed/{carId}")
    public ResponseEntity<?> paymentFailed(@PathVariable("carId") String carId) {
        return ResponseEntity.ok(Map.of("message", payOsService.paymentFailed(carId)));
    }
}
