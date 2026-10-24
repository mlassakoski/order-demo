/**
 * Driving adapter: REST controllers and HTTP error mapping.
 *
 * <p>Controllers depend only on input ports ({@code *UseCase}). Request and response
 * bodies are records in this package. Domain exceptions are mapped to HTTP status codes
 * in a single {@code @RestControllerAdvice}.
 */
package com.demo.order.adapter.in.web;
