package com.example.backend.service.dto;

import com.example.backend.entity.CarBrandEntity;
import com.example.backend.entity.enums.CarState;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CarDTO {
    String id;
    CarBrandEntity brand;
    float maxSpeed;
    float carRange;
    String carImageUrl;
    CarState state;
    int seatsNumber;
    float rentalPrice;
    String engineType;
    float rating;
}
