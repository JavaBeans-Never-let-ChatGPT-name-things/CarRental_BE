package com.example.backend.service;

import com.example.backend.entity.RentalContractEntity;
import com.example.backend.entity.enums.CarState;
import com.example.backend.entity.enums.PaymentStatus;
import com.example.backend.repository.CarRepository;
import com.example.backend.repository.ContractRepository;
import com.example.backend.service.dto.request.Item;
import com.example.backend.service.dto.request.PaymentRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.example.backend.service.AccountServiceImpl.pendingPayments;

@Service
@RequiredArgsConstructor
public class PayOsService {
    @Value("${pay-os.client-id}")
    private String clientId;
    @Value("${pay-os.api-key}")
    private String apiKey;
    @Value("${pay-os.checksum-key}")
    private String checksumKey;
    final String CANCEL_URL = "http://cancelpayment.com";
    final String RETURN_URL = "http://successpayment.com";
    private static final int PAYMENT_TIMEOUT =  300000;

    private final CarRepository carRepository;
    private final ContractRepository contractRepository;

    public String getCheckoutUrl(Item item)
    {
        int orderId = generateOrderId();
        long expiredAt = (System.currentTimeMillis() / 1000) + (PAYMENT_TIMEOUT / 1000);
        RestTemplate restTemplate = new RestTemplate();
        PaymentRequestDTO paymentRequestDTO = PaymentRequestDTO.builder()
                .orderCode(orderId)
                .amount(2000)
                .description("Payment For Rental Order")
                .items(List.of(item))
                .cancelUrl(CANCEL_URL)
                .returnUrl(RETURN_URL)
                .expiredAt(expiredAt)
                .build();

        String signature = createPayOsSignature(paymentRequestDTO.getAmount(), CANCEL_URL, paymentRequestDTO.getDescription(), String.valueOf(paymentRequestDTO.getOrderCode()), RETURN_URL);
        paymentRequestDTO.setSignature(signature);
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-client-id", clientId);
        headers.set("x-api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PaymentRequestDTO> entity = new HttpEntity<>(paymentRequestDTO, headers);

        // Schedule a task to check for payment failure after 5 minutes

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api-merchant.payos.vn/v2/payment-requests/",
                HttpMethod.POST,
                entity,
                Map.class
        );

        return ((Map<String, Object>) response.getBody().get("data")).get("checkoutUrl").toString();

    }

    private String createPayOsSignature(int amount, String cancelUrl, String description, String orderCode, String returnUrl){
        final String HMAC_SHA256 = "HmacSHA256";
        try {
            String data = String.format(Locale.getDefault(), "amount=%d&cancelUrl=%s&description=%s&orderCode=%s&returnUrl=%s",
                    amount, cancelUrl, description, orderCode, returnUrl);

            Mac sha256_HMAC = Mac.getInstance(HMAC_SHA256);
            sha256_HMAC.init(new SecretKeySpec(checksumKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));

            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

            Formatter formatter = new Formatter();
            for (byte b : hash) {
                formatter.format("%02x", b);
            }

            return formatter.toString();
        }
        catch (Exception e) {
            return null;
        }
    }
    private static int generateOrderId() {
        return (int) (Math.random() * 1000000);
    }

    public String paymentSuccess(Long orderId) {
        RentalContractEntity contract = contractRepository.findById(orderId).orElseThrow(
                () -> new RuntimeException("Rental contract not found")
        );
        pendingPayments.remove(orderId);
        contract.setPaymentStatus(PaymentStatus.SUCCESS);
        contractRepository.save(contract);
        return "Payment Success";
    }
    public String paymentFailed(String carId) {
        var car = carRepository.findById(carId).orElseThrow(
                () -> new RuntimeException("Car not found")
        );
        car.setState(CarState.AVAILABLE);
        carRepository.save(car);
        return "Payment Failed";
    }
}
