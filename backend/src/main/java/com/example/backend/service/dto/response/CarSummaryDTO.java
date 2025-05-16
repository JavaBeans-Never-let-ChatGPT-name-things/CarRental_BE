package com.example.backend.service.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CarSummaryDTO {
    String id;
    String imageUrl;
    Long rentalCount;
    double rating;
}
