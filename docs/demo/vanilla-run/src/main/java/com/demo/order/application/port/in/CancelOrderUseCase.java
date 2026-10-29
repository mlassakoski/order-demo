package com.demo.order.application.port.in;

import com.demo.order.domain.Order;

/** Input port: cancel an order that has not shipped yet. */
public interface CancelOrderUseCase {

    /**
     * Returns the cancelled order. Throws {@link OrderNotFoundException} when no order has that identifier, and
     * {@link com.demo.order.domain.OrderNotCancellableException} when the order is no longer Pending or Paid.
     */
    Order cancel(CancelOrderCommand command);
}
