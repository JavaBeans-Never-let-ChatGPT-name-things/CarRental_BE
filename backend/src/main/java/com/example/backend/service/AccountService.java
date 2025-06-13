package com.example.backend.service;

import com.example.backend.entity.ReviewEntity;
import com.example.backend.service.dto.*;
import com.example.backend.service.dto.request.ContractRequestDTO;
import com.example.backend.service.dto.request.UpdateUserRequestDTO;
import com.example.backend.service.dto.response.UserSummaryDTO;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AccountService {
    Optional<AccountDTO> findByUsername(String token);
    void updateFavouriteCar(String carId, String token);
    List<CarDTO> getFavouriteCars(String token);
    void updateProfile(UpdateUserRequestDTO updateUserRequestDTO, String token) throws IOException;
    Long rentCar(ContractRequestDTO contractRequestDTO, String token, String carId);
    List<RentalContractDTO> getRentalContracts(String token);
    void reviewRentalContract(Long rentalContractId, ReviewEntity entity);
    List<UserDTO> searchAndSortUsers(String query, String sort, String status);
    UserDetailDTO getUserDetail(String displayName);
    void upgradeUserRole(String displayName);
    void downgradeUserRole(String displayName);
    List<String> getAvailableEmployees(Long contractId);
    void verifyAccount(String token);

    List<UserSummaryDTO> top3BestUser();
    List<UserSummaryDTO> top3WorstUser();
    List<UserSummaryDTO> top3BestUserFromDateToDate(LocalDate fromDate, LocalDate toDate);
    List<UserSummaryDTO> top3WorstUserFromDateToDate(LocalDate fromDate, LocalDate toDate);
}
