package com.diego.interview.infraestructure.in.rest.advice;

import com.diego.interview.domain.exception.BusinessException;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.FieldError;

import java.util.Locale;
import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler {
    private final MessageSource messageSource;

    public RestExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, String>> handleBusiness(BusinessException ex, Locale locale) {

        String translated = messageSource.getMessage(
                ex.getCode(),
                ex.getArgs(),    
                locale
        );

        return ResponseEntity
                .badRequest()
                .body(Map.of("mensaje", translated));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Solicitud inválida");

        return ResponseEntity.badRequest()
                .body(Map.of("mensaje", errorMessage));
    }
}
