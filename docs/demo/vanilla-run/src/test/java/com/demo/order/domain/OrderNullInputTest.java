package com.demo.order.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class OrderNullInputTest {

    private static final OrderId ORDER_ID = new OrderId("8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01");
    private static final CustomerId CUSTOMER = new CustomerId("customer-1");
    private static final List<OrderLine> LINES = List.of(new OrderLine(new ProductId("product-1"), 1));

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void should_refuse_order_id_when_missing_or_blank(String value) {
        assertThatThrownBy(() -> new OrderId(value))
                .isInstanceOf(InvalidIdentifierException.class)
                .hasMessage("Order id must not be blank");
    }

    @Test
    void should_refuse_order_line_when_product_id_is_missing() {
        assertThatThrownBy(() -> new OrderLine(null, 1))
                .isInstanceOf(InvalidIdentifierException.class)
                .hasMessage("Product id must not be blank");
    }

    @Test
    void should_refuse_order_when_placed_without_a_customer() {
        assertThatThrownBy(() -> Order.place(null, LINES))
                .isInstanceOf(InvalidIdentifierException.class)
                .hasMessage("Customer id must not be blank");
    }

    @Test
    void should_refuse_order_when_placed_with_null_lines() {
        assertThatThrownBy(() -> Order.place(CUSTOMER, null))
                .isInstanceOf(EmptyOrderException.class)
                .hasMessage("An order needs at least one order line");
    }

    @Test
    void should_refuse_order_when_one_of_its_lines_is_null() {
        var lines = Arrays.asList(new OrderLine(new ProductId("product-1"), 1), null);

        assertThatThrownBy(() -> Order.place(CUSTOMER, lines))
                .isInstanceOf(InvalidOrderLineException.class)
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("An order line must not be null");
    }

    @Test
    void should_refuse_rehydration_when_order_id_is_missing() {
        assertThatThrownBy(() -> Order.rehydrate(null, CUSTOMER, LINES, OrderStatus.PENDING))
                .isInstanceOf(InvalidIdentifierException.class)
                .hasMessage("Order id must not be blank");
    }

    @Test
    void should_refuse_rehydration_when_customer_is_missing() {
        assertThatThrownBy(() -> Order.rehydrate(ORDER_ID, null, LINES, OrderStatus.PENDING))
                .isInstanceOf(InvalidIdentifierException.class)
                .hasMessage("Customer id must not be blank");
    }

    @Test
    void should_refuse_rehydration_when_lines_are_null() {
        assertThatThrownBy(() -> Order.rehydrate(ORDER_ID, CUSTOMER, null, OrderStatus.PENDING))
                .isInstanceOf(EmptyOrderException.class);
    }

    @Test
    void should_refuse_rehydration_when_status_is_missing() {
        assertThatThrownBy(() -> Order.rehydrate(ORDER_ID, CUSTOMER, LINES, null))
                .isInstanceOf(MissingOrderStatusException.class)
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Order status must not be null");
    }
}
