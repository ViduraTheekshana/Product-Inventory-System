package com.millenniumitesp.productinventoryservice.exception;

/**
 * Groups all Product-domain exceptions under one file as nested static
 * classes, instead of one file per exception. Named ProductExceptions
 * (plural) rather than ProductException specifically so static analysis
 * tools don't expect this outer container class itself to extend
 * Throwable - only the nested classes are actual exceptions.
 */
public class ProductExceptions {

    private ProductExceptions() {}

    public static class NotFound extends RuntimeException {
        public NotFound(Long id) {
            super("Product not found with id: " + id);
        }
    }

    public static class DuplicateSku extends RuntimeException {
        public DuplicateSku(String sku) {
            super("A product with sku '" + sku + "' already exists");
        }
    }

    public static class StockLimitExceeded extends RuntimeException {
        public StockLimitExceeded(int requested, int min, int max) {
            super("stockQuantity " + requested + " is outside the allowed range [" + min + ", " + max + "]");
        }
    }

    public static class InvalidStatusTransition extends RuntimeException {
        public InvalidStatusTransition(String message) {
            super(message);
        }
    }
}