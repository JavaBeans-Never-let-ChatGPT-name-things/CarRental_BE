package com.example.backend.init;

import com.example.backend.entity.AccountEntity;
import com.example.backend.entity.CarEntity;
import com.example.backend.entity.RentalContractEntity;
import com.example.backend.entity.enums.*;
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
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

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
        if (contractRepository.count() > 0) {
            log.info("Contract data already exists in the database.");
            return;
        }

        log.info("Initializing contract data...");

        List<String> carIds = List.of(
                "Audi A3", "Audi e-tron", "BMW 3 Series", "Chevrolet Camaro",
                "Ferrari 488 GTB", "Ferrari GTC4Lusso", "Ford Mustang"
        );

        List<CarEntity> allCars = carRepository.findAllById(carIds);
        Map<String, CarEntity> carMap = allCars.stream()
                .collect(Collectors.toMap(CarEntity::getId, c -> c));

        Random rnd = new Random();
        List<AccountEntity> users = accountRepository.findAll().stream()
                .filter(a -> a.getAccountRole() == AccountRole.USER)
                .toList();

        List<RentalContractEntity> contracts = new ArrayList<>();

        for (AccountEntity user : users) {
            int numContracts = 2 + rnd.nextInt(2);

            for (int i = 0; i < numContracts; i++) {
                String randomCarId = carIds.get(rnd.nextInt(carIds.size()));
                CarEntity car = carMap.get(randomCarId);

                RentalContractEntity contract = new RentalContractEntity();
                contract.setAccount(user);
                contract.setEmployee(null);
                contract.setCar(car);

                LocalDate start = LocalDate.now().minusDays(rnd.nextInt(10));
                LocalDate end = start.plusDays(3);
                contract.setStartDate(start);
                contract.setEndDate(end);

                contract.setDeposit(500f + rnd.nextInt(500));
                contract.setTotalPrice(1000f + rnd.nextInt(1000));
                contract.setPaymentMethod("PayOS");

                contract.setRetryCountLeft(3);
                contract.setLastRetryAt(null);

                if (rnd.nextBoolean()) {
                    contract.setPaymentStatus(PaymentStatus.SUCCESS);
                    car.setState(CarState.RENTED);
                } else {
                    contract.setPaymentStatus(PaymentStatus.FAILED);
                    car.setState(CarState.AVAILABLE);
                }

                contract.setContractStatus(ContractStatus.BOOKED);
                carRepository.save(car);
                contracts.add(contract);
            }
        }

        contractRepository.saveAll(contracts);
        log.info("Contract data initialized successfully.");
    }
}


