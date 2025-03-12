package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class AccountPrincipal implements UserDetails {
    private AccountEntity accountEntity;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(accountEntity.getAccountRole().toString()));
    }

    @Override
    public String getPassword() {
        return accountEntity.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return accountEntity.getUsername();
    }
    @Override
    public boolean isEnabled() {
        return accountEntity.isEnabled();
    }
}
