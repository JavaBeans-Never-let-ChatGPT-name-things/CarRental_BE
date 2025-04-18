package com.example.backend.service.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UpdateUserRequestDTO {
    String email;
    String displayName;
    int gender;
    String address;
    String phoneNumber;
    MultipartFile avatar;
}
