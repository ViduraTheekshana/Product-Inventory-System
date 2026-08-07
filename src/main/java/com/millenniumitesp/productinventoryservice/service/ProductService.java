package com.millenniumitesp.productinventoryservice.service;

import com.millenniumitesp.productinventoryservice.config.StockLimitsProperties;
import com.millenniumitesp.productinventoryservice.dto.CreateProductRequest;
import com.millenniumitesp.productinventoryservice.dto.ProductResponse;
import com.millenniumitesp.productinventoryservice.dto.UpdateStockPriceRequest;
import com.millenniumitesp.productinventoryservice.entity.Product;
import com.millenniumitesp.productinventoryservice.enums.ProductStatus;
import com.millenniumitesp.productinventoryservice.exception.ProductExceptions;
import com.millenniumitesp.productinventoryservice.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;
    private final StockLimitsProperties stockLimits;

    public ProductService(ProductRepository productRepository, StockLimitsProperties stockLimits) {
        this.productRepository = productRepository;
        this.stockLimits = stockLimits;
    }

    /**
     * retrieve 1 record
     */
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return productRepository.findByIdAndStatusNot(id, ProductStatus.DELETED)
                .map(ProductResponse::fromEntity)
                .orElseThrow(() -> new ProductExceptions.NotFound(id));
    }

    /**
     * get pageable records
     */
    public Page<ProductResponse> getAll(Pageable pageable) {
        Pageable safePageable = pageable.getPageSize() > MAX_PAGE_SIZE
                ? PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort())
                : pageable;

        return productRepository.findAllByStatusNot(ProductStatus.DELETED, safePageable)
                .map(ProductResponse::fromEntity);
    }

    /**
     * create a new record
     */
    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new ProductExceptions.DuplicateSku(request.sku());
        }
        validateStockWithinLimits(request.stockQuantity());

        Product product = Product.builder()
                .name(request.name())
                .sku(request.sku())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .status(ProductStatus.ACTIVE)
                .build();

        Product saved = productRepository.save(product);
        return ProductResponse.fromEntity(saved);
    }

    //update price and stock
    @Transactional
    public ProductResponse updatePriceAndStock(Long id, UpdateStockPriceRequest request) {
        Product product = findActiveOrThrow(id);

        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.stockQuantity() != null) {
            validateStockWithinLimits(request.stockQuantity());
            product.setStockQuantity(request.stockQuantity());
        }
        return ProductResponse.fromEntity(product);
    }

    /**
     * update status only
     */
    @Transactional
    public ProductResponse updateStatus(Long id, ProductStatus newStatus) {
        if (newStatus == ProductStatus.DELETED) {
            throw new ProductExceptions.InvalidStatusTransition(
                    "Use the DELETE endpoint to remove a product, not this one.");
        }

        Product product = findActiveOrThrow(id);
        product.setStatus(newStatus);
        return ProductResponse.fromEntity(product);
    }

    /**
     Delete Record by setting status to DELETED (Soft Delete)
     */
    @Transactional
    public void delete(Long id) {
        Product product = findActiveOrThrow(id);
        product.setStatus(ProductStatus.DELETED);
    }

    private Product findActiveOrThrow(Long id) {
        return productRepository.findByIdAndStatusNot(id, ProductStatus.DELETED)
                .orElseThrow(() -> new ProductExceptions.NotFound(id));
    }

    private void validateStockWithinLimits(int stockQuantity) {
        int min = stockLimits.getMinLimit();
        int max = stockLimits.getMaxLimit();
        if (stockQuantity < min || stockQuantity > max) {
            throw new ProductExceptions.StockLimitExceeded(stockQuantity, min, max);
        }
    }
}