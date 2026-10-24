package com.demo.order.adapter.in.web;

import com.demo.order.domain.OrderLine;
import com.demo.order.domain.ProductId;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** One order line in a place-order request. */
public record OrderLineRequest(
        @NotBlank String productId, @NotNull @Min(1) Integer quantity) {

    OrderLine toDomain() {
        return new OrderLine(new ProductId(productId), quantity);
    }
}
