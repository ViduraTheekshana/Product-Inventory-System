package com.millenniumitesp.productinventoryservice.repository;

import com.millenniumitesp.productinventoryservice.entity.Product;
import com.millenniumitesp.productinventoryservice.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Checks ALL rows, including DELETED - a SKU is now permanently
    // reserved once used, even after the product is deleted.
    boolean existsBySku(String sku);

    Optional<Product> findByIdAndStatusNot(Long id, ProductStatus excludedStatus);

    Page<Product> findAllByStatusNot(ProductStatus excludedStatus, Pageable pageable);
}