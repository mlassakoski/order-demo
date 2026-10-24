package com.demo.order.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class OrderInvariantsTest {

    private static final CustomerId CUSTOMER = new CustomerId("customer-1");

    @Test
    void should_refuse_order_when_placed_without_lines() {
        assertThatThrownBy(() -> Order.place(CUSTOMER, List.of()))
                .isInstanceOf(EmptyOrderException.class)
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("An order needs at least one order line");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void should_refuse_order_line_when_quantity_is_below_one(int quantity) {
        assertThatThrownBy(() -> new OrderLine(new ProductId("product-1"), quantity))
                .isInstanceOf(InvalidQuantityException.class)
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Quantity must be at least 1");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void should_refuse_customer_id_when_missing_or_blank(String value) {
        assertThatThrownBy(() -> new CustomerId(value))
                .isInstanceOf(InvalidIdentifierException.class)
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Customer id must not be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void should_refuse_product_id_when_missing_or_blank(String value) {
        assertThatThrownBy(() -> new ProductId(value))
                .isInstanceOf(InvalidIdentifierException.class)
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Product id must not be blank");
    }
}
