package com.example.service;

import com.example.model.ApiResponse;
import com.example.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple in-memory Product service demonstrating Java 21 features.
 */
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private static final ProductService INSTANCE = new ProductService();

    private final Map<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    private ProductService() {
        // Seed with sample data using text blocks (Java 15+) in log messages
        seedData();
    }

    public static ProductService getInstance() {
        return INSTANCE;
    }

    // -----------------------------------------------------------------------
    // CRUD operations
    // -----------------------------------------------------------------------

    public ApiResponse<List<Product>> findAll() {
        var products = List.copyOf(store.values());
        log.info("findAll() → {} products", products.size());
        return new ApiResponse.Success<>(products);
    }

    public ApiResponse<Product> findById(Long id) {
        // Java 21: use Optional + pattern matching
        return Optional.ofNullable(store.get(id))
                .<ApiResponse<Product>>map(ApiResponse.Success::new)
                .orElseGet(() -> new ApiResponse.Failure<>(404,
                        "Product with id %d not found".formatted(id)));
    }

    public ApiResponse<Product> create(Product product) {
        long newId = idSequence.getAndIncrement();
        // Records are immutable; create a new one with the generated id
        var saved = new Product(newId, product.name(), product.description(),
                product.price(), product.stock());
        store.put(newId, saved);
        log.info("Created product: {}", saved);
        return new ApiResponse.Success<>(saved, "Product created");
    }

    public ApiResponse<Product> update(Long id, Product updated) {
        if (!store.containsKey(id)) {
            return new ApiResponse.Failure<>(404,
                    "Product with id %d not found".formatted(id));
        }
        var replacing = new Product(id, updated.name(), updated.description(),
                updated.price(), updated.stock());
        store.put(id, replacing);
        log.info("Updated product: {}", replacing);
        return new ApiResponse.Success<>(replacing, "Product updated");
    }

    public ApiResponse<Void> delete(Long id) {
        if (store.remove(id) == null) {
            return new ApiResponse.Failure<>(404,
                    "Product with id %d not found".formatted(id));
        }
        log.info("Deleted product id={}", id);
        return new ApiResponse.Success<>(null, "Product deleted");
    }

    // -----------------------------------------------------------------------
    // Demo: Java 21 pattern matching in switch + sealed types
    // -----------------------------------------------------------------------

    /**
     * Returns a human-readable status for a response — showcases switch
     * pattern matching on the sealed ApiResponse hierarchy.
     */
    public static <T> String describeResponse(ApiResponse<T> response) {
        return switch (response) {
            case ApiResponse.Success<T> s when s.data() == null ->
                    "Operation succeeded with no content";
            case ApiResponse.Success<T> s ->
                    "Operation succeeded: " + s.message();
            case ApiResponse.Failure<T> f ->
                    "Operation failed [%d]: %s".formatted(f.statusCode(), f.error());
        };
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void seedData() {
        List.of(
                new Product(idSequence.getAndIncrement(), "Laptop Pro 21",
                        "High-performance laptop with Java 21 sticker", 1299.99, 10),
                new Product(idSequence.getAndIncrement(), "Wireless Mouse",
                        "Ergonomic wireless mouse", 49.99, 50),
                new Product(idSequence.getAndIncrement(), "Mechanical Keyboard",
                        "Tactile switches, RGB backlight", 129.99, 25)
        ).forEach(p -> store.put(p.id(), p));
    }
}
