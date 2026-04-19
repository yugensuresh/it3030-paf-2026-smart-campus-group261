package campus_nexus.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for Campus Nexus
 * Intercepts all exceptions thrown by controllers and formats them
 * into a professional, consistent JSON structure
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles business logic exceptions (duplicate resource, not found, conflicts)
     * Returns appropriate HTTP status based on error message
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {

        HttpStatus status;
        String message = ex.getMessage();

        // Determine appropriate HTTP status based on error message
        if (message.contains("not found") || message.contains("does not exist")) {
            status = HttpStatus.NOT_FOUND;
            logger.warn("Resource not found at {}: {}", request.getRequestURI(), message);
        } else if (message.contains("already exists") || message.contains("duplicate")) {
            status = HttpStatus.CONFLICT;
            logger.warn("Duplicate resource at {}: {}", request.getRequestURI(), message);
        } else if (message.contains("conflict") || message.contains("overlapping")) {
            status = HttpStatus.CONFLICT;
            logger.warn("Conflict at {}: {}", request.getRequestURI(), message);
        } else if (message.contains("Invalid") || message.contains("validation")) {
            status = HttpStatus.BAD_REQUEST;
            logger.warn("Invalid request at {}: {}", request.getRequestURI(), message);
        } else {
            status = HttpStatus.BAD_REQUEST;
            logger.warn("Business logic error at {}: {}", request.getRequestURI(), message);
        }

        ErrorResponse error = new ErrorResponse(
                status.value(),
                message,
                request.getRequestURI()
        );

        return new ResponseEntity<>(error, status);
    }

    /**
     * Handles data validation errors (e.g., empty fields, invalid data)
     * Returns 400 Bad Request with field-specific errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage()));

        logger.error("Validation failed for request at {}: {}", request.getRequestURI(), fieldErrors);

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("errors", fieldErrors);
        response.put("path", request.getRequestURI());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Catch-all handler for any unexpected system errors
     * Prevents internal server details from leaking to the client
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {

        logger.error("Unexpected error at {}: ", request.getRequestURI(), ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected system error occurred. Please contact support.",
                request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}