package com.example.backend.service;

import com.example.backend.entity.AccountEntity;
import com.example.backend.entity.CarEntity;
import com.example.backend.entity.RentalContractEntity;
import com.example.backend.entity.ReviewEntity;
import com.example.backend.entity.enums.CarState;
import com.example.backend.entity.enums.ContractStatus;
import com.example.backend.repository.AccountRepository;
import com.example.backend.repository.CarRepository;
import com.example.backend.repository.ContractRepository;
import com.example.backend.service.dto.AccountDTO;
import com.example.backend.service.dto.CarDTO;
import com.example.backend.service.dto.RentalContractDTO;
import com.example.backend.service.dto.request.ContractRequestDTO;
import com.example.backend.service.dto.request.UpdateUserRequestDTO;
import com.example.backend.service.mapper.AccountMapper;
import com.example.backend.service.mapper.CarMapper;
import com.example.backend.service.mapper.ContractRequestMapper;
import com.example.backend.service.mapper.RentalContractMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountServiceImpl implements AccountService{
    private final AccountRepository accountRepository;
    private final CarRepository carRepository;
    private final AccountMapper accountMapper;
    private final CarMapper carMapper;
    private final JwtService jwtService;
    private final CloudinaryService cloudinaryService;
    private final ContractRequestMapper contractRequestMapper;
    private final ContractRepository contractRepository;
    private final RentalContractMapper rentalContractMapper;
    @Override
    public Optional<AccountDTO> findByUsername(String token) {
        String username = jwtService.extractUserName(token);
        if (username != null) {
            return accountRepository.findByUsername(username)
                    .map(accountMapper::toDto);
        }
        return Optional.empty();
    }

    @Override
    public void updateFavouriteCar(String carId, String token) {
        String username = jwtService.extractUserName(token);
        if (username != null) {
            AccountEntity accountEntity = accountRepository.findByUsername(username).orElseThrow(
                    () -> new RuntimeException("Account not found")
            );
            CarEntity carEntity = carRepository.findById(carId).orElseThrow(
                    () -> new RuntimeException("Car not found")
            );
            if (accountEntity.getFavouriteCars().contains(carEntity)) {
                accountEntity.removeFavouriteCar(carEntity);
            } else {
                accountEntity.addFavouriteCar(carEntity);
            }
            accountRepository.save(accountEntity);
            carRepository.save(carEntity);
        }
    }

    @Override
    public List<CarDTO> getFavouriteCars(String token) {
        String username = jwtService.extractUserName(token);
        if (username != null) {
            AccountEntity accountEntity = accountRepository.findByUsername(username).orElseThrow(
                    () -> new RuntimeException("Account not found")
            );

            return carRepository.findAllByIdIn(accountEntity.getFavouriteCars().stream().map(CarEntity::getId).toList())
                    .stream().map(carMapper::toDto).toList();
        }
        return List.of();
    }


    //Todo : check if email is already used, and send email to confirm
    @Override
    public void updateProfile(UpdateUserRequestDTO updateUserRequestDTO, String token) {
        AccountEntity account = accountRepository.findByUsername(jwtService.extractUserName(token)).orElseThrow(
                () -> new RuntimeException("Account not found")
        );
        try {
            if (updateUserRequestDTO.getAvatar() != null) {
                String url = cloudinaryService.uploadFile(updateUserRequestDTO.getAvatar(), "profile");
                account.setAvatarUrl(url);
            }
        account.setGender(updateUserRequestDTO.getGender());
        account.setDisplayName(updateUserRequestDTO.getDisplayName());
        account.setAddress(updateUserRequestDTO.getAddress());
        account.setPhoneNumber(updateUserRequestDTO.getPhoneNumber());
        account.setEmail(updateUserRequestDTO.getEmail());
        accountRepository.save(account);
        }
        catch(Exception e){
            log.info("Error uploading file: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void rentCar(ContractRequestDTO contractRequestDTO, String token, String carId) {
        String username = jwtService.extractUserName(token);
        if (username != null) {
            AccountEntity accountEntity = accountRepository.findByUsername(username).orElseThrow(
                    () -> new RuntimeException("Account not found")
            );
            CarEntity carEntity = carRepository.findById(carId).orElseThrow(
                    () -> new RuntimeException("Car not found")
            );
            if (carEntity.getState() != CarState.AVAILABLE) {
                throw new RuntimeException("Car is not available");
            }
            RentalContractEntity rentalContractEntity = contractRequestMapper.toEntity(contractRequestDTO);
            rentalContractEntity.setContractStatus(ContractStatus.BOOKED);
            accountEntity.addContract(rentalContractEntity);
            carEntity.addContract(rentalContractEntity);
            rentalContractEntity.setAccount(accountEntity);
            rentalContractEntity.setCar(carEntity);
            carEntity.setState(CarState.RENTED);
            contractRepository.save(rentalContractEntity);
        }
    }

    @Override
    public List<RentalContractDTO> getRentalContracts(String token) {
        String username = jwtService.extractUserName(token);
        if (username != null) {
            AccountEntity accountEntity = accountRepository.findByUsername(username).orElseThrow(
                    () -> new RuntimeException("Account not found")
            );
            return contractRepository.findAllByAccount_Username(username)
                    .stream().map(rentalContractMapper::toDto).toList();
        }
        return List.of();
    }

    @Override
    public void reviewRentalContract(Long rentalContractId, ReviewEntity entity) {
        RentalContractEntity rentalContractEntity = contractRepository.findById(rentalContractId).orElseThrow(
                () -> new RuntimeException("Rental contract not found")
        );
        AccountEntity account = accountRepository.findById(rentalContractEntity.getAccount().getId()).orElseThrow(
                () -> new RuntimeException("Account not found")
        );
        entity.setAccount(account);
        CarEntity car = carRepository.findById(rentalContractEntity.getCar().getId()).orElseThrow(
                () -> new RuntimeException("Car not found")
        );
        if (car.getState() != CarState.RENTED) {
            throw new RuntimeException("Car is not rented");
        }
        int star = entity.getStarsNum();
        float currentRating = car.getRating() * car.getReviewsNum();
        car.setReviewsNum(car.getReviewsNum() + 1);
        car.setRating((currentRating + star) / car.getReviewsNum());
        carRepository.save(car);
        rentalContractEntity.setReview(entity);
        contractRepository.save(rentalContractEntity);
    }

}
