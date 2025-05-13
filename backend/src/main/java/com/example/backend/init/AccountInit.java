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

import java.util.ArrayList;
import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class AccountInit implements CommandLineRunner {
    AccountRepository accountRepository;

    @Override
    public void run(String... args) throws Exception {
        if (accountRepository.count() > 0) {
            log.info("Accounts already exist");
            return;
        }

        log.info("No accounts found, creating new accounts");

        List<AccountEntity> accounts = new ArrayList<>();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        // Default accounts
        accounts.add(AccountEntity.builder()
                .username("test")
                .passwordHash(encoder.encode("123456"))
                .accountRole(AccountRole.USER)
                .gender(1)
                .enabled(true)
                .email("user@gmail.com")
                .displayName("Test User")
                .build());

        accounts.add(AccountEntity.builder()
                .username("employee")
                .passwordHash(encoder.encode("123456"))
                .gender(1)
                .accountRole(AccountRole.EMPLOYEE)
                .enabled(true)
                .email("employee@gmail.com")
                .displayName("Employee")
                .build());

        accounts.add(AccountEntity.builder()
                .username("admin")
                .passwordHash(encoder.encode("123456"))
                .gender(1)
                .accountRole(AccountRole.ADMIN)
                .enabled(true)
                .email("admin@gmail.com")
                .displayName("Admin")
                .build());
        List<String> userNames = List.of(
                "alex.miller",
                "sophie.williams",
                "jake.turner",
                "mia.johnson",
                "lucas.thompson",
                "emma.brown",
                "noah.davis",
                "olivia.moore",
                "liam.taylor",
                "ava.anderson"
        );
        for (int i = 1; i <= 10; i++) {
            accounts.add(AccountEntity.builder()
                    .username(userNames.get(i - 1))
                    .passwordHash(encoder.encode("123456"))
                    .accountRole(AccountRole.USER)
                    .gender(i % 2)
                    .enabled(true)
                    .email(userNames.get(i - 1).replace(".", "") + "@gmail.com")
                    .displayName(userNames.get(i-1).replace(".", " "))
                    .build());
        }

        accountRepository.saveAll(accounts);
        log.info("Accounts created successfully");
    }
}

