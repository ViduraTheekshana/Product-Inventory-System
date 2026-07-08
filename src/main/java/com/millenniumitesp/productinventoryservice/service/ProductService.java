package com.millenniumitesp.productinventoryservice.service;

import com.millenniumitesp.productinventoryservice.config.StockLimitsProperties;
import com.millenniumitesp.productinventoryservice.dto.CreateProductRequest;
import com.millenniumitesp.productinventoryservice.dto.ProductResponse;
import com.millenniumitesp.productinventoryservice.dto.UpdateStockPriceRequest;
import com.millenniumitesp.productinventoryservice.entity.Product;
import com.millenniumitesp.productinventoryservice.entity.ProductArchive;
import com.millenniumitesp.productinventoryservice.exception.DuplicateSkuException;
import com.millenniumitesp.productinventoryservice.exception.ProductNotFoundException;
import com.millenniumitesp.productinventoryservice.exception.StockLimitExceededException;
import com.millenniumitesp.productinventoryservice.repository.ProductArchiveRepository;
import com.millenniumitesp.productinventoryservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductArchiveRepository productArchiveRepository;
    private final StockLimitsProperties stockLimits;

    public ProductService(ProductRepository productRepository,
                          ProductArchiveRepository productArchiveRepository,
                          StockLimitsProperties stockLimits) {
        this.productRepository = productRepository;
        this.productArchiveRepository = productArchiveRepository;
        this.stockLimits = stockLimits;
    }

    @Transactional(readOnly = true)//hibernate skip the tracking whether entity change
    public ProductResponse getById(Long id) {
        return productRepository.findById(id)
                .map(ProductResponse::fromEntity)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateSkuException(request.sku());
        }
        validateStockWithinLimits(request.stockQuantity());

        Product product = Product.builder()
                .name(request.name())
                .sku(request.sku())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .build();

        Product saved = productRepository.save(product);
        return ProductResponse.fromEntity(saved);
    }

    @Transactional
    public ProductResponse updatePriceAndStock(Long id, UpdateStockPriceRequest request) {
        Product product = findProductOrThrow(id);

        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.stockQuantity() != null) {
            validateStockWithinLimits(request.stockQuantity());
            product.setStockQuantity(request.stockQuantity());
        }
        return ProductResponse.fromEntity(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = findProductOrThrow(id);
        productArchiveRepository.save(ProductArchive.fromProduct(product));
        productRepository.delete(product);
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private void validateStockWithinLimits(int stockQuantity) {
        int min = stockLimits.getMinLimit();
        int max = stockLimits.getMaxLimit();
        if (stockQuantity < min || stockQuantity > max) {
            throw new StockLimitExceededException(stockQuantity, min, max);
        }
    }
}