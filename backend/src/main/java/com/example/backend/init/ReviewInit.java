package com.example.backend.init;

import com.example.backend.entity.AccountEntity;
import com.example.backend.entity.CarEntity;
import com.example.backend.entity.RentalContractEntity;
import com.example.backend.entity.ReviewEntity;
import com.example.backend.entity.enums.CarState;
import com.example.backend.entity.enums.ContractStatus;
import com.example.backend.entity.enums.PaymentStatus;
import com.example.backend.repository.AccountRepository;
import com.example.backend.repository.CarRepository;
import com.example.backend.repository.ContractRepository;
import com.example.backend.repository.ReviewRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Order(4)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ReviewInit implements CommandLineRunner {
    ReviewRepository reviewRepository;
    AccountRepository accountRepository;
    ContractRepository contractRepository;
    CarRepository carRepository;
    List<String> comments = List.of(
            "Great car, very comfortable!",
            "Had a wonderful experience with this car.",
            "The car was in excellent condition.",
            "I loved the features of this car.",
            "Nah, not my type.",
            "The car was okay, but not great.",
            "I had some issues with the car."
    );
    @Override
    public void run(String... args) throws Exception {
        if (reviewRepository.count() == 0) {
            log.info("Initializing review data...");

            List<RentalContractEntity> successfulContracts = contractRepository.findAll()
                    .stream()
                    .filter(contract -> contract.getContractStatus().equals(ContractStatus.COMPLETE))
                    .toList();
            Random random = new Random();

            for (RentalContractEntity contract : successfulContracts) {
                int starNum = 1 + random.nextInt(5);
                String comment = comments.get(random.nextInt(comments.size()));
                AccountEntity account = accountRepository.findById(contract.getAccount().getId())
                        .orElseThrow(() -> new RuntimeException("Account not found"));
                CarEntity car = carRepository.findById(contract.getCar().getId())
                        .orElseThrow(() -> new RuntimeException("Car not found"));
                if (car.getState() != CarState.RENTED)
                {
                    log.warn("Car is not in rented state, skipping review.");
                    continue;
                }
                car.setReviewsNum(car.getReviewsNum() + 1);
                car.setRating((car.getRating() * (car.getReviewsNum() - 1) + starNum) / car.getReviewsNum());
                carRepository.save(car);
                ReviewEntity review = new ReviewEntity();
                review.setStarsNum(starNum);
                review.setComment(comment);
                review.setAccount(account);
                contract.setReview(review);
                contractRepository.save(contract);
            }
            log.info("Review data initialized successfully.");
        } else {
            log.info("Review data already exists in the database.");
        }
    }
}
