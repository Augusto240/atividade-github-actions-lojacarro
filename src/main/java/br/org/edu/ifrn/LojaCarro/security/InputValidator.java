package br.org.edu.ifrn.LojaCarro.security;

import org.springframework.stereotype.Component;

@Component
public class InputValidator {

    private static final int MAX_FIELD_LENGTH = 255;
    private static final String HTML_SCRIPT_PATTERN = "(?i)(<[^>]*>|javascript:|onerror=|onload=)";

    public String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll(HTML_SCRIPT_PATTERN, "").trim();
    }

    public void validateFieldLength(String value, String fieldName) {
        if (value != null && value.length() > MAX_FIELD_LENGTH) {
            throw new ValidationException(
                    fieldName + " must not exceed " + MAX_FIELD_LENGTH + " characters",
                    422
            );
        }
    }

    public void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required", 400);
        }
    }

    public void validateEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException("Invalid email format", 400);
        }
    }

    public static class ValidationException extends RuntimeException {
        private final int statusCode;

        public ValidationException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
