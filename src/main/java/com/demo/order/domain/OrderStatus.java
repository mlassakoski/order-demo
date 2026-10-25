package com.demo.order.domain;

/** Lifecycle state of an order. */
public enum OrderStatus {
    PENDING,
    PAID,
    SHIPPED,
    CANCELLED
}
