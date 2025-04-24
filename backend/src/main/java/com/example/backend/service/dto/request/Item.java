package com.example.backend.service.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Item {
    String name;
    int quantity;
    double price;
    String description;
}
