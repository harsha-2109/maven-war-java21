package com.example.servlet;

import com.example.model.ApiResponse;
import com.example.model.Product;
import com.example.service.ProductService;
import jakarta.json.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * REST-like servlet for Product CRUD.
 *
 * Routes:
 *   GET    /api/products          — list all
 *   GET    /api/products/{id}     — find by id
 *   POST   /api/products          — create
 *   PUT    /api/products/{id}     — update
 *   DELETE /api/products/{id}     — delete
 */
@WebServlet(name = "ProductServlet", urlPatterns = "/api/products/*")
public class ProductServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ProductServlet.class);

    private final ProductService productService = ProductService.getInstance();

    // -----------------------------------------------------------------------
    // GET
    // -----------------------------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // List all products
            ApiResponse<List<Product>> result = productService.findAll();
            sendJson(resp, 200, toJsonArray(result));
        } else {
            // Get by id
            Long id = parseId(pathInfo, resp);
            if (id == null) return;

            ApiResponse<Product> result = productService.findById(id);
            int status = result instanceof ApiResponse.Failure<?> f ? f.statusCode() : 200;
            sendJson(resp, status, toJson(result));
        }
    }

    // -----------------------------------------------------------------------
    // POST
    // -----------------------------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Product product = parseProduct(req, resp, null);
        if (product == null) return;

        ApiResponse<Product> result = productService.create(product);
        sendJson(resp, 201, toJson(result));
    }

    // -----------------------------------------------------------------------
    // PUT
    // -----------------------------------------------------------------------

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Long id = parseId(req.getPathInfo(), resp);
        if (id == null) return;

        Product product = parseProduct(req, resp, id);
        if (product == null) return;

        ApiResponse<Product> result = productService.update(id, product);
        int status = result instanceof ApiResponse.Failure<?> f ? f.statusCode() : 200;
        sendJson(resp, status, toJson(result));
    }

    // -----------------------------------------------------------------------
    // DELETE
    // -----------------------------------------------------------------------

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Long id = parseId(req.getPathInfo(), resp);
        if (id == null) return;

        ApiResponse<Void> result = productService.delete(id);
        int status = result instanceof ApiResponse.Failure<?> f ? f.statusCode() : 200;
        sendJson(resp, status, toJson(result));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Long parseId(String pathInfo, HttpServletResponse resp) throws IOException {
        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(resp, 400, "Missing product id in path");
            return null;
        }
        try {
            return Long.parseLong(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            sendError(resp, 400, "Invalid product id: " + pathInfo.substring(1));
            return null;
        }
    }

    private Product parseProduct(HttpServletRequest req,
                                  HttpServletResponse resp,
                                  Long existingId) throws IOException {
        try (JsonReader reader = Json.createReader(req.getReader())) {
            JsonObject obj = reader.readObject();
            return new Product(
                    existingId,
                    obj.getString("name"),
                    obj.getString("description", ""),
                    obj.getJsonNumber("price").doubleValue(),
                    obj.getInt("stock", 0)
            );
        } catch (Exception e) {
            sendError(resp, 400, "Invalid request body: " + e.getMessage());
            return null;
        }
    }

    private String toJson(ApiResponse<?> response) {
        // Java 21 pattern matching in switch
        return switch (response) {
            case ApiResponse.Success<?> s when s.data() instanceof Product p ->
                    productToJsonObject(p)
                            .add("message", s.message())
                            .build()
                            .toString();
            case ApiResponse.Success<?> s ->
                    Json.createObjectBuilder()
                            .add("message", s.message())
                            .build()
                            .toString();
            case ApiResponse.Failure<?> f ->
                    Json.createObjectBuilder()
                            .add("error", f.error())
                            .add("status", f.statusCode())
                            .build()
                            .toString();
        };
    }

    @SuppressWarnings("unchecked")
    private String toJsonArray(ApiResponse<List<Product>> response) {
        if (response instanceof ApiResponse.Success<List<Product>> s) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            s.data().forEach(p -> arrayBuilder.add(productToJsonObject(p).build()));
            return Json.createObjectBuilder()
                    .add("data", arrayBuilder)
                    .add("count", s.data().size())
                    .build()
                    .toString();
        }
        return "{}";
    }

    private JsonObjectBuilder productToJsonObject(Product p) {
    JsonObjectBuilder builder = Json.createObjectBuilder();

    if (p.id() != null) {
        builder.add("id", p.id());
    } else {
        builder.addNull("id");
    }

    builder.add("name", p.name())
           .add("description", p.description())
           .add("price", p.price())
           .add("stock", p.stock())
           .add("available", p.isAvailable());

    return builder;
}

    private void sendJson(HttpServletResponse resp, int status, String json)
            throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(status);
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(json);
        }
    }

    private void sendError(HttpServletResponse resp, int status, String message)
            throws IOException {
        sendJson(resp, status,
                Json.createObjectBuilder()
                        .add("error", message)
                        .add("status", status)
                        .build()
                        .toString());
    }
}
