package com.demo.order.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.demo.order.application.port.in.PlaceOrderCommand;
import com.demo.order.application.port.in.PlaceOrderUseCase;
import com.demo.order.domain.CustomerId;
import com.demo.order.domain.EmptyOrderException;
import com.demo.order.domain.InvalidIdentifierException;
import com.demo.order.domain.InvalidOrderLineException;
import com.demo.order.domain.OrderLine;
import com.demo.order.domain.OrderStatus;
import com.demo.order.domain.ProductId;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlaceOrderServiceTest {

    private final FakeOrderRepository repository = new FakeOrderRepository();
    private final PlaceOrderUseCase placeOrder = new PlaceOrderService(repository);

    @Test
    void should_return_pending_order_with_given_customer_and_lines_when_order_is_placed() {
        var lines = List.of(new OrderLine(new ProductId("product-1"), 2), new OrderLine(new ProductId("product-2"), 1));

        var order = placeOrder.place(new PlaceOrderCommand(new CustomerId("customer-1"), lines));

        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.customerId()).isEqualTo(new CustomerId("customer-1"));
        assertThat(order.lines())
                .containsExactly(
                        new OrderLine(new ProductId("product-1"), 2), new OrderLine(new ProductId("product-2"), 1));
    }

    @Test
    void should_save_the_order_when_order_is_placed() {
        var command = new PlaceOrderCommand(
                new CustomerId("customer-1"), List.of(new OrderLine(new ProductId("product-1"), 2)));

        var order = placeOrder.place(command);

        assertThat(repository.findById(order.id())).hasValueSatisfying(saved -> {
            assertThat(saved.customerId()).isEqualTo(new CustomerId("customer-1"));
            assertThat(saved.status()).isEqualTo(OrderStatus.PENDING);
        });
    }

    @Test
    void should_refuse_command_when_customer_is_missing() {
        var lines = List.of(new OrderLine(new ProductId("product-1"), 2));

        assertThatThrownBy(() -> placeOrder.place(new PlaceOrderCommand(null, lines)))
                .isInstanceOf(InvalidIdentifierException.class)
                .hasMessage("Customer id must not be blank");
        assertThat(repository.isEmpty()).isTrue();
    }

    @Test
    void should_refuse_command_when_lines_are_null() {
        assertThatThrownBy(() -> placeOrder.place(new PlaceOrderCommand(new CustomerId("customer-1"), null)))
                .isInstanceOf(EmptyOrderException.class)
                .hasMessage("An order needs at least one order line");
        assertThat(repository.isEmpty()).isTrue();
    }

    @Test
    void should_refuse_command_when_one_of_its_lines_is_null() {
        var lines = Arrays.asList(new OrderLine(new ProductId("product-1"), 2), null);

        assertThatThrownBy(() -> new PlaceOrderCommand(new CustomerId("customer-1"), lines))
                .isInstanceOf(InvalidOrderLineException.class)
                .hasMessage("An order line must not be null");
    }
}
