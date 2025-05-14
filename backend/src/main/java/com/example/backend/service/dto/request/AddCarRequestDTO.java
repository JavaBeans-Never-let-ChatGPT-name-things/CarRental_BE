package com.example.backend.service.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AddCarRequestDTO {
    String id;
    String brandName;
    float maxSpeed;
    float carRange;
    MultipartFile carImage;
    int seatsNumber;
    float rentalPrice;
    String engineType;
    String gearType;
    String drive;
}
