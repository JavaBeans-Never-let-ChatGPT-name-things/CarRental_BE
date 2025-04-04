package com.example.backend.service;

import com.example.backend.repository.CarRepository;
import com.example.backend.service.dto.CarDTO;
import com.example.backend.service.mapper.CarMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService{
    private final CarRepository carRepository;
    private final CarMapper carMapper;
    @Override
    public List<CarDTO> findAll() {
        return carMapper.toDto(carRepository.findAll());
    }
}
