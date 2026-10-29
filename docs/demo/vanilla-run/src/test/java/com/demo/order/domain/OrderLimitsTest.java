package com.demo.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OrderLimitsTest {

    private static final CustomerId CUSTOMER = new CustomerId("customer-1");
    private static final OrderLine LINE = new OrderLine(new ProductId("product-1"), 1);

    @Test
    void should_accept_order_when_it_has_100_lines() {
        var order = Order.place(CUSTOMER, Collections.nCopies(100, LINE));

        assertThat(order.lines()).hasSize(100);
    }

    @Test
    void should_refuse_order_when_it_has_more_than_100_lines() {
        assertThatThrownBy(() -> Order.place(CUSTOMER, Collections.nCopies(101, LINE)))
                .isInstanceOf(TooManyOrderLinesException.class)
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("An order can have at most 100 order lines");
    }

    @Test
    void should_accept_customer_id_when_it_has_64_characters() {
        assertThat(new CustomerId("c".repeat(64)).value()).hasSize(64);
    }

    @Test
    void should_refuse_customer_id_when_longer_than_64_characters() {
        assertThatThrownBy(() -> new CustomerId("c".repeat(65)))
                .isInstanceOf(IdentifierTooLongException.class)
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Customer id must be at most 64 characters");
    }

    @Test
    void should_accept_product_id_when_it_has_64_characters() {
        assertThat(new ProductId("p".repeat(64)).value()).hasSize(64);
    }

    @Test
    void should_refuse_product_id_when_longer_than_64_characters() {
        assertThatThrownBy(() -> new ProductId("p".repeat(65)))
                .isInstanceOf(IdentifierTooLongException.class)
                .hasMessage("Product id must be at most 64 characters");
    }

    @Test
    void should_accept_order_line_when_quantity_is_1000() {
        assertThat(new OrderLine(new ProductId("product-1"), 1000).quantity()).isEqualTo(1000);
    }

    @Test
    void should_refuse_order_line_when_quantity_is_above_1000() {
        assertThatThrownBy(() -> new OrderLine(new ProductId("product-1"), 1001))
                .isInstanceOf(InvalidQuantityException.class)
                .hasMessage("Quantity must be between 1 and 1000");
    }

    @Test
    void should_accept_order_id_when_it_is_a_uuid() {
        assertThat(new OrderId("8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01").value())
                .isEqualTo("8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01");
    }

    @Test
    void should_tell_well_formed_when_value_is_a_canonical_uuid() {
        assertThat(OrderId.isWellFormed("8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01")).isTrue();
        assertThat(OrderId.isWellFormed("order-1")).isFalse();
        assertThat(OrderId.isWellFormed(" ")).isFalse();
        assertThat(OrderId.isWellFormed(null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "order-1",
                "1-1-1-1-1",
                "8A1D0C5E-4B7F-4C1A-9D3E-2F6B7A8C9D01",
                "8a1d0c5e4b7f4c1a9d3e2f6b7a8c9d01"
            })
    void should_refuse_order_id_when_it_is_not_a_uuid(String value) {
        assertThatThrownBy(() -> new OrderId(value))
                .isInstanceOf(InvalidOrderIdException.class)
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Order id must be a UUID");
    }
}
