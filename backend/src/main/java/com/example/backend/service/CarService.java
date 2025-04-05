package com.example.backend.service;

import com.example.backend.service.dto.CarDTO;
import com.example.backend.service.dto.request.CarPageRequestDTO;
import org.springframework.data.domain.Page;

import java.util.List;


public interface CarService {
    List<CarDTO> findAll();
    Page<CarDTO> findByAllWithPagination(CarPageRequestDTO carPageRequestDTO);
    Page<CarDTO> findByAllWithPaginationAndFilter(CarPageRequestDTO carPageRequestDTO, String id);
}
