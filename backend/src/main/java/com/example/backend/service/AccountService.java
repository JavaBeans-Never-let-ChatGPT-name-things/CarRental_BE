package com.example.backend.service;

import com.example.backend.service.dto.AccountDTO;
import com.example.backend.service.dto.CarDTO;
import com.example.backend.service.dto.request.CarPageRequestDTO;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface AccountService {
    Optional<AccountDTO> findByUsername(String token);
    void updateFavouriteCar(String carId, String token);
    Page<CarDTO> getFavouriteCars(String token, CarPageRequestDTO carPageRequestDTO);
}
