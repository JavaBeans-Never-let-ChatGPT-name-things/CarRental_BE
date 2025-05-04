package com.example.backend.init;

import com.example.backend.entity.AccountEntity;
import com.example.backend.entity.CarEntity;
import com.example.backend.entity.RentalContractEntity;
import com.example.backend.entity.enums.CarState;
import com.example.backend.entity.enums.ContractStatus;
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

                LocalDate startDate = LocalDate.now().minusDays(random.nextInt(10));
                LocalDate endDate = startDate.plusDays(3);
                contract.setStartDate(startDate);
                contract.setEndDate(endDate);

                contract.setDeposit(500.0f + i * 100);
                contract.setPaymentMethod("PayOS");
                contract.setTotalPrice(1000.0f + i * 200);
                contract.setPenaltyFee(0.0f);

                boolean isPaid = random.nextBoolean();
                if (isPaid) {
                    contract.setPaymentStatus(PaymentStatus.SUCCESS);

                    int statusCase = random.nextInt(5);
                    switch (statusCase) {
                        case 0 -> {
                            contract.setContractStatus(ContractStatus.BOOKED);
                            car.setState(CarState.RENTED);
                        }
                        case 1 -> {
                            contract.setContractStatus(ContractStatus.PICKED_UP);
                            car.setState(CarState.RENTED);
                        }
                        case 2 -> {
                            contract.setContractStatus(ContractStatus.COMPLETE);
                            contract.setReturnDate(endDate);
                            contract.setReturnCarStatus(ReturnCarStatus.INTACT);
                        }
                        case 3 -> {
                            contract.setContractStatus(ContractStatus.OVERDUE);
                            contract.setReturnDate(null);
                            car.setState(CarState.RENTED);
                            contract.setReturnCarStatus(ReturnCarStatus.NOT_RETURNED);
                        }
                        case 4 -> {
                            contract.setContractStatus(ContractStatus.REVIEWED);
                            contract.setReturnDate(endDate);
                            contract.setReturnCarStatus(ReturnCarStatus.INTACT);
                        }
                    }

                } else {
                    contract.setPaymentStatus(PaymentStatus.FAILED);
                    contract.setContractStatus(ContractStatus.BOOKED);
                    contract.setReturnCarStatus(null);
                    contract.setReturnDate(null);
                }

                carRepository.save(car);
                contracts.add(contract);
            }

            contractRepository.saveAll(contracts);
            log.info("Contract data initialized successfully.");
        } else {
            log.info("Contract data already exists in the database.");
        }
    }
}

