package com.example.backend.service;

import com.example.backend.entity.*;
import com.example.backend.entity.enums.CarState;
import com.example.backend.entity.enums.ContractStatus;
import com.example.backend.entity.enums.PaymentStatus;
import com.example.backend.entity.enums.ReturnCarStatus;
import com.example.backend.repository.ContractRepository;
import com.example.backend.repository.FCMRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.service.dto.request.NotificationFCMRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
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
@Order(100)
public class ContractServiceImpl implements ContractService{
    ContractRepository contractRepository;
    JwtService jwtService;
    FCMRepository fcmRepository;
    FirebaseMessagingService firebaseMessagingService;
    PayOsService payOsService;
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
