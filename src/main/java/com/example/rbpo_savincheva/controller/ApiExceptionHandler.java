package com.example.rbpo_savincheva.controller;

import com.example.rbpo_savincheva.service.ConflictException;
import com.example.rbpo_savincheva.service.NotFoundException;
import com.example.rbpo_savincheva.service.RefreshDeniedException;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, String>> notFound(NotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<Map<String, String>> conflict(ConflictException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
  }

  @ExceptionHandler(RefreshDeniedException.class)
  public ResponseEntity<Map<String, String>> unauthorized(RefreshDeniedException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, String>> integrity(DataIntegrityViolationException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("error", "Operation violates data integrity rules (duplicate or invalid value)"));
  }
}
