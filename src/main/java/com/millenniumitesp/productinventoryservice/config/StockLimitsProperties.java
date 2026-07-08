package com.millenniumitesp.productinventoryservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "inventory.stock")
public class StockLimitsProperties {

    private int minLimit;
    private int maxLimit;

    public int getMinLimit() { return minLimit; }
    public void setMinLimit(int minLimit) { this.minLimit = minLimit; }
    public int getMaxLimit() { return maxLimit; }
    public void setMaxLimit(int maxLimit) { this.maxLimit = maxLimit; }
}