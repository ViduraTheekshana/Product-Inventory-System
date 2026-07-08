package com.millenniumitesp.productinventoryservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record UpdateStockPriceRequest(

        @DecimalMin(value = "0.0", inclusive = false, message = "price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "price may have at most 2 decimal places")
        BigDecimal price,

        @PositiveOrZero(message = "stockQuantity cannot be negative")
        Integer stockQuantity

) {}