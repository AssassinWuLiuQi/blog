package com.scholarsmanuscript.exception;

public enum ErrorCode {

    USER_NOT_FOUND("USER_001", "User not found"),
    USERNAME_ALREADY_EXISTS("USER_002", "Username already exists"),
    EMAIL_ALREADY_EXISTS("USER_003", "Email already exists"),
    INVALID_CREDENTIALS("AUTH_001", "Invalid username or password"),
    TOKEN_EXPIRED("AUTH_002", "Token has expired"),
    TOKEN_INVALID("AUTH_003", "Token is invalid"),
    ARTICLE_NOT_FOUND("ARTICLE_001", "Article not found"),
    CATEGORY_NOT_FOUND("CATEGORY_001", "Category not found"),
    UNAUTHORIZED("AUTH_000", "Unauthorized access"),
    FORBIDDEN("AUTH_004", "Forbidden access");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
