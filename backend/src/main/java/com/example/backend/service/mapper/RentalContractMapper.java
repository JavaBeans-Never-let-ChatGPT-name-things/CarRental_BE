package com.example.backend.service.mapper;

import com.example.backend.entity.RentalContractEntity;
import com.example.backend.service.dto.RentalContractDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = DefaultConfigMapper.class)
public interface RentalContractMapper extends EntityMapper<RentalContractDTO, RentalContractEntity> {
    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "car.carImageUrl", target = "carImageUrl")
    @Mapping(source = "car.createdDate", target = "contractDate")
    RentalContractDTO toDto(RentalContractEntity contract);

    List<RentalContractDTO> toDto(List<RentalContractEntity> contracts);
}
