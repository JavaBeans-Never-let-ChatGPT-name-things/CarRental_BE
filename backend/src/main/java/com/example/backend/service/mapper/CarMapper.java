package com.example.backend.service.mapper;

import com.example.backend.entity.CarEntity;
import com.example.backend.service.dto.CarDTO;
import org.mapstruct.Mapper;

@Mapper(config = DefaultConfigMapper.class)
public interface CarMapper extends EntityMapper<CarDTO, CarEntity> {
}
