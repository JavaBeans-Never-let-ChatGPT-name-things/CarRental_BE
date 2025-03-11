package com.example.backend.service;

import com.example.backend.entity.AccountEntity;
import com.example.backend.service.dto.AccountDTO;

public interface AccountService {
    String verify(String username, String password);
    AccountEntity register(AccountDTO accountDTO);

}
