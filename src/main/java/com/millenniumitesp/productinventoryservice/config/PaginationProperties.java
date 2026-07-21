package com.millenniumitesp.productinventoryservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "inventory.pagination")
public class PaginationProperties {

    private int defaultSize;

}