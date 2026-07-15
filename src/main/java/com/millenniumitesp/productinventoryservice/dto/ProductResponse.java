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
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}