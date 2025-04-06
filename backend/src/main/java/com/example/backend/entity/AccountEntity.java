package com.example.backend.entity;

import com.example.backend.entity.enums.AccountRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "accounts",
    indexes = {
        @Index(
            columnList = "username",
            unique = true
        )
    })
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity extends AbstractAuditing<Long>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "username", unique = true, nullable = false, updatable = false)
    String username;

    @Column(name = "password_hash", nullable = false)
    String passwordHash;

    @Column(name = "email", unique = true, nullable = false)
    String email;

    @Column(name = "display_name", nullable = false)
    String displayName;

    @Column(name = "address")
    String address;

    @Column(name = "phone_number")
    String phoneNumber;

    @Column(name = "avatar", columnDefinition = "TEXT")
    String avatarUrl;

    @Column(name = "status")
    boolean enabled;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    AccountRole accountRole;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "verification_expiration")
    private LocalDateTime verificationCodeExpiresAt;

    @OneToMany(mappedBy = "account",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    List<RentalContractEntity> rentalContracts = new ArrayList<>();

    public void addContract(RentalContractEntity contract) {
        rentalContracts.add(contract);
    }

    @OneToMany(mappedBy = "account_notification"
            ,cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    List<NotificationEntity> notifications = new ArrayList<>();

    public void addNotification(NotificationEntity notification) {
        notifications.add(notification);
    }

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable ( name = "favourite_cars",
                joinColumns = @JoinColumn(name = "account_id"),
                inverseJoinColumns = @JoinColumn(name = "car_id"))
    List<CarEntity> favouriteCars = new ArrayList<>();

    public void addFavouriteCar(CarEntity car) {
        favouriteCars.add(car);
    }
    public void removeFavouriteCar(CarEntity car) {
        favouriteCars.remove(car);
    }
}
