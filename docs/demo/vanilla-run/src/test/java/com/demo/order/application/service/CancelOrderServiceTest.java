package com.demo.order.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.demo.order.application.port.in.CancelOrderCommand;
import com.demo.order.application.port.in.CancelOrderUseCase;
import com.demo.order.application.port.in.OrderNotFoundException;
import com.demo.order.domain.CustomerId;
import com.demo.order.domain.InvalidIdentifierException;
import com.demo.order.domain.Order;
import com.demo.order.domain.OrderId;
import com.demo.order.domain.OrderLine;
import com.demo.order.domain.OrderNotCancellableException;
import com.demo.order.domain.OrderStatus;
import com.demo.order.domain.ProductId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CancelOrderServiceTest {

    private static final String ORDER_ID = "8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01";
    private static final String UNKNOWN_ORDER_ID = "00000000-0000-4000-8000-000000000000";

    private final FakeOrderRepository repository = new FakeOrderRepository();
    private final CancelOrderUseCase cancelOrder = new CancelOrderService(repository);

    @ParameterizedTest
    @EnumSource(
            value = OrderStatus.class,
            names = {"PENDING", "PAID"})
    void should_store_and_return_the_cancelled_order_when_order_is(OrderStatus status) {
        repository.save(orderWithStatus(status));

        var order = cancelOrder.cancel(new CancelOrderCommand(new OrderId(ORDER_ID)));

        assertThat(order.id()).isEqualTo(new OrderId(ORDER_ID));
        assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(repository.findById(new OrderId(ORDER_ID)))
                .hasValueSatisfying(stored -> assertThat(stored.status()).isEqualTo(OrderStatus.CANCELLED));
    }

    @ParameterizedTest
    @EnumSource(
            value = OrderStatus.class,
            names = {"SHIPPED", "CANCELLED"})
    void should_refuse_and_leave_the_stored_order_unchanged_when_order_is(OrderStatus status) {
        repository.save(orderWithStatus(status));

        assertThatThrownBy(() -> cancelOrder.cancel(new CancelOrderCommand(new OrderId(ORDER_ID))))
                .isInstanceOf(OrderNotCancellableException.class);

        assertThat(repository.findById(new OrderId(ORDER_ID)))
                .hasValueSatisfying(stored -> assertThat(stored.status()).isEqualTo(status));
    }

    @Test
    void should_throw_order_not_found_and_store_nothing_when_id_is_unknown() {
        assertThatThrownBy(() -> cancelOrder.cancel(new CancelOrderCommand(new OrderId(UNKNOWN_ORDER_ID))))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order 00000000-0000-4000-8000-000000000000 was not found");

        assertThat(repository.isEmpty()).isTrue();
    }

    @Test
    void should_refuse_command_when_order_id_is_missing() {
        assertThatThrownBy(() -> new CancelOrderCommand(null))
                .isInstanceOf(InvalidIdentifierException.class)
                .hasMessage("Order id must not be blank");
    }

    private static Order orderWithStatus(OrderStatus status) {
        return Order.rehydrate(
                new OrderId(ORDER_ID),
                new CustomerId("customer-1"),
                List.of(new OrderLine(new ProductId("product-1"), 2)),
                status);
    }
}
