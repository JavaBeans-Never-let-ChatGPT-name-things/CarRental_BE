package com.example.backend.service;

import com.example.backend.entity.*;
import com.example.backend.entity.enums.CarState;
import com.example.backend.entity.enums.ContractStatus;
import com.example.backend.entity.enums.PaymentStatus;
import com.example.backend.entity.enums.ReturnCarStatus;
import com.example.backend.repository.AccountRepository;
import com.example.backend.repository.ContractRepository;
import com.example.backend.repository.FCMRepository;
import com.example.backend.service.dto.request.NotificationFCMRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ContractServiceImpl implements ContractService{
    ContractRepository contractRepository;
    JwtService jwtService;
    FCMRepository fcmRepository;
    FirebaseMessagingService firebaseMessagingService;
    AccountRepository accountRepository;
    @Override
    public String retryContract(Long contractId, String token) {
        String username = jwtService.extractUserName(token);
        RentalContractEntity rentalContractEntity = contractRepository.findById(contractId)
                .orElseThrow( () -> new RuntimeException("Contract not found"));
        CarEntity carEntity = rentalContractEntity.getCar();
        if (rentalContractEntity.getPaymentStatus().equals(PaymentStatus.SUCCESS))
        {
            return "Contract's already done";
        }
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
        rentalContractEntity.setPaymentStatus(PaymentStatus.SUCCESS);
        carEntity.setState(CarState.RENTED);
        contractRepository.save(rentalContractEntity);
        return "Successfully paid for contract id: " + rentalContractEntity.getId();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateContractsStatus() {
        LocalDate currentDate = LocalDate.now();
        List<RentalContractEntity> contracts = contractRepository.findAll();
        for (RentalContractEntity contract: contracts) {
            if (contract.getContractStatus().equals(ContractStatus.BOOKED)
             && contract.getStartDate().isBefore(currentDate)
             && contract.getPaymentStatus().equals(PaymentStatus.SUCCESS)){
                contract.setContractStatus(ContractStatus.EXPIRED);
                contract.getCar().setState(CarState.AVAILABLE);
                contractRepository.save(contract);
                log.info("Contract {} has expired", contract.getId());
                sendNotification(contract.getId(), contract.getCar().getCarImageUrl(), contract.getAccount(), ContractStatus.EXPIRED);
            }
            if (contract.getContractStatus().equals(ContractStatus.COMPLETE)
            && contract.getEndDate().isBefore(currentDate)
            && contract.getReturnDate()==null){
                contract.setContractStatus(ContractStatus.OVERDUE);
                contract.getCar().setState(CarState.AVAILABLE);
                contract.setReturnCarStatus(ReturnCarStatus.NOT_RETURNED);
                contractRepository.save(contract);
                log.info("Contract {} is overdue", contract.getId());
                sendNotification(contract.getId(), contract.getCar().getCarImageUrl(), contract.getAccount(), ContractStatus.OVERDUE);
            }
        }
    }

    private void sendNotification(Long contractId, String carImage, AccountEntity account, ContractStatus contractStatus){
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
        else if (contractStatus.equals(ContractStatus.OVERDUE)){
            notificationFCMRequest.setBody("Your contract is overdue, please return the car. And penalty fee will be applied from today");
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
        account.addNotification(notificationEntity);
        accountRepository.save(account);
        log.info("Notification saved successfully");
    }
}
