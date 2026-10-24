package com.demo.order.adapter.in.web;

import com.demo.order.application.port.in.NotFoundException;
import com.demo.order.domain.DomainValidationException;
import java.net.URI;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Maps exceptions to RFC 9457 problem details. The only place where errors become HTTP responses.
 *
 * <p>Spring's own MVC errors (malformed body, bean validation, ...) are handled by the base class.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final URI ORDERS = URI.create("/orders");

    @ExceptionHandler(DomainValidationException.class)
    ResponseEntity<ProblemDetail> handleDomainValidation(DomainValidationException exception) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        return ResponseEntity.of(problem).build();
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ProblemDetail> handleNotFound(NotFoundException exception) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        return ResponseEntity.of(problem).build();
    }

    /** Like any not-found, but the instance is the collection so the raw path value is never reflected. */
    @ExceptionHandler(MalformedOrderIdException.class)
    ResponseEntity<ProblemDetail> handleMalformedOrderId(MalformedOrderIdException exception) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setInstance(ORDERS);
        return ResponseEntity.of(problem).build();
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var detail = exception.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField)
                        .thenComparing(FieldError::getDefaultMessage, Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        return handleExceptionInternal(exception, problem, headers, status, request);
    }
}
