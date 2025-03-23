package com.example.backend.service;

import com.example.backend.entity.AccountEntity;
import com.example.backend.entity.AccountPrincipal;
import com.example.backend.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AccountEntity> account = accountRepository.findByUsername(username);
        if (account.isEmpty()){
            throw new UsernameNotFoundException("User not found");
        }
        return new AccountPrincipal(account.get());
    }
}
