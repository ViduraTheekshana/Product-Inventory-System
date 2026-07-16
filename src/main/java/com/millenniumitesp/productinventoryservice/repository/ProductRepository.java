package com.millenniumitesp.productinventoryservice.repository;

import com.millenniumitesp.productinventoryservice.entity.Product;
import com.millenniumitesp.productinventoryservice.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySkuAndStatusNot(String sku, ProductStatus excludedStatus);

    Optional<Product> findByIdAndStatusNot(Long id, ProductStatus excludedStatus);

    Page<Product> findAllByStatusNot(ProductStatus excludedStatus, Pageable pageable);
}