package com.example.backend.service;

import com.example.backend.entity.AccountEntity;
import com.example.backend.entity.AccountPrincipal;
import com.example.backend.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements UserDetailsService {
    private final AccountRepository accountRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AccountEntity account = accountRepository.findByUsername(username);
        if (account == null){
            throw new UsernameNotFoundException("User not found");
        }
        return new AccountPrincipal(account);
    }
}
