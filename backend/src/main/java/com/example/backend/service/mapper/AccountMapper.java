package com.example.backend.service.mapper;

import com.example.backend.entity.AccountEntity;
import com.example.backend.service.dto.AccountDTO;
import org.mapstruct.Mapper;

@Mapper(config = DefaultConfigMapper.class)
public interface AccountMapper extends EntityMapper<AccountDTO, AccountEntity> {
}
