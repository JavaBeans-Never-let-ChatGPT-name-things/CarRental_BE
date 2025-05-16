package com.example.backend.service.dto.response;

import com.example.backend.entity.enums.ReturnCarStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ReturnedStatusDTO {
    ReturnCarStatus returnCarStatus;
    int value;
}
