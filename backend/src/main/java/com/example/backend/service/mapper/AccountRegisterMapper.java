package com.example.backend.service.mapper;

import com.example.backend.entity.AccountEntity;
import com.example.backend.service.dto.request.RegisterRequest;
import org.mapstruct.Mapper;

@Mapper(config = DefaultConfigMapper.class)
public interface AccountRegisterMapper extends EntityMapper<RegisterRequest, AccountEntity> {
}
