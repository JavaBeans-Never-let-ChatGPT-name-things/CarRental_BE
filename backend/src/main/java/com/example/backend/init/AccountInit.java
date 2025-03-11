package com.example.backend.init;

import com.example.backend.entity.AccountEntity;
import com.example.backend.entity.enums.AccountRole;
import com.example.backend.entity.enums.AccountStatus;
import com.example.backend.repository.AccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
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
            return;
        }
        log.info("No accounts found, creating new account");
        accountRepository.save(
                AccountEntity.builder()
                        .username("test")
                        .passwordHash(new BCryptPasswordEncoder(12).encode("123456"))
                        .accountRole(AccountRole.USER)
                        .accountStatus(AccountStatus.ACTIVE)
                        .email("testingPurposeOnly@gmail.com")
                        .displayName("Test User")
                        .build()
        );
    }
}
