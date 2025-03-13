package com.example.backend.service.dto.request;


public record VerifyUserDTO (String email,
                             String verificationCode) {

}
