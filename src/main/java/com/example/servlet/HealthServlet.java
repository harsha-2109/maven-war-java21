package com.example.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.json.Json;

import java.io.IOException;
import java.time.Instant;

/**
 * Simple health-check endpoint: GET /api/health
 */
@WebServlet(name = "HealthServlet", urlPatterns = "/api/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(200);

        String body = Json.createObjectBuilder()
                .add("status", "UP")
                .add("javaVersion", System.getProperty("java.version"))
                .add("timestamp", Instant.now().toString())
                .add("application", "maven-war-java21")
                .build()
                .toString();

        resp.getWriter().write(body);
    }
}
