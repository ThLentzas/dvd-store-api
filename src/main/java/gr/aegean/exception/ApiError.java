package gr.aegean.exception;


public record ApiError(String message, Integer statusCode) {}
