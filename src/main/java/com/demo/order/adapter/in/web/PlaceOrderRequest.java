package com.demo.order.adapter.in.web;

import com.demo.order.application.port.in.PlaceOrderCommand;
import com.demo.order.domain.CustomerId;
import com.demo.order.domain.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Body of {@code POST /orders}. The client never sends an order identifier. Limits mirror {@link CustomerId} and
 * {@link Order}; messages are fixed English text so the problem detail does not depend on the server locale.
 */
public record PlaceOrderRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = CustomerId.MAX_LENGTH, message = "must be at most 64 characters")
        String customerId,

        @NotEmpty(message = "must not be empty")
        @Size(max = Order.MAX_LINES, message = "must have at most 100 order lines")
        List<@NotNull(message = "must not be null") @Valid OrderLineRequest> lines) {

    PlaceOrderCommand toCommand() {
        return new PlaceOrderCommand(
                new CustomerId(customerId),
                lines.stream().map(OrderLineRequest::toDomain).toList());
    }
}
