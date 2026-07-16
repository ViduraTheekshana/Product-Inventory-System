package com.millenniumitesp.productinventoryservice.dto;

import com.millenniumitesp.productinventoryservice.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateProductStatusRequest(
        @NotNull(message = "status is required")
        ProductStatus status
) {}