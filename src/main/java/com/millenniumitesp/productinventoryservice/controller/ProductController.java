package com.millenniumitesp.productinventoryservice.controller;

import com.millenniumitesp.productinventoryservice.config.PaginationProperties;
import com.millenniumitesp.productinventoryservice.dto.CreateProductRequest;
import com.millenniumitesp.productinventoryservice.dto.ProductResponse;
import com.millenniumitesp.productinventoryservice.dto.UpdateProductStatusRequest;
import com.millenniumitesp.productinventoryservice.dto.UpdateStockPriceRequest;
import com.millenniumitesp.productinventoryservice.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/products")
@Validated
public class ProductController implements ProductApi {

    private final ProductService productService;
    private final PaginationProperties paginationProperties;

    public ProductController(ProductService productService, PaginationProperties paginationProperties) {
        this.productService = productService;
        this.paginationProperties = paginationProperties;
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        ProductResponse product = productService.getById(id);
        return ResponseEntity.ok(product);
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(Pageable pageable) {
        Pageable effectivePageable = pageable.getPageSize() == 20 && pageable.getSort().isUnsorted()
                ? PageRequest.of(pageable.getPageNumber(), paginationProperties.getDefaultSize())
                : pageable;

        Page<ProductResponse> products = productService.getAll(effectivePageable);
        return ResponseEntity.ok(products);
    }

    @Override
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody CreateProductRequest request) {
        ProductResponse product = productService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(product.id())
                .toUri();
        return ResponseEntity.created(location).body(product);
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> updatePriceAndStock(
            @PathVariable Long id,
            @RequestBody UpdateStockPriceRequest request
    ) {
        ProductResponse product = productService.updatePriceAndStock(id, request);
        return ResponseEntity.ok(product);
    }

    @Override
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateProductStatusRequest request
    ) {
        ProductResponse product = productService.updateStatus(id, request.status());
        return ResponseEntity.ok(product);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}