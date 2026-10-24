package com.demo.order.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.demo.order.application.port.in.GetOrderQuery;
import com.demo.order.application.port.in.GetOrderUseCase;
import com.demo.order.application.port.in.OrderNotFoundException;
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

class GetOrderServiceTest {

    private final FakeOrderRepository repository = new FakeOrderRepository();
    private final GetOrderUseCase getOrder = new GetOrderService(repository);

    @Test
    void should_return_the_stored_order_when_id_is_known() {
        repository.save(Order.rehydrate(
                new OrderId("order-1"),
                new CustomerId("customer-1"),
                List.of(new OrderLine(new ProductId("product-1"), 2)),
                OrderStatus.PENDING));

        var order = getOrder.get(new GetOrderQuery(new OrderId("order-1")));

        assertThat(order.id()).isEqualTo(new OrderId("order-1"));
        assertThat(order.customerId()).isEqualTo(new CustomerId("customer-1"));
        assertThat(order.lines()).containsExactly(new OrderLine(new ProductId("product-1"), 2));
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void should_throw_order_not_found_with_the_id_when_id_is_unknown() {
        assertThatThrownBy(() -> getOrder.get(new GetOrderQuery(new OrderId("unknown-order"))))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("unknown-order");
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
