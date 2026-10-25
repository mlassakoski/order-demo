package com.demo.order.application.port.in;

import com.demo.order.domain.Order;

/** Input port: view an order by its identifier. */
public interface GetOrderUseCase {

    /** Returns the order, or throws {@link OrderNotFoundException} when no order has that identifier. */
    Order get(GetOrderQuery query);
}
