package com.example.backend.entity;

import com.example.backend.entity.enums.CarState;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "cars")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CarEntity extends AbstractAuditing<String> {
    @Id
    String id = UUID.randomUUID().toString();

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="brand_id", nullable = false)
    CarBrandEntity brand;

    @Column(name = "max_speed", nullable = false)
    float maxSpeed;

    @Column(name = "car_range", nullable = false)
    float carRange;

    @Column(name = "car_image_url", nullable = false)
    String carImageUrl;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    CarState state;

    @Column(name = "seats_num", nullable = false)
    int seatsNumber;

    @Column(name = "rental_price_per_day", nullable = false)
    float rentalPrice;

    @Column(name = "engine_type", nullable = false)
    String engineType;

    @Column(name = "rating")
    float rating;

    @Column(name = "reviews_num")
    int reviewsNum;

    @OneToMany(mappedBy = "car",
            cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH},
            fetch = FetchType.LAZY)
    List<RentalContractEntity> rentalContracts = new ArrayList<>();

    public void addContract(RentalContractEntity contract) {
        rentalContracts.add(contract);
    }

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}
    ,fetch = FetchType.LAZY)
    @JoinTable(
            name = "favourite_cars",
            joinColumns = @JoinColumn(name = "car_id"),
            inverseJoinColumns = @JoinColumn(name = "account_id")
    )
    List<AccountEntity> accounts = new ArrayList<>();

    public void addAccount(AccountEntity account) {
        accounts.add(account);
    }
    public void removeAccount(AccountEntity account) {
        accounts.remove(account);
    }
}
