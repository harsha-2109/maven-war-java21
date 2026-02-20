package com.example;

import com.example.model.ApiResponse;
import com.example.model.Product;
import com.example.service.ProductService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private final ProductService service = ProductService.getInstance();

    @Test
    void findAll_returnsSeedData() {
        ApiResponse<List<Product>> resp = service.findAll();
        assertInstanceOf(ApiResponse.Success.class, resp);
        ApiResponse.Success<List<Product>> success = (ApiResponse.Success<List<Product>>) resp;
        assertFalse(success.data().isEmpty());
    }

    @Test
    void findById_existingId_returnsProduct() {
        ApiResponse<Product> resp = service.findById(1L);
        assertInstanceOf(ApiResponse.Success.class, resp);
    }

    @Test
    void findById_missingId_returnsFailure() {
        ApiResponse<Product> resp = service.findById(9999L);
        assertInstanceOf(ApiResponse.Failure.class, resp);
        ApiResponse.Failure<Product> failure = (ApiResponse.Failure<Product>) resp;
        assertEquals(404, failure.statusCode());
    }

    @Test
    void create_validProduct_assignsId() {
        Product input = new Product(null, "Test Item", "desc", 9.99, 5);
        ApiResponse<Product> resp = service.create(input);
        assertInstanceOf(ApiResponse.Success.class, resp);
        ApiResponse.Success<Product> success = (ApiResponse.Success<Product>) resp;
        assertNotNull(success.data().id());
    }

    @Test
    void product_record_isAvailable() {
        Product p = new Product(1L, "Widget", "desc", 5.00, 3);
        assertTrue(p.isAvailable());

        Product outOfStock = p.withStock(0);
        assertFalse(outOfStock.isAvailable());
    }

    @Test
    void describeResponse_patternsMatch() {
        ApiResponse<Product> success = new ApiResponse.Success<>(
                new Product(1L, "item", "", 1.0, 1));
        ApiResponse<Product> failure = new ApiResponse.Failure<>(404, "not found");

        assertTrue(ProductService.describeResponse(success).startsWith("Operation succeeded"));
        assertTrue(ProductService.describeResponse(failure).startsWith("Operation failed"));
    }
}
