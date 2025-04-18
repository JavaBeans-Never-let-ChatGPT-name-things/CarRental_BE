package com.example.backend.service;

import com.example.backend.entity.AccountEntity;
import com.example.backend.entity.CarEntity;
import com.example.backend.repository.AccountRepository;
import com.example.backend.repository.CarRepository;
import com.example.backend.service.dto.AccountDTO;
import com.example.backend.service.dto.CarDTO;
import com.example.backend.service.dto.request.CarPageRequestDTO;
import com.example.backend.service.dto.request.UpdateUserRequestDTO;
import com.example.backend.service.mapper.AccountMapper;
import com.example.backend.service.mapper.CarMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AccountServiceImpl implements AccountService{
    private final AccountRepository accountRepository;
    private final CarRepository carRepository;
    private final AccountMapper accountMapper;
    private final CarMapper carMapper;
    private final JwtService jwtService;
    private final CloudinaryService cloudinaryService;
    private final AuthenticationService authenticationService;
    private final EmailService emailService;
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
    public void updateProfile(UpdateUserRequestDTO updateUserRequestDTO, String token) throws IOException {
        String url = cloudinaryService.uploadFile(updateUserRequestDTO.getAvatar(), "profile");
        AccountEntity account = accountRepository.findByUsername(jwtService.extractUserName(token)).orElseThrow(
                () -> new RuntimeException("Account not found")
        );
        account.setGender(updateUserRequestDTO.getGender());
        account.setDisplayName(updateUserRequestDTO.getDisplayName());
        account.setAddress(updateUserRequestDTO.getAddress());
        account.setPhoneNumber(updateUserRequestDTO.getPhoneNumber());
        account.setAvatarUrl(url);
        account.setEmail(updateUserRequestDTO.getEmail());
        accountRepository.save(account);
    }

}
