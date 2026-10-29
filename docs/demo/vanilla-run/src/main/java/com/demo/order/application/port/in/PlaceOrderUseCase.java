package com.demo.order.application.port.in;

import com.demo.order.domain.Order;

/** Input port: place a new order. */
public interface PlaceOrderUseCase {

    Order place(PlaceOrderCommand command);
}
