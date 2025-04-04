package com.example.backend.service;

import com.example.backend.service.dto.CarDTO;

import java.util.List;


public interface CarService {
    List<CarDTO> findAll();
}
