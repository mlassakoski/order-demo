package com.demo.order.adapter.in.web;

import com.demo.order.domain.OrderLine;
import com.demo.order.domain.ProductId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** One order line in a place-order request. Limits mirror {@link ProductId} and {@link OrderLine}. */
public record OrderLineRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = ProductId.MAX_LENGTH, message = "must be at most 64 characters")
        String productId,

        @NotNull(message = "must not be null")
        @Min(value = OrderLine.MIN_QUANTITY, message = "must be at least 1")
        @Max(value = OrderLine.MAX_QUANTITY, message = "must be at most 1000")
        Integer quantity) {

    OrderLine toDomain() {
        return new OrderLine(new ProductId(productId), quantity);
    }
}
