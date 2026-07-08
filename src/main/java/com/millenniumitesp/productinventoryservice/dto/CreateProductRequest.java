package com.millenniumitesp.productinventoryservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(

        @NotBlank(message = "name is required")
        @Size(max = 200, message = "name must be at most 200 characters")
        String name,

        @NotBlank(message = "sku is required")
        @Size(max = 64, message = "sku must be at most 64 characters")
        String sku,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "price may have at most 2 decimal places")
        BigDecimal price,

        @NotNull(message = "stockQuantity is required")
        @PositiveOrZero(message = "stockQuantity cannot be negative")
        Integer stockQuantity

) {}