package com.demo.order.adapter.in.web;

import com.demo.order.domain.OrderLine;
import com.demo.order.domain.ProductId;

/** One order line in a place-order request. */
public record OrderLineRequest(String productId, int quantity) {

    OrderLine toDomain() {
        return new OrderLine(new ProductId(productId), quantity);
    }
}
