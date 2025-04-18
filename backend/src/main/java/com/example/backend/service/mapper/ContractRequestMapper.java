package com.example.backend.service.mapper;

import com.example.backend.entity.RentalContractEntity;
import com.example.backend.service.dto.request.ContractRequestDTO;
import org.mapstruct.Mapper;

@Mapper(config = DefaultConfigMapper.class)
public interface ContractRequestMapper extends EntityMapper<ContractRequestDTO, RentalContractEntity>{
}
