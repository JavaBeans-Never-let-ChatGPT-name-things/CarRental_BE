package com.example.backend.service;

import com.example.backend.entity.AccountEntity;
import com.example.backend.entity.enums.AccountRole;
import com.example.backend.repository.AccountRepository;
import com.example.backend.service.dto.request.ForgotPasswordRequest;
import com.example.backend.service.dto.request.VerifyUserDTO;
import com.example.backend.service.dto.request.RegisterRequest;
import com.example.backend.service.mapper.AccountRegisterMapper;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    private final AccountRegisterMapper accountMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Override
    public String verify(String username, String password) {
        AccountEntity account = accountRepository.findByUsername(username);
        if (!account.isEnabled())
        {
            throw new RuntimeException("Account is not enabled");
        }
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        if (authentication.isAuthenticated())
            return jwtService.generateToken(account);
        else
            return "Account is not authenticated";
    }

    @Override
    public AccountEntity register(RegisterRequest request) {
        AccountEntity account = accountMapper.toEntity(request);
        account.setVerificationCode(generateVerificationCode());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        account.setEnabled(false);
        account.setDisplayName(request.getDisplayName());
        account.setAccountRole(AccountRole.USER);
        sendVerificationEmail(account, "Account Verification");
        return accountRepository.save(account);
    }

    @Override
    public void verifyUser(VerifyUserDTO input) {
        AccountEntity account = accountRepository.findByEmail(input.email());
        if (account != null)
        {
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
    }

    @Override
    public void resendVerificationCode(String email){
        AccountEntity account = accountRepository.findByEmail(email);
        if (account != null)
        {
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
        AccountEntity account = accountRepository.findByEmail(email);
        if (account != null)
        {
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
        AccountEntity account = accountRepository.findByEmail(request.email());
        if (account != null)
        {
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
        AccountEntity account = accountRepository.findByEmail(email);
        if (account != null)
        {
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
           e.printStackTrace();
        }
    }
    private String generateVerificationCode(){
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
