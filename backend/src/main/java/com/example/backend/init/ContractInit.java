package com.example.backend.init;

import com.example.backend.entity.AccountEntity;
import com.example.backend.entity.CarEntity;
import com.example.backend.entity.RentalContractEntity;
import com.example.backend.entity.enums.PaymentStatus;
import com.example.backend.entity.enums.ReturnCarStatus;
import com.example.backend.repository.AccountRepository;
import com.example.backend.repository.CarRepository;
import com.example.backend.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(3)
public class ContractInit implements CommandLineRunner {
    private final ContractRepository contractRepository;
    private final CarRepository carRepository;
    private final AccountRepository accountRepository;
    @Override
    public void run(String... args) throws Exception {
        if (contractRepository.count() == 0) {
            log.info("Initializing contract data...");
            AccountEntity account = accountRepository.findById(1L).orElseThrow(
                    () -> new RuntimeException("Account not found"));
            List<String> carId = List.of("Audi A3", "Audi e-tron", "BMW 3 Series", "Chevrolet Camaro",
                                    "Ferrari 488 GTB", "Ferrari GTC4Lusso", "Ford Mustang");
            List<RentalContractEntity> contracts = new ArrayList<>();
            Random random = new Random();

            for (int i = 0; i < carId.size(); i++) {
                int finalI = i;
                CarEntity car = carRepository.findById(carId.get(i)).orElseThrow(
                        () -> new RuntimeException("Car not found: " + carId.get(finalI))
                );

                RentalContractEntity contract = new RentalContractEntity();

                contract.setAccount(account);
                contract.setCar(car);

                LocalDate startDate = LocalDate.now().minusDays(10 + i);
                LocalDate endDate = LocalDate.now().plusDays(100 + i);

                contract.setStartDate(startDate);
                contract.setEndDate(endDate);
                contract.setDeposit(500.0f + i * 100);
                contract.setPaymentMethod("CREDIT_CARD");
                boolean isSuccess = random.nextBoolean();
                if (isSuccess) {
                    contract.setPaymentStatus(PaymentStatus.SUCCESS);
                    ReturnCarStatus[] possibleStatuses = {
                            ReturnCarStatus.NOT_RETURNED,
                            ReturnCarStatus.INTACT,
                            ReturnCarStatus.DAMAGED,
                            ReturnCarStatus.LOST
                    };
                    ReturnCarStatus returnStatus = possibleStatuses[random.nextInt(possibleStatuses.length)];
                    contract.setReturnCarStatus(returnStatus);

                    if (returnStatus == ReturnCarStatus.NOT_RETURNED) {
                        contract.setReturnDate(null);
                    } else {
                        contract.setReturnDate(endDate);
                    }
                } else {
                    contract.setPaymentStatus(PaymentStatus.FAILED);
                    contract.setReturnCarStatus(null);
                    contract.setReturnDate(null);
                }

                contract.setTotalPrice(1000.0f + i * 200);
                contract.setPenaltyFee(0.0f);

                contracts.add(contract);
            }
            contractRepository.saveAll(contracts);
            log.info("Contract data initialized successfully.");
        }
        else
        {
            log.info("Contract data already exists in the database.");
        }
    }
}
