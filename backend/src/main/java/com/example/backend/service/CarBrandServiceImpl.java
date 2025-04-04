package com.example.backend.service;

import com.example.backend.entity.CarBrandEntity;
import com.example.backend.repository.CarBrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CarBrandServiceImpl implements CarBrandService{
    private final CarBrandRepository carBrandRepository;
    @Override
    public List<CarBrandEntity> getAllCarBrands() {
        return carBrandRepository.findAll();
    }
}
