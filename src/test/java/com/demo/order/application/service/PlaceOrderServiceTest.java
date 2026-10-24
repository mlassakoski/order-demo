package com.demo.order.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.demo.order.application.port.in.PlaceOrderCommand;
import com.demo.order.application.port.in.PlaceOrderUseCase;
import com.demo.order.application.port.out.OrderRepositoryPort;
import com.demo.order.domain.CustomerId;
import com.demo.order.domain.Order;
import com.demo.order.domain.OrderId;
import com.demo.order.domain.OrderLine;
import com.demo.order.domain.OrderStatus;
import com.demo.order.domain.ProductId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    private static final class FakeOrderRepository implements OrderRepositoryPort {

        private final Map<OrderId, Order> orders = new HashMap<>();

        @Override
        public void save(Order order) {
            orders.put(order.id(), order);
        }

        @Override
        public Optional<Order> findById(OrderId id) {
            return Optional.ofNullable(orders.get(id));
        }
    }
}
