package com.millenniumitesp.productinventoryservice.repository;

import com.millenniumitesp.productinventoryservice.entity.ProductArchive;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductArchiveRepository extends JpaRepository<ProductArchive, Long> {
}