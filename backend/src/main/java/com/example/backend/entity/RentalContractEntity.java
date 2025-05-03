package com.example.backend.entity;

import com.example.backend.entity.enums.ContractStatus;
import com.example.backend.entity.enums.PaymentStatus;
import com.example.backend.entity.enums.ReturnCarStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "rental_contracts")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalContractEntity extends AbstractAuditing<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    CarEntity car;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    AccountEntity account;

    @Column(name = "start_date", nullable = false)
    LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    LocalDate endDate;

    @Column(name = "deposit", nullable = false)
    float deposit;

    @Column(name = "payment_method", nullable = false)
    String paymentMethod;

    @Column(name = "payment_status", nullable = false)
    @Enumerated(EnumType.STRING)
    PaymentStatus paymentStatus;

    @Column(name = "total_price", nullable = false)
    float totalPrice;

    @Column(name = "return_car_status")
    @Enumerated(EnumType.STRING)
    ReturnCarStatus returnCarStatus;

    @Column(name = "return_date")
    LocalDate returnDate;

    @Column(name = "penalty_fee")
    float penaltyFee;

    @Column(name = "last_retry_at")
    Instant lastRetryAt;

    @Column(name = "retry_count_left")
    int retryCountLeft = 3;

    @Column(name = "contract_status")
    @Enumerated(EnumType.STRING)
    ContractStatus contractStatus;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "review_id")
    ReviewEntity review;
}
