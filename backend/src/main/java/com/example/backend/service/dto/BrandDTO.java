package com.example.backend.service.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class BrandDTO {
    String name;
    MultipartFile logo;
}
