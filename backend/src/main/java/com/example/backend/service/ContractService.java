package com.example.backend.service;

import com.example.backend.entity.enums.ReturnCarStatus;

public interface ContractService {
    String retryContractSuccess(Long contractId, String token);
    String retryContract(Long contractId, String token);
    String comfirmPickupContract(Long contractId, String token);
    String reportLostContract(Long contractId);
    String extendContract(Long contractId, String token, int extraDays);
    String confirmReturnContract(Long contractId, String token, ReturnCarStatus returnCarStatus);
}
