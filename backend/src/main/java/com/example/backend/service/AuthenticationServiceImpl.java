package com.example.backend.service;

import com.example.backend.entity.AccountEntity;
import com.example.backend.entity.LogoutToken;
import com.example.backend.entity.enums.AccountRole;
import com.example.backend.repository.AccountRepository;
import com.example.backend.repository.LogoutTokenRepository;
import com.example.backend.service.dto.request.ForgotPasswordRequest;
import com.example.backend.service.dto.request.VerifyUserDTO;
import com.example.backend.service.dto.request.RegisterRequest;
import com.example.backend.service.dto.response.TokenResponse;
import com.example.backend.service.mapper.AccountRegisterMapper;
import jakarta.mail.MessagingException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthenticationServiceImpl implements AuthenticationService {
    AccountRepository accountRepository;
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
    Validator validator;
    AccountRegisterMapper accountMapper;
    AuthenticationManager authenticationManager;
    JwtService jwtService;
    EmailService emailService;
    LogoutTokenRepository logoutTokenRepository;
    @Override
    public TokenResponse verify(String username, String password) {
        Optional<AccountEntity> accountOpt = accountRepository.findByUsername(username);
        if (accountOpt.isEmpty())
        {
            throw new RuntimeException("Account not found");
        }
        AccountEntity account = accountOpt.get();
        if (!account.isEnabled())
        {
            throw new RuntimeException("Account is not enabled");
        }
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        if (authentication.isAuthenticated())
            return TokenResponse.builder()
                    .role(account.getAccountRole())
                    .accessToken(jwtService.generateAccessToken(account))
                    .refreshToken(jwtService.generateRefreshToken(account))
                    .build();
        else
            return TokenResponse.builder()
                    .accessToken(null)
                    .refreshToken(null)
                    .role(null)
                    .build();
    }

    @Override
    public RegisterRequest register(RegisterRequest request) {
        String mail = request.getEmail();
        String username = request.getUsername();
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new RuntimeException(violations.iterator().next().getMessage());
        }
        if (accountRepository.findByUsername(username).isPresent() || accountRepository.findByEmail(mail).isPresent())
        {
            throw new RuntimeException("Account already exists");
        }
        AccountEntity account = accountMapper.toEntity(request);
        account.setVerificationCode(generateVerificationCode());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        account.setEnabled(false);
        account.setDisplayName(request.getDisplayName());
        account.setAccountRole(AccountRole.USER);
        sendVerificationEmail(account, "Account Verification");
        accountRepository.save(account);
        return request;
    }

    @Override
    public TokenResponse verifyUser(VerifyUserDTO input) {
        Optional<AccountEntity> accountOpt = accountRepository.findByEmail(input.email());
        if (accountOpt.isPresent())
        {
            AccountEntity account = accountOpt.get();
            if (account.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now()))
            {
                throw new RuntimeException("Verification code has expired");
            }
            if (account.getVerificationCode().equals(input.verificationCode())){
                account.setEnabled(true);
                account.setVerificationCode(null);
                account.setVerificationCodeExpiresAt(null);
                accountRepository.save(account);

            }
        }
        else
        {
            throw new RuntimeException("Account not found");
        }
        return TokenResponse.builder()
                .role(accountOpt.get().getAccountRole())
                .accessToken(jwtService.generateAccessToken(accountOpt.get()))
                .refreshToken(jwtService.generateRefreshToken(accountOpt.get()))
                .build();
    }

    @Override
    public void resendVerificationCode(String email){
        Optional<AccountEntity> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isPresent())
        {
            AccountEntity account = accountOpt.get();
            if (account.isEnabled())
            {
                throw new RuntimeException("Account is already verified");
            }
            account.setVerificationCode(generateVerificationCode());
            account.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
            sendVerificationEmail(account, "Resend Verification Code");
            accountRepository.save(account);
        }
        else
        {
            throw new RuntimeException("Account not found");
        }
    }

    @Override
    public void sendForgotPasswordEmail(String email) {
        Optional<AccountEntity> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isPresent())
        {
            AccountEntity account = accountOpt.get();
            if (!account.isEnabled())
            {
                throw new RuntimeException("Account is not verified");
            }
            account.setVerificationCode(generateVerificationCode());
            account.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
            sendVerificationEmail(account, "Forgot Password");
            accountRepository.save(account);
        }
        else
        {
            throw new RuntimeException("Account not found");
        }
    }

    @Override
    public void resetPassword(ForgotPasswordRequest request) {
        Optional<AccountEntity> accountOpt = accountRepository.findByEmail(request.email());
        if (accountOpt.isPresent())
        {
            AccountEntity account = accountOpt.get();
            if (account.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now()))
            {
                throw new RuntimeException("Verification code has expired");
            }
            if (account.getVerificationCode().equals(request.verificationCode()))
            {
                account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
                account.setVerificationCode(null);
                account.setVerificationCodeExpiresAt(null);
                accountRepository.save(account);
            }
        }
        else
        {
            throw new RuntimeException("Account not found");
        }
    }

    @Override
    public void resendForgotPasswordEmail(String email) {
        Optional<AccountEntity> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isPresent())
        {
            AccountEntity account = accountOpt.get();
            account.setVerificationCode(generateVerificationCode());
            account.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
            sendVerificationEmail(account, "Resend Forgot Password");
            accountRepository.save(account);
        }
        else
        {
            throw new RuntimeException("Account not found");
        }
    }

    @Override
    public void logout(String token) {
        if (jwtService.validateRefreshToken(token))
        {
            logoutTokenRepository.save(LogoutToken.builder().token(token).build());
        }
        else
        {
            throw new RuntimeException("Invalid token");
        }
    }

    @Override
    public TokenResponse refreshAccessToken(String refreshToken) {
        if (jwtService.validateRefreshToken(refreshToken) && logoutTokenRepository.findByToken(refreshToken).isEmpty())
        {
            String username = jwtService.extractUserName(refreshToken);
            Optional<AccountEntity> accountOpt = accountRepository.findByUsername(username);
            if (accountOpt.isPresent())
            {
                AccountEntity account = accountOpt.get();
                if (account.isEnabled())
                {
                    return TokenResponse.builder()
                            .role(account.getAccountRole())
                            .accessToken(jwtService.generateAccessToken(account))
                            .build();
                }
                else
                {
                    throw new RuntimeException("Account is not verified");
                }
            }
            else
            {
                throw new RuntimeException("Account not found");
            }
        }
        else
        {
            throw new RuntimeException("Invalid token");
        }
    }

    public void sendVerificationEmail(AccountEntity account, String title) {
        String verificationCode = account.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
        try{
            emailService.sendVerificationEmail(account.getEmail(), title, htmlMessage);
        }
        catch (MessagingException e)
        {
           throw new RuntimeException("Failed to send email due to " + e.getCause());
        }
    }
    private String generateVerificationCode(){
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
