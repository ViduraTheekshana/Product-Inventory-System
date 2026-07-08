package com.millenniumitesp.productinventoryservice.dto;

import com.millenniumitesp.productinventoryservice.entity.Product;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record ProductResponse(
        Long id,
        String name,
        String sku,
        BigDecimal price,
        Integer stockQuantity,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ProductResponse fromEntity(Product product) {
        return new ProductResponse(
                product.getId(), product.getName(), product.getSku(),
                product.getPrice(), product.getStockQuantity(),
                product.getCreatedAt(), product.getUpdatedAt()
        );
    }
}