package com.example.backend.init;

import com.example.backend.entity.AccountEntity;
import com.example.backend.entity.enums.AccountRole;
import com.example.backend.repository.AccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class AccountInit implements CommandLineRunner {
    AccountRepository accountRepository;
    @Override
    public void run(String... args) throws Exception {
        if (accountRepository.count() > 0)
        {
            log.info("Accounts already exist");
        }
        else {
            log.info("No accounts found, creating new account");
            accountRepository.save(
                    AccountEntity.builder()
                            .username("test")
                            .passwordHash(new BCryptPasswordEncoder(12).encode("123456"))
                            .accountRole(AccountRole.USER)
                            .gender(1)
                            .enabled(true)
                            .email("user@gmail.com")
                            .displayName("Test User")
                            .build()
            );
            log.info("User account created successfully");

            accountRepository.save(
                    AccountEntity.builder()
                            .username("employee")
                            .passwordHash(new BCryptPasswordEncoder(12).encode("123456"))
                            .gender(1)
                            .accountRole(AccountRole.EMPLOYEE)
                            .enabled(true)
                            .email("employee@gmail.com")
                            .displayName("Employee")
                            .build());
            log.info("Employee account created successfully");

            accountRepository.save(
                    AccountEntity.builder()
                            .username("admin")
                            .passwordHash(new BCryptPasswordEncoder(12).encode("123456"))
                            .gender(1)
                            .accountRole(AccountRole.ADMIN)
                            .enabled(true)
                            .email("admin@gmail.com")
                            .displayName("Admin")
                            .build());
            log.info("Admin account created successfully");
        }
    }
}
