package com.example.backend.entity;

import com.example.backend.entity.enums.AccountRole;
import com.example.backend.entity.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    @Column(name = "avatar", columnDefinition = "text")
    String avatarUrl;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    AccountRole accountRole = AccountRole.USER;

    @OneToMany(mappedBy = "account",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    List<RentalContractEntity> rentalContracts = new ArrayList<>();

    public void addContract(RentalContractEntity contract) {
        rentalContracts.add(contract);
    }
}
