package com.millenniumitesp.productinventoryservice.exception;

public class StockLimitExceededException extends RuntimeException {
    public StockLimitExceededException(int requested, int min, int max) {
        super("stockQuantity " + requested + " is outside the allowed range [" + min + ", " + max + "]");
    }
}