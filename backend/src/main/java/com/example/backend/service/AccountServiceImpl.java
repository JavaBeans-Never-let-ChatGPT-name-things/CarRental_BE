package com.example.backend.service;

import com.example.backend.entity.AccountEntity;
import com.example.backend.entity.enums.AccountRole;
import com.example.backend.entity.enums.AccountStatus;
import com.example.backend.repository.AccountRepository;
import com.example.backend.service.dto.AccountDTO;
import com.example.backend.service.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    private final AccountMapper accountMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    @Override
    public String verify(String username, String password) {
        AccountEntity account = accountRepository.findByUsername(username);
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        if (authentication.isAuthenticated())
            return jwtService.generateToken(account);
        else
            return "User is not authenticated";
    }

    @Override
    public AccountEntity register(AccountDTO accountDTO) {
        accountDTO.setPassword(passwordEncoder.encode(accountDTO.getPassword()));
        AccountEntity account = accountMapper.toEntity(accountDTO);
        account.setAccountStatus(AccountStatus.PENDING);
        account.setAccountRole(AccountRole.USER);
        return accountRepository.save(account);
    }
}
