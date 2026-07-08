package com.millenniumitesp.productinventoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ProductInventoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductInventoryServiceApplication.class, args);
    }
}