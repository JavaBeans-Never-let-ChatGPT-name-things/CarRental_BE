package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationEntity extends AbstractAuditing<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    String title;

    @Column(name = "message", nullable = false, columnDefinition = "LONGTEXT")
    String message;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    AccountEntity account_notification;

    @ManyToOne
    @JoinColumn(name = "contract_id", nullable = false)
    RentalContractEntity contract;
}
