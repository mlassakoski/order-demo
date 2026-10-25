package com.demo.order.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.demo.order.application.port.in.GetOrderQuery;
import com.demo.order.application.port.in.GetOrderUseCase;
import com.demo.order.application.port.in.OrderNotFoundException;
import com.demo.order.domain.CustomerId;
import com.demo.order.domain.InvalidIdentifierException;
import com.demo.order.domain.Order;
import com.demo.order.domain.OrderId;
import com.demo.order.domain.OrderLine;
import com.demo.order.domain.OrderStatus;
import com.demo.order.domain.ProductId;
import java.util.List;
import org.junit.jupiter.api.Test;

class GetOrderServiceTest {

    private static final String ORDER_ID = "8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01";
    private static final String UNKNOWN_ORDER_ID = "00000000-0000-4000-8000-000000000000";

    private final FakeOrderRepository repository = new FakeOrderRepository();
    private final GetOrderUseCase getOrder = new GetOrderService(repository);

    @Test
    void should_return_the_stored_order_when_id_is_known() {
        repository.save(Order.rehydrate(
                new OrderId(ORDER_ID),
                new CustomerId("customer-1"),
                List.of(new OrderLine(new ProductId("product-1"), 2)),
                OrderStatus.PENDING));

        var order = getOrder.get(new GetOrderQuery(new OrderId(ORDER_ID)));

        assertThat(order.id()).isEqualTo(new OrderId(ORDER_ID));
        assertThat(order.customerId()).isEqualTo(new CustomerId("customer-1"));
        assertThat(order.lines()).containsExactly(new OrderLine(new ProductId("product-1"), 2));
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void should_throw_order_not_found_with_the_id_when_id_is_unknown() {
        assertThatThrownBy(() -> getOrder.get(new GetOrderQuery(new OrderId(UNKNOWN_ORDER_ID))))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order 00000000-0000-4000-8000-000000000000 was not found");
    }

    @Test
    void should_refuse_query_when_order_id_is_missing() {
        assertThatThrownBy(() -> new GetOrderQuery(null))
                .isInstanceOf(InvalidIdentifierException.class)
                .hasMessage("Order id must not be null");
    }
}
