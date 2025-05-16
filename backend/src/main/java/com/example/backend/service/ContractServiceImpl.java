package com.example.backend.service;

import com.example.backend.entity.*;
import com.example.backend.entity.enums.CarState;
import com.example.backend.entity.enums.ContractStatus;
import com.example.backend.entity.enums.PaymentStatus;
import com.example.backend.entity.enums.ReturnCarStatus;
import com.example.backend.repository.AccountRepository;
import com.example.backend.repository.ContractRepository;
import com.example.backend.repository.FCMRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.service.dto.RentalContractDTO;
import com.example.backend.service.dto.request.NotificationFCMRequest;
import com.example.backend.service.dto.response.ContractSummaryDTO;
import com.example.backend.service.dto.response.MonthlyReportDTO;
import com.example.backend.service.dto.response.ReturnedStatusDTO;
import com.example.backend.service.mapper.RentalContractMapper;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ContractServiceImpl implements ContractService{
    ContractRepository contractRepository;
    JwtService jwtService;
    FCMRepository fcmRepository;
    AccountRepository accountRepository;
    FirebaseMessagingService firebaseMessagingService;
    PayOsService payOsService;
    RentalContractMapper rentalContractMapper;
    Map<Long, String> pendingPayments = new ConcurrentHashMap<>();
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    NotificationRepository notificationRepository;

    @Override
    public String retryContractSuccess(Long contractId, String token) {
        String username = jwtService.extractUserName(token);
        RentalContractEntity rentalContractEntity = contractRepository.findById(contractId)
                .orElseThrow( () -> new RuntimeException("Contract not found"));
        CarEntity carEntity = rentalContractEntity.getCar();
        if (rentalContractEntity.getPaymentStatus().equals(PaymentStatus.SUCCESS))
        {
            return "Contract's already done";
        }
        if (!username.equals(rentalContractEntity.getAccount().getUsername()))
        {
            return "You are not the owner of this contract";
        }
        if (rentalContractEntity.getContractStatus().equals(ContractStatus.EXPIRED))
        {
            return "Contract is expired";
        }
        rentalContractEntity.setPaymentStatus(PaymentStatus.SUCCESS);
        carEntity.setState(CarState.RENTED);
        contractRepository.save(rentalContractEntity);
        payOsService.sendNotificationToAdmin(rentalContractEntity);
        return "Successfully paid for contract id: " + rentalContractEntity.getId();
    }

    @Override
    public String retryContract(Long contractId, String token) {
        String username = jwtService.extractUserName(token);
        RentalContractEntity rentalContractEntity = contractRepository.findById(contractId)
                .orElseThrow( () -> new RuntimeException("Contract not found"));
        CarEntity carEntity = rentalContractEntity.getCar();
        if (!carEntity.getState().equals(CarState.AVAILABLE))
        {
            return "Car is unavailable";
        }
        if (!username.equals(rentalContractEntity.getAccount().getUsername()))
        {
            return "You are not the owner of this contract";
        }
        if (rentalContractEntity.getContractStatus().equals(ContractStatus.EXPIRED))
        {
            return "Contract is expired";
        }
        if (rentalContractEntity.getRetryCountLeft() == 0 ){
            return "You have no retry left";
        }
        Instant now = Instant.now();
        if (rentalContractEntity.getLastRetryAt() != null && Duration.between(rentalContractEntity.getLastRetryAt(), now).toMinutes() < 5) {
            return "Please wait 5 minutes before retrying";
        }
        rentalContractEntity.setRetryCountLeft(rentalContractEntity.getRetryCountLeft() - 1);
        rentalContractEntity.setLastRetryAt(now);
        carEntity.setState(CarState.RENTED);
        contractRepository.save(rentalContractEntity);
        pendingPayments.put(rentalContractEntity.getId(), carEntity.getId());
        scheduler.schedule(() -> {
            if (pendingPayments.containsKey(rentalContractEntity.getId())) {
                payOsService.paymentFailed(carEntity.getId());
            }
        }, 5, TimeUnit.MINUTES);
        return "Successfully holded for contract id: " + rentalContractEntity.getId();
    }

    @Override
    public String assignContract(Long contractId, String employeeName) {
        AccountEntity accountEntity = accountRepository.findByUsername(employeeName)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        RentalContractEntity rentalContract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        if (rentalContract.getPaymentStatus() != PaymentStatus.SUCCESS)
        {
            return "Payment is not successful";
        }
        rentalContract.setEmployee(accountEntity);
        rentalContract.setPending(true);
        List<FCMTokenEntity> fcmTokenList = fcmRepository.findAllByUserId(accountEntity.getId());
        if (!fcmTokenList.isEmpty()) {
            for (FCMTokenEntity fcmTokenEntity : fcmTokenList) {
                String token = fcmTokenEntity.getToken();
                NotificationFCMRequest notificationFCMRequest = NotificationFCMRequest.builder()
                        .token(token)
                        .title("New Contract Assigned")
                        .body("Contract No: " + rentalContract.getId() + " is assigned to you, please confirm or reject")
                        .image(rentalContract.getCar().getCarImageUrl())
                        .build();
                firebaseMessagingService.sendNotification(notificationFCMRequest);
            }
        }
        contractRepository.save(rentalContract);
        return "Successfully picked up contract id: " + rentalContract.getId();
    }

    @Override
    public String reportLostContract(Long contractId, String token) {
        String username = jwtService.extractUserName(token);
        RentalContractEntity rentalContract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        if (!username.equals(rentalContract.getAccount().getUsername()))
        {
            throw new RuntimeException("You are not the owner of this contract");
        }
        rentalContract.setContractStatus(ContractStatus.COMPLETE);
        rentalContract.setReturnCarStatus(ReturnCarStatus.LOST);
        rentalContract.setPenaltyFee(rentalContract.getPenaltyFee()+rentalContract.getCar().getRentalPrice() * 0.8f);
        contractRepository.save(rentalContract);
        sendNotificationToEmployee(rentalContract, rentalContract.getCar().getCarImageUrl(), rentalContract.getEmployee(), rentalContract.getPenaltyFee());
        return "Successfully reported lost contract id: " + rentalContract.getId();
    }

    @Override
    public String extendContract(Long contractId, String token, int extraDays) {
        String username = jwtService.extractUserName(token);
        RentalContractEntity rentalContract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        if (!username.equals(rentalContract.getAccount().getUsername()))
            throw new RuntimeException("You are not the owner of this contract");
        if (rentalContract.getEndDate().minusDays(1).isAfter(LocalDate.now()))
            throw new RuntimeException("Too early to extend");
        if (extraDays > 4 || !rentalContract.isExtendable())
            throw new RuntimeException("Cannot extend");

        rentalContract.setEndDate(rentalContract.getEndDate().plusDays(extraDays));
        rentalContract.setExtendable(false);
        List<FCMTokenEntity> fcmTokenList = fcmRepository.findAllByUserId(rentalContract.getEmployee().getId());
        if (!fcmTokenList.isEmpty()) {
            for (FCMTokenEntity fcmTokenEntity : fcmTokenList) {
                String fcmToken = fcmTokenEntity.getToken();
                NotificationFCMRequest notificationFCMRequest = NotificationFCMRequest.builder()
                        .token(fcmToken)
                        .title("Contract Extended")
                        .body("Contract No: " + rentalContract.getId() + " from user " + rentalContract.getAccount().getUsername() + " has been extended by " + extraDays + " days")
                        .image(rentalContract.getCar().getCarImageUrl())
                        .build();
                firebaseMessagingService.sendNotification(notificationFCMRequest);
            }
        }
        contractRepository.save(rentalContract);
        return "Successfully extended contract id: " + rentalContract.getId()+ " by " + extraDays + " days";
    }

    @Override
    public String confirmAssignContract(Long contractId, String token) {
        String username= jwtService.extractUserName(token);
        RentalContractEntity rentalContract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        if (!username.equals(rentalContract.getEmployee().getUsername()))
        {
            return "You are not incharge of this contract";
        }
        rentalContract.setPending(false);
        List<FCMTokenEntity> fcmTokenList = fcmRepository.findAllByUserId(rentalContract.getAccount().getId());
        if (!fcmTokenList.isEmpty()) {
            NotificationFCMRequest notificationFCMRequest = NotificationFCMRequest.builder()
                    .title("Contract Assigned")
                    .body("Contract No: " + rentalContract.getId() + " has been assigned to employee " + rentalContract.getEmployee().getDisplayName()
                            + ", please meet he/she to pick up the car at " + rentalContract.getStartDate())
                    .image(rentalContract.getCar().getCarImageUrl())
                    .build();
            for (FCMTokenEntity fcmTokenEntity : fcmTokenList) {
                String fcmToken = fcmTokenEntity.getToken();
                notificationFCMRequest.setToken(fcmToken);
                firebaseMessagingService.sendNotification(notificationFCMRequest);
            }
            NotificationEntity notificationEntity = NotificationEntity.builder()
                    .title(notificationFCMRequest.getTitle())
                    .message(notificationFCMRequest.getBody())
                    .isRead(false)
                    .imageUrl(rentalContract.getCar().getCarImageUrl())
                    .account(rentalContract.getAccount())
                    .build();
            notificationRepository.save(notificationEntity);
        }
        contractRepository.save(rentalContract);
        return "Successfully confirm incharge for contract id: " + rentalContract.getId();
    }

    @Override
    public String rejectAssignContract(Long contractId, String token) {
        String username= jwtService.extractUserName(token);
        RentalContractEntity rentalContract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        if (!username.equals(rentalContract.getEmployee().getUsername()))
        {
            return "You are not incharge of this contract";
        }
        sendNotificationToAdmin(rentalContract);
        rentalContract.setEmployee(null);
        rentalContract.setPending(false);
        contractRepository.save(rentalContract);
        return "Successfully rejected incharge for contract id: " + rentalContract.getId();
    }

    @Override
    public String comfirmPickupContract(Long contractId, String token) {
        String username= jwtService.extractUserName(token);
        RentalContractEntity rentalContract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        if (!username.equals(rentalContract.getEmployee().getUsername()))
        {
            return "You are not incharge of this contract";
        }
        if (rentalContract.getStartDate().isAfter(LocalDate.now()))
        {
            return "Contract is not started yet";
        }
        if (rentalContract.getPaymentStatus() != PaymentStatus.SUCCESS)
        {
            return "Payment is not successful";
        }
        rentalContract.setContractStatus(ContractStatus.PICKED_UP);
        return "Successfully confirmed pickup for contract id: " + rentalContract.getId();
    }

    private void sendNotificationToAdmin(RentalContractEntity rentalContract) {
        List<FCMTokenEntity> fcmTokenList = fcmRepository.findAllByUserId(3L);
        if (fcmTokenList.isEmpty()) {
            log.info("No tokens found for admin");
            return;
        }
        for (FCMTokenEntity fcmTokenEntity : fcmTokenList) {
            String token = fcmTokenEntity.getToken();
            NotificationFCMRequest notificationFCMRequest = NotificationFCMRequest.builder()
                    .token(token)
                    .title("Reject Assign Contract")
                    .body("Employee " + rentalContract.getEmployee().getDisplayName() + " rejected contract no: " + rentalContract.getId())
                    .image(rentalContract.getCar().getCarImageUrl())
                    .build();
            firebaseMessagingService.sendNotification(notificationFCMRequest);
        }
    }

    @Override
    public String confirmReturnContract(Long contractId, String token, ReturnCarStatus returnCarStatus) {
        RentalContractEntity rentalContract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        rentalContract.setReturnDate(LocalDate.now());
        rentalContract.setReturnCarStatus(returnCarStatus);
        if (returnCarStatus.equals(ReturnCarStatus.DAMAGED))
        {
            rentalContract.setPenaltyFee(rentalContract.getPenaltyFee() + 100);
        }
        rentalContract.setContractStatus(ContractStatus.COMPLETE);
        CarEntity carEntity = rentalContract.getCar();
        carEntity.setState(CarState.AVAILABLE);
        contractRepository.save(rentalContract);
        return "Successfully returned contract id: " + rentalContract.getId();
    }

    @Override
    public List<RentalContractDTO> getPendingContracts() {
        return contractRepository.findAllByContractStatusIsAndStartDateBeforeAndPaymentStatusIs(ContractStatus.BOOKED, LocalDate.now(), PaymentStatus.SUCCESS)
                .stream()
                .filter( rentalContract -> rentalContract.getEmployee() == null)
                .map(rentalContractMapper::toDto)
                .toList();
    }

    @Override
    public List<RentalContractDTO> getEmployeePendingContracts(String token) {
        String username = jwtService.extractUserName(token);
        AccountEntity employee = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return contractRepository.findAllByPendingIsTrueAndEmployee_Id(employee.getId())
                .stream()
                .map(rentalContractMapper::toDto)
                .toList();
    }

    @Override
    public List<RentalContractDTO> getEmployeeContracts(String token) {
        return contractRepository.findAllByEmployee_Username(jwtService.extractUserName(token))
                .stream()
                .map(rentalContractMapper::toDto)
                .toList();
    }

    @Override
    public List<MonthlyReportDTO> totalRevenueByMonth(int year) {
        List<Object[]> monthlyRevenue = contractRepository.findMonthlyRevenueByYear(year);
        return monthlyRevenue.stream()
                .map(obj -> {
                    int month = ((Number) obj[0]).intValue();
                    double revenue = ((Number) obj[1]).doubleValue();
                    return MonthlyReportDTO.builder()
                            .month(month)
                            .value(Math.ceil(revenue))
                            .build();
                }).toList();
    }

    @Override
    public List<MonthlyReportDTO> totalPenaltyByMonth(int year) {
        List<Object[]> monthlyPenalty = contractRepository.findMonthlyPenaltyByYear(year);
        return monthlyPenalty.stream()
                .map(obj ->{
                    int month = (Integer) obj[0];
                    double revenue = (Double) obj[1];
                    return MonthlyReportDTO.builder()
                            .month(month)
                            .value(Math.ceil(revenue))
                            .build();
                }).toList();
    }

    @Override
    public Double totalRevenueFromDateToDate(LocalDate startDate, LocalDate endDate) {
        return Math.ceil(contractRepository.totalRevenueFromDateToDate(startDate, endDate));
    }

    @Override
    public Double totalPenaltyFromDateToDate(LocalDate startDate, LocalDate endDate) {
        return Math.ceil(contractRepository.totalPenaltyFromDateToDate(startDate, endDate));
    }

    @Override
    public Double totalRevenue() {
        return  Math.ceil(contractRepository.totalRevenue());
    }

    @Override
    public Double totalPenalty() {
        return  Math.ceil(contractRepository.totalPenalty());
    }

    @Override
    public List<ContractSummaryDTO> getContractSummaryFromDateToDate(LocalDate startDate, LocalDate endDate) {
        List<Object[]> contractSummary = contractRepository.countContractsByStatusFromDateToDate(startDate, endDate);
        return contractSummary.stream()
                .map(obj ->{
                    String status = ((String) obj[0]);
                    int count = ((Long) obj[1]).intValue();
                    return ContractSummaryDTO.builder()
                            .contractStatus(ContractStatus.valueOf(status))
                            .value(count)
                            .build();
                }).toList();
    }

    @Override
    public List<ContractSummaryDTO> getContractSummary() {
        List<Object[]> contractSummary = contractRepository.countContractsByStatus();
        return contractSummary.stream()
                .map(obj ->{
                    String status = ((String) obj[0]);
                    int count = ((Long) obj[1]).intValue();
                    return ContractSummaryDTO.builder()
                            .contractStatus(ContractStatus.valueOf(status))
                            .value(count)
                            .build();
                }).toList();
    }

    @Override
    public List<ReturnedStatusDTO> getReturnedStatusFromDateToDate(LocalDate startDate, LocalDate endDate) {
        List<Object[]> returnedStatus = contractRepository.countContractsByReturnCarStatusFromDateToDate(startDate, endDate);
        return returnedStatus.stream()
                .map(obj ->{
                    String status = ((String) obj[0]);
                    int count = ((Long) obj[1]).intValue();
                    return ReturnedStatusDTO.builder()
                            .returnCarStatus(ReturnCarStatus.valueOf(status))
                            .value(count)
                            .build();
                }).toList();
    }

    @Override
    public List<ReturnedStatusDTO> getReturnedStatus() {
        List<Object[]> returnedStatus = contractRepository.countContractsByReturnCarStatus();
        return returnedStatus.stream()
                .map(obj ->{
                    String status = ((String) obj[0]);
                    int count = ((Long) obj[1]).intValue();
                    return ReturnedStatusDTO.builder()
                            .returnCarStatus(ReturnCarStatus.valueOf(status))
                            .value(count)
                            .build();
                }).toList();
    }
    private void sendNotificationToEmployee(RentalContractEntity rentalContract, String carImageUrl, AccountEntity employee, float penaltyFee) {
        List<FCMTokenEntity> fcmTokenList = fcmRepository.findAllByUserId(employee.getId());
        if (fcmTokenList.isEmpty()) {
            log.info("No tokens found for employee");
            return;
        }
        Long id = rentalContract.getId();
        NotificationFCMRequest notificationFCMRequest = new NotificationFCMRequest();
        String username = rentalContract.getAccount().getUsername();
        notificationFCMRequest.setTitle("User " + username + " reported Car " + rentalContract.getCar().getId() + " as lost");
        notificationFCMRequest.setBody("Contract No: " + id + " of user " + username + " is reported as lost. Penalty fee: " + penaltyFee);
        notificationFCMRequest.setImage(carImageUrl);
        for (FCMTokenEntity fcmTokenEntity : fcmTokenList) {
            String token = fcmTokenEntity.getToken();
            notificationFCMRequest.setToken(token);
            firebaseMessagingService.sendNotification(notificationFCMRequest);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void updateContractsStatus() {
        LocalDate currentDate = LocalDate.now();
        log.info("Running scheduled task: updateContractsStatus");
        List<RentalContractEntity> contracts = contractRepository.findAll();
        for (RentalContractEntity contract: contracts) {
            log.info("Checking contract ID {} with status {}", contract.getId(), contract.getContractStatus());
            log.info("Start date: {}, End date: {}, Return date: {}", contract.getStartDate(), contract.getEndDate(), contract.getReturnDate());
            if (contract.getContractStatus().equals(ContractStatus.BOOKED)
                    && contract.getStartDate().isEqual(currentDate)
                    && contract.getPaymentStatus().equals(PaymentStatus.SUCCESS)){
                contract.setContractStatus(ContractStatus.EXPIRED);
                contract.getCar().setState(CarState.AVAILABLE);
                contractRepository.save(contract);
                sendNotification(contract.getId(), contract.getCar().getCarImageUrl(), contract.getAccount(), ContractStatus.BOOKED, 0);
            }
            if (contract.getContractStatus().equals(ContractStatus.BOOKED)
             && contract.getStartDate().isBefore(currentDate)
             && contract.getPaymentStatus().equals(PaymentStatus.SUCCESS)){
                contract.setContractStatus(ContractStatus.EXPIRED);
                contract.getCar().setState(CarState.AVAILABLE);
                contractRepository.save(contract);
                log.info("Contract {} has expired", contract.getId());
                sendNotification(contract.getId(), contract.getCar().getCarImageUrl(), contract.getAccount(), ContractStatus.EXPIRED, 0);
            }
            if (contract.getContractStatus().equals(ContractStatus.PICKED_UP)
            && contract.getEndDate().isBefore(currentDate)
            && contract.getReturnDate()==null){
                contract.setContractStatus(ContractStatus.OVERDUE);
                contract.setReturnCarStatus(ReturnCarStatus.NOT_RETURNED);
                sendNotification(contract.getId(), contract.getCar().getCarImageUrl(), contract.getAccount(), ContractStatus.OVERDUE, contract.getPenaltyFee());
                contract.setPenaltyFee(contract.getPenaltyFee() + 100);
                contractRepository.save(contract);
                log.info("Contract {} is overdue", contract.getId());
            }
            if (contract.getContractStatus().equals(ContractStatus.OVERDUE)) {
                contract.setPenaltyFee(contract.getPenaltyFee() + 100);
                contractRepository.save(contract);
                log.info("Contract {} is still overdue", contract.getId());
                sendNotification(contract.getId(), contract.getCar().getCarImageUrl(), contract.getAccount(), ContractStatus.OVERDUE, contract.getPenaltyFee());

                if (contract.getEndDate().plusDays(3).isBefore(currentDate) && contract.getEmployee() != null) {
                    sendNotificationToEmployee(
                            contract,
                            contract.getCar().getCarImageUrl(),
                            contract.getEmployee(),
                            contract.getPenaltyFee()
                    );
                    log.info("Notified employee about long overdue contract {}", contract.getId());
                }
            }
        }
    }

    private void sendNotification(Long contractId, String carImage, AccountEntity account, ContractStatus contractStatus, float penaltyFee) {
        List<FCMTokenEntity> fcmTokenList = fcmRepository.findAllByUserId(account.getId());
        if (fcmTokenList.isEmpty()) {
            log.info("No tokens found for user");
            return;
        }
        NotificationFCMRequest notificationFCMRequest = new NotificationFCMRequest();
        notificationFCMRequest.setTitle("Contract No: " + contractId + " status changed to " + contractStatus.name());
        if (contractStatus.equals(ContractStatus.BOOKED)){
            notificationFCMRequest.setTitle("Contrct No: " + contractId + " started today");
            notificationFCMRequest.setBody("Please remember to pick up the car today");
        }
        if (contractStatus.equals(ContractStatus.EXPIRED)){
            notificationFCMRequest.setBody("Your contract has expired");
        }
        else if (contractStatus.equals(ContractStatus.OVERDUE) && penaltyFee == 0){
            notificationFCMRequest.setBody("Your contract is overdue, please return the car. Penalty fee will be applied from today." +
                    " Current Penalty fee: " + penaltyFee);
        }
        else if (contractStatus.equals(ContractStatus.OVERDUE) && penaltyFee > 0){
            notificationFCMRequest.setBody("Your contract is overdue, please return the car. Current Penalty fee: " + penaltyFee);
        }
        notificationFCMRequest.setImage(carImage);
        for (FCMTokenEntity fcmTokenEntity : fcmTokenList) {
            String token = fcmTokenEntity.getToken();
            notificationFCMRequest.setToken(token);
            String result = firebaseMessagingService.sendNotification(notificationFCMRequest);
            if (!result.equals("Notification sent successfully")) {
                log.info("Failed to send notification due to: {}", result);
            }
            log.info("Successfully sent notification to token: {}", token);
        }
        NotificationEntity notificationEntity = NotificationEntity.builder()
                .title(notificationFCMRequest.getTitle())
                .message(notificationFCMRequest.getBody())
                .isRead(false)
                .imageUrl(carImage)
                .account(account)
                .build();
        notificationRepository.save(notificationEntity);
        log.info("Notification saved successfully");
    }
}
