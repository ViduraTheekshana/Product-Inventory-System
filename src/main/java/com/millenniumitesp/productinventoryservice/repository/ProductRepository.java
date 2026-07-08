package com.millenniumitesp.productinventoryservice.repository;

import com.millenniumitesp.productinventoryservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);
}