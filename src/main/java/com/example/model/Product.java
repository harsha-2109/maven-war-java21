package com.example.model;

/**
 * Product record using Java 21 Records feature.
 */
public record Product(
        Long id,
        String name,
        String description,
        double price,
        int stock
) {
    // Compact constructor with validation
    public Product {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stock must be non-negative");
        }
    }

    /** Returns a new Product with updated stock */
    public Product withStock(int newStock) {
        return new Product(id, name, description, price, newStock);
    }

    /** Returns a new Product with a new price */
    public Product withPrice(double newPrice) {
        return new Product(id, name, description, newPrice, stock);
    }

    public boolean isAvailable() {
        return stock > 0;
    }
}
