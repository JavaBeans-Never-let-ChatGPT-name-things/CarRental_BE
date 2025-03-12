package com.example.backend.service.dto;


public record VerifyUserDTO (String email,
                             String verificationCode) {

}
