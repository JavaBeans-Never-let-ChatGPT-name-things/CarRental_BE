package com.example.backend.service.dto.response;

import com.example.backend.entity.enums.ContractStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ContractSummaryDTO {
    ContractStatus contractStatus;
    int value;
}
