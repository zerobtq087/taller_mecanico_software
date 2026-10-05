package com.taller.security.controller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler({IllegalArgumentException.class, BadCredentialsException.class})
    public ResponseEntity<Map<String, String>> badRequest(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> friendlyValidationMessage(error.getField(), error.getDefaultMessage()))
                .orElse("Datos invalidos");
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    private String friendlyValidationMessage(String field, String defaultMessage) {
        return switch (field) {
            case "personalPhone" -> "El telefono personal debe contener 10 digitos.";
            case "workPhone" -> "El telefono del trabajo debe contener 10 digitos.";
            case "email" -> "El email debe tener un formato valido.";
            case "workEmail" -> "El email del trabajo debe tener un formato valido.";
            case "postalCode" -> "El codigo postal no tiene un formato valido.";
            case "photoDataUrl" -> "La foto no debe superar 20 MB.";
            default -> "Dato invalido: " + defaultMessage;
        };
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> unreadable(HttpMessageNotReadableException exception) {
        log.warn("JSON invalido en la peticion", exception);
        return ResponseEntity.badRequest().body(Map.of("error", "La peticion contiene JSON invalido o datos no soportados"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> methodNotSupported(HttpRequestMethodNotSupportedException exception) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of("error", "Metodo HTTP no permitido para este endpoint"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integrity(DataIntegrityViolationException exception) {
        String cause = exception.getMostSpecificCause() == null
                ? exception.getMessage()
                : exception.getMostSpecificCause().getMessage();
        log.error("Error de integridad en base de datos: {}", cause, exception);
        boolean duplicated = cause != null && cause.toLowerCase().contains("duplicate");
        HttpStatus status = duplicated ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        String message = duplicated
                ? "Registro duplicado; la informacion ya existe"
                : "No se pudo guardar por una restriccion de base de datos";
        return ResponseEntity.status(status).body(Map.of("error", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> generic(Exception exception) {
        log.error("Error no controlado", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor. Revisa los logs del backend."));
    }
}
