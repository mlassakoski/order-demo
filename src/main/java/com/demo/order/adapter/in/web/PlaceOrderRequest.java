package com.demo.order.adapter.in.web;

import com.demo.order.application.port.in.PlaceOrderCommand;
import com.demo.order.domain.CustomerId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Body of {@code POST /orders}. The client never sends an order identifier. */
public record PlaceOrderRequest(
        @NotBlank String customerId, @NotEmpty List<@NotNull @Valid OrderLineRequest> lines) {

    PlaceOrderCommand toCommand() {
        return new PlaceOrderCommand(
                new CustomerId(customerId),
                lines.stream().map(OrderLineRequest::toDomain).toList());
    }
}
