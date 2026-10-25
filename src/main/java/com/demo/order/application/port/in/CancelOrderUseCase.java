package com.demo.order.application.port.in;

import com.demo.order.domain.Order;

/** Input port: cancel an order by its identifier. */
public interface CancelOrderUseCase {

    /**
     * Cancels the order and returns it as stored, or throws {@link OrderNotFoundException} when no order has that
     * identifier.
     */
    Order cancel(CancelOrderCommand command);
}
