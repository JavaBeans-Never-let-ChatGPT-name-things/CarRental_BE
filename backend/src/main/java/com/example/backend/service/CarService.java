package com.example.backend.service;

import com.example.backend.service.dto.CarDTO;
import com.example.backend.service.dto.request.CarPageRequestDTO;
import org.springframework.data.domain.Page;

import java.util.List;


public interface CarService {
    Long countAll();
    Long countByBrandId(Long brandId);
    Long countById(String id);
    List<CarDTO> findAll();
    Page<CarDTO> findAllWithPagination(CarPageRequestDTO carPageRequestDTO);
    Page<CarDTO> findByIdWithPaginationAndFilter(CarPageRequestDTO carPageRequestDTO, String id);
    Page<CarDTO> findByBrandIdWithPagination(CarPageRequestDTO carPageRequestDTO, Long brandId);
}
