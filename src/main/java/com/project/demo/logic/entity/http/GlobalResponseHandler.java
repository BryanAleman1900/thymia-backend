package com.project.demo.logic.entity.http;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalResponseHandler {
    @ResponseBody
    public <T> ResponseEntity<?> handleResponse(String message, T body, HttpStatus status, HttpServletRequest request) {
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        if (body instanceof HttpResponse) {
            HttpResponse<?> response = (HttpResponse<?>) body;
            response.setMeta(meta);
            return new ResponseEntity<>(response, status);
        }
        HttpResponse<T> response = new HttpResponse<>(message, body, meta);
        return  new ResponseEntity<>(response, status);
    }

    @ResponseBody
    public <T> ResponseEntity<?> handleResponse(String message, HttpStatus status, HttpServletRequest request) {
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        HttpResponse<?> response = new HttpResponse<>(message, meta);
        return  new ResponseEntity<>(response, status);
    }

    @ResponseBody
    public <T> ResponseEntity<?> handleResponse(String message, T body, HttpStatus status, Meta meta) {
        if (body instanceof HttpResponse) {
            HttpResponse<?> response = (HttpResponse<?>) body;
            response.setMeta(meta);
            return new ResponseEntity<>(response, status);
        }
        HttpResponse<T> response = new HttpResponse<>(message, body, meta);
        return  new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(Exception e) {
        if (e instanceof ResponseStatusException) {
            throw (ResponseStatusException) e;
        }
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setDetail(e.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        HttpResponse<?> response = new HttpResponse<>(ex.getReason(), meta);
        return new ResponseEntity<>(response, ex.getStatusCode());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        HttpResponse<?> response = new HttpResponse<>(ex.getMessage(), meta);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
