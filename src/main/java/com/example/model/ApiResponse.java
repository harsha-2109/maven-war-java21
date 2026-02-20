package com.example.model;

/**
 * Sealed interface for API responses — Java 17+ feature, fully supported in Java 21.
 */
public sealed interface ApiResponse<T>
        permits ApiResponse.Success, ApiResponse.Failure {

    record Success<T>(T data, String message) implements ApiResponse<T> {
        public Success(T data) {
            this(data, "OK");
        }
    }

    record Failure<T>(int statusCode, String error) implements ApiResponse<T> {}

    /**
     * Pattern match on result using Java 21 pattern matching in switch.
     */
    default String toSummary() {
        return switch (this) {
            case Success<T> s  -> "SUCCESS: " + s.message();
            case Failure<T> f  -> "ERROR " + f.statusCode() + ": " + f.error();
        };
    }
}
