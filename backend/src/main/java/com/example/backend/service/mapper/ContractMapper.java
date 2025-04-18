package com.example.backend.service.mapper;

import com.example.backend.entity.RentalContractEntity;
import com.example.backend.service.dto.request.ContractDTO;
import org.mapstruct.Mapper;

@Mapper(config = DefaultConfigMapper.class)
public interface ContractMapper extends EntityMapper<ContractDTO, RentalContractEntity>{
}
