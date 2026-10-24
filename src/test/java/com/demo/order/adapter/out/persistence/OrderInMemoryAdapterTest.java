package com.demo.order.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.demo.order.application.port.out.OrderRepositoryPort;
import com.demo.order.domain.CustomerId;
import com.demo.order.domain.Order;
import com.demo.order.domain.OrderId;
import com.demo.order.domain.OrderLine;
import com.demo.order.domain.OrderStatus;
import com.demo.order.domain.ProductId;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderInMemoryAdapterTest {

    private final OrderRepositoryPort repository = new OrderInMemoryAdapter();

    @Test
    void should_return_an_equal_but_separate_order_when_saved_order_is_found() {
        var order = Order.rehydrate(
                new OrderId("8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01"),
                new CustomerId("customer-1"),
                List.of(new OrderLine(new ProductId("product-1"), 2), new OrderLine(new ProductId("product-2"), 1)),
                OrderStatus.PENDING);

        repository.save(order);
        var found = repository
                .findById(new OrderId("8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01"))
                .orElseThrow();

        assertThat(found).isNotSameAs(order);
        assertThat(found.id()).isEqualTo(new OrderId("8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01"));
        assertThat(found.customerId()).isEqualTo(new CustomerId("customer-1"));
        assertThat(found.lines())
                .containsExactly(
                        new OrderLine(new ProductId("product-1"), 2), new OrderLine(new ProductId("product-2"), 1));
        assertThat(found.status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void should_return_empty_when_order_is_unknown() {
        assertThat(repository.findById(new OrderId("00000000-0000-4000-8000-000000000000")))
                .isEmpty();
    }
}
