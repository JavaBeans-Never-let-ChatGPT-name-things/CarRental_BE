package com.example.backend.service;

import com.example.backend.entity.enums.ReturnCarStatus;
import com.example.backend.service.dto.RentalContractDTO;
import com.example.backend.service.dto.response.ContractSummaryDTO;
import com.example.backend.service.dto.response.MonthlyReportDTO;
import com.example.backend.service.dto.response.ReturnedStatusDTO;

import java.time.LocalDate;
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

    List<MonthlyReportDTO> totalRevenueByMonth(int year);

    List<MonthlyReportDTO> totalPenaltyByMonth(int year);

    Double totalRevenueFromDateToDate(LocalDate startDate, LocalDate endDate);
    Double totalPenaltyFromDateToDate(LocalDate startDate, LocalDate endDate);
    Double totalRevenue();
    Double totalPenalty();
    List<ContractSummaryDTO> getContractSummaryFromDateToDate(LocalDate startDate, LocalDate endDate);
    List<ContractSummaryDTO> getContractSummary();

    List<ReturnedStatusDTO> getReturnedStatusFromDateToDate(LocalDate startDate, LocalDate endDate);
    List<ReturnedStatusDTO> getReturnedStatus();
}
