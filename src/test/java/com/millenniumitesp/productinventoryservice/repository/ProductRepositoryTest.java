package com.millenniumitesp.productinventoryservice.repository;

import com.millenniumitesp.productinventoryservice.entity.Product;
import com.millenniumitesp.productinventoryservice.enums.ProductStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
class ProductRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    // Constructor injection, exactly the same pattern already used in
    // ProductService/AuthService - Spring's SpringExtension supports
    // this for test classes too, no @Autowired needed at all.
    private final ProductRepository productRepository;

    ProductRepositoryTest(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Test
    void existsBySku_shouldReturnTrue_whenSkuExists() {
        Product product = Product.builder()
                .name("Test Widget").sku("TW-1")
                .price(new BigDecimal("10.00")).stockQuantity(5)
                .status(ProductStatus.ACTIVE)
                .build();
        productRepository.save(product);

        assertTrue(productRepository.existsBySku("TW-1"));
    }

    @Test
    void findByIdAndStatusNot_shouldReturnEmpty_whenProductIsDeleted() {
        Product product = productRepository.save(Product.builder()
                .name("Test").sku("T-1").price(BigDecimal.TEN)
                .stockQuantity(1).status(ProductStatus.DELETED)
                .build());

        Optional<Product> result = productRepository
                .findByIdAndStatusNot(product.getId(), ProductStatus.DELETED);

        assertTrue(result.isEmpty());
    }

    @Test
    void save_shouldThrowConstraintViolation_whenNameIsNull() {
        Product invalid = Product.builder()
                .sku("T-2").price(BigDecimal.TEN).stockQuantity(1)
                .status(ProductStatus.ACTIVE)
                .build();

        assertThrows(DataIntegrityViolationException.class,
                () -> productRepository.saveAndFlush(invalid));
    }
}