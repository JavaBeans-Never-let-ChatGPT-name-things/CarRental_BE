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

import java.time.Instant;
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

            AccountEntity user = accountRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("User account not found"));

            List<String> carIds = List.of(
                    "Audi A3","Audi e-tron","BMW 3 Series","Chevrolet Camaro",
                    "Ferrari 488 GTB","Ferrari GTC4Lusso","Ford Mustang"
            );

            List<RentalContractEntity> contracts = new ArrayList<>();
            Random rnd = new Random();

            for (String carId : carIds) {
                CarEntity car = carRepository.findById(carId)
                        .orElseThrow(() -> new RuntimeException("Car not found: " + carId));

                var c = new RentalContractEntity();
                c.setAccount(user);
                c.setEmployee(null);
                c.setCar(car);

                LocalDate start = LocalDate.now().minusDays(rnd.nextInt(10));
                LocalDate end = start.plusDays(3);
                c.setStartDate(start);
                c.setEndDate(end);

                c.setDeposit(500f + rnd.nextInt(500));
                c.setTotalPrice(1000f + rnd.nextInt(1000));
                c.setPaymentMethod("PayOS");

                c.setRetryCountLeft(3);
                c.setLastRetryAt(null);

                if (rnd.nextBoolean()) {
                    c.setPaymentStatus(PaymentStatus.SUCCESS);
                    car.setState(CarState.RENTED);
                } else {
                    c.setPaymentStatus(PaymentStatus.FAILED);
                    car.setState(CarState.AVAILABLE);
                }
                c.setContractStatus(ContractStatus.BOOKED);
                carRepository.save(car);
                contracts.add(c);
            }

            contractRepository.saveAll(contracts);
            log.info("Contract data initialized successfully.");
        } else {
            log.info("Contract data already exists in the database.");
        }
    }
}


