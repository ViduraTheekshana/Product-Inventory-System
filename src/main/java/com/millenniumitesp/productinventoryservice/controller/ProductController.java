package com.millenniumitesp.productinventoryservice.controller;

import com.millenniumitesp.productinventoryservice.dto.CreateProductRequest;
import com.millenniumitesp.productinventoryservice.dto.ProductResponse;
import com.millenniumitesp.productinventoryservice.dto.UpdateStockPriceRequest;
import com.millenniumitesp.productinventoryservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 1. Get Product
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    // 2. Create Product
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse created = productService.create(request);
        URI location = URI.create("/api/v1/products/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    // 3. Update Product (Partial - price and stock only)
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> updatePriceAndStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockPriceRequest request
    ) {
        return ResponseEntity.ok(productService.updatePriceAndStock(id, request));
    }

    // 4. Delete Product
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}