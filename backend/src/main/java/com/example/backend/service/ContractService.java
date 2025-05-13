package com.example.backend.service;

import com.example.backend.entity.enums.ReturnCarStatus;
import com.example.backend.service.dto.RentalContractDTO;

import java.util.List;

public interface ContractService {
    String retryContractSuccess(Long contractId, String token);
    String retryContract(Long contractId, String token);
    String assignContract(Long contractId, String employeeName);
    String reportLostContract(Long contractId, String token);
    String extendContract(Long contractId, String token, int extraDays);
    String confirmAssignContract(Long contractId, String token);
    String rejectAssignContract(Long contractId, String token);
    String comfirmPickupContract(Long contractId, String token);
    String confirmReturnContract(Long contractId, String token, ReturnCarStatus returnCarStatus);
    List<RentalContractDTO> getPendingContracts();
    List<RentalContractDTO> getEmployeePendingContracts(String token);
    List<RentalContractDTO> getEmployeeContracts(String token);
}
