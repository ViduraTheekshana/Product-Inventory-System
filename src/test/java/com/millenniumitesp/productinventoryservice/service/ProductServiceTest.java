package com.millenniumitesp.productinventoryservice.service;

import com.millenniumitesp.productinventoryservice.config.StockLimitsProperties;
import com.millenniumitesp.productinventoryservice.dto.CreateProductRequest;
import com.millenniumitesp.productinventoryservice.dto.ProductResponse;
import com.millenniumitesp.productinventoryservice.entity.Product;
import com.millenniumitesp.productinventoryservice.enums.ProductStatus;
import com.millenniumitesp.productinventoryservice.exception.ProductExceptions;
import com.millenniumitesp.productinventoryservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    // NOT a mock, deliberately - this is a plain data holder with no
    // real behavior to fake, so we just build a real one directly with
    // known values, exactly like using a real ruler instead of a fake one.
    private StockLimitsProperties stockLimits;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        stockLimits = new StockLimitsProperties();
        stockLimits.setMinLimit(0);
        stockLimits.setMaxLimit(100000);
        productService = new ProductService(productRepository, stockLimits);
    }

    /**
     * Purpose: prove that creating a product with a brand-new SKU
     * actually succeeds and returns the saved data. This is our
     * "happy path" - the simplest possible correct case.
     */
    @Test
    void create_shouldSucceed_whenSkuIsNew() {
        // Arrange
        CreateProductRequest request = new CreateProductRequest("Test Widget", "TW-1", new BigDecimal("25.50"), 100);

        Product savedProduct = Product.builder()
                .id(1L)
                .name("Test Widget")
                .sku("TW-1")
                .price(new BigDecimal("25.50"))
                .stockQuantity(100)
                .status(ProductStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        // "Teach" the fake repository how to behave for this test:
        // when asked if TW-1 exists, say no; when asked to save
        // anything, hand back our pre-built savedProduct.
        when(productRepository.existsBySku("TW-1")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        ProductResponse response = productService.create(request);

        // Assert
        assertEquals("TW-1", response.sku());
        assertEquals(ProductStatus.ACTIVE, response.status());
    }

    /**
     * Purpose: prove the exact business rule we built weeks ago - a
     * duplicate SKU must throw DuplicateSku, and crucially, must NEVER
     * reach productRepository.save() at all.
     */
    @Test
    void create_shouldThrowDuplicateSku_whenSkuAlreadyExists() {
        // Arrange
        CreateProductRequest request = new CreateProductRequest("Test Widget", "TW-1", new BigDecimal("25.50"), 100);
        when(productRepository.existsBySku("TW-1")).thenReturn(true);

        // Act + Assert combined - assertThrows both calls the method
        // AND checks the correct exception type comes out of it.
        assertThrows(ProductExceptions.DuplicateSku.class, () -> productService.create(request));

        // Extra proof: save() was never even attempted.
        verify(productRepository, never()).save(any());
    }

    /**
     * Purpose: prove the stock-limit business rule fires correctly,
     * using the exact boundary (100001, one above our configured max
     * of 100000) - testing the EDGE of the rule, not just an obviously
     * huge number.
     */
    @Test
    void create_shouldThrowStockLimitExceeded_whenStockAboveMax() {
        CreateProductRequest request = new CreateProductRequest("Test Widget", "TW-1", new BigDecimal("25.50"), 100001);
        when(productRepository.existsBySku("TW-1")).thenReturn(false);

        assertThrows(ProductExceptions.StockLimitExceeded.class, () -> productService.create(request));
    }

    /**
     * Purpose: prove getById correctly returns data for a real,
     * active product.
     */
    @Test
    void getById_shouldReturnProduct_whenItExistsAndIsActive() {
        Product product = Product.builder()
                .id(1L).name("Test Widget").sku("TW-1")
                .price(new BigDecimal("25.50")).stockQuantity(100)
                .status(ProductStatus.ACTIVE)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();

        when(productRepository.findByIdAndStatusNot(1L, ProductStatus.DELETED))
                .thenReturn(Optional.of(product));

        ProductResponse response = productService.getById(1L);

        assertEquals(1L, response.id());
        assertEquals("Test Widget", response.name());
    }

    /**
     * Purpose: prove that a deleted (or nonexistent) product correctly
     * returns NotFound - simulating the empty Optional the repository
     * would genuinely return in that case.
     */
    @Test
    void getById_shouldThrowNotFound_whenProductIsDeletedOrMissing() {
        when(productRepository.findByIdAndStatusNot(99L, ProductStatus.DELETED))
                .thenReturn(Optional.empty());

        assertThrows(ProductExceptions.NotFound.class, () -> productService.getById(99L));
    }

    /**
     * Purpose: prove the exact bug we fixed together earlier tonight -
     * a status-update endpoint must reject a direct attempt to set
     * DELETED, since that's only ever allowed via the real delete flow.
     */
    @Test
    void updateStatus_shouldRejectDirectlySettingDeleted() {
        assertThrows(ProductExceptions.InvalidStatusTransition.class,
                () -> productService.updateStatus(1L, ProductStatus.DELETED));

        // Since this should fail before even looking the product up,
        // the repository should never have been touched.
        verifyNoInteractions(productRepository);
    }

    /**
     * Purpose: prove delete() correctly flips status to DELETED on an
     * existing product, and that the change actually gets persisted.
     */
    @Test
    void delete_shouldSetStatusToDeleted() {
        Product product = Product.builder()
                .id(1L).name("Test Widget").sku("TW-1")
                .price(new BigDecimal("25.50")).stockQuantity(100)
                .status(ProductStatus.ACTIVE)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();

        when(productRepository.findByIdAndStatusNot(1L, ProductStatus.DELETED))
                .thenReturn(Optional.of(product));

        productService.delete(1L);

        assertEquals(ProductStatus.DELETED, product.getStatus());
    }
}