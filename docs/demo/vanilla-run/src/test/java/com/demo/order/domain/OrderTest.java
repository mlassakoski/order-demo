package com.demo.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class OrderTest {

    private static final CustomerId CUSTOMER = new CustomerId("customer-1");

    @Test
    void should_be_pending_with_an_id_when_placed() {
        var order = Order.place(CUSTOMER, List.of(new OrderLine(new ProductId("product-1"), 2)));

        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.id()).isNotNull();
        assertThat(order.customerId()).isEqualTo(new CustomerId("customer-1"));
    }

    @Test
    void should_keep_lines_in_order_when_placed_with_several_lines() {
        var first = new OrderLine(new ProductId("product-1"), 2);
        var second = new OrderLine(new ProductId("product-2"), 1);
        var third = new OrderLine(new ProductId("product-1"), 5);

        var order = Order.place(CUSTOMER, List.of(first, second, third));

        assertThat(order.lines()).containsExactly(first, second, third);
    }

    @Test
    void should_expose_an_unmodifiable_copy_of_lines_when_placed() {
        var lines = new ArrayList<OrderLine>();
        lines.add(new OrderLine(new ProductId("product-1"), 2));

        var order = Order.place(CUSTOMER, lines);
        lines.add(new OrderLine(new ProductId("product-2"), 1));

        assertThat(order.lines()).hasSize(1);
        assertThatThrownBy(() -> order.lines().add(new OrderLine(new ProductId("product-3"), 1)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_get_different_ids_when_two_orders_are_placed() {
        var lines = List.of(new OrderLine(new ProductId("product-1"), 1));

        var first = Order.place(CUSTOMER, lines);
        var second = Order.place(CUSTOMER, lines);

        assertThat(first.id()).isNotEqualTo(second.id());
    }

    @Test
    void should_keep_given_state_when_rehydrated() {
        var id = new OrderId("8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01");
        var lines = List.of(new OrderLine(new ProductId("product-1"), 3));

        var order = Order.rehydrate(id, CUSTOMER, lines, OrderStatus.PAID);

        assertThat(order.id()).isEqualTo(new OrderId("8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01"));
        assertThat(order.customerId()).isEqualTo(CUSTOMER);
        assertThat(order.lines()).containsExactly(new OrderLine(new ProductId("product-1"), 3));
        assertThat(order.status()).isEqualTo(OrderStatus.PAID);
    }

    @ParameterizedTest
    @EnumSource(
            value = OrderStatus.class,
            names = {"PENDING", "PAID"})
    void should_be_cancelled_keeping_id_customer_and_lines_when_cancelled_from(OrderStatus status) {
        var id = new OrderId("8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01");
        var lines = List.of(new OrderLine(new ProductId("product-1"), 3));
        var order = Order.rehydrate(id, CUSTOMER, lines, status);

        var cancelled = order.cancel();

        assertThat(cancelled.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(cancelled.id()).isEqualTo(id);
        assertThat(cancelled.customerId()).isEqualTo(CUSTOMER);
        assertThat(cancelled.lines()).containsExactly(new OrderLine(new ProductId("product-1"), 3));
        assertThat(order.status()).isEqualTo(status);
    }

    @ParameterizedTest
    @EnumSource(
            value = OrderStatus.class,
            names = {"SHIPPED", "CANCELLED"})
    void should_refuse_to_cancel_when_order_is(OrderStatus status) {
        var order = Order.rehydrate(
                new OrderId("8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01"),
                CUSTOMER,
                List.of(new OrderLine(new ProductId("product-1"), 1)),
                status);

        assertThatThrownBy(order::cancel)
                .isInstanceOf(OrderNotCancellableException.class)
                .hasMessage("An order that is " + status + " cannot be cancelled");
    }
}
