package com.example.backend.service;

import com.example.backend.service.dto.AccountDTO;
import com.example.backend.service.dto.CarDTO;

import java.util.List;
import java.util.Optional;

public interface AccountService {
    Optional<AccountDTO> findByUsername(String token);
    void updateFavouriteCar(String carId, String token);
    List<CarDTO> getFavouriteCars(String token);
}
