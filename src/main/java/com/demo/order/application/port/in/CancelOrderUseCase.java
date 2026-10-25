package com.demo.order.application.port.in;

import com.demo.order.domain.Order;
import com.demo.order.domain.OrderCannotBeCancelledException;

/** Input port: cancel an order by its identifier. */
public interface CancelOrderUseCase {

    /**
     * Cancels the order and returns it as stored. Throws {@link OrderNotFoundException} when no order has that
     * identifier, and {@link OrderCannotBeCancelledException} when the order is Shipped or already Cancelled.
     */
    Order cancel(CancelOrderCommand command);
}
