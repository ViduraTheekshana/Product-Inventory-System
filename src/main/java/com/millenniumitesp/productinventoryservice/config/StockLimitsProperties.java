package com.millenniumitesp.productinventoryservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "inventory.stock")
public class StockLimitsProperties {

    private int minLimit;
    private int maxLimit;

}