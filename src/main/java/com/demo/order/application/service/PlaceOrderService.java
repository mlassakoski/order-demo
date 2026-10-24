package com.demo.order.application.service;

import com.demo.order.application.port.in.PlaceOrderCommand;
import com.demo.order.application.port.in.PlaceOrderUseCase;
import com.demo.order.application.port.out.OrderRepositoryPort;
import com.demo.order.domain.Order;
import org.springframework.stereotype.Service;

@Service
public class PlaceOrderService implements PlaceOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    public PlaceOrderService(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order place(PlaceOrderCommand command) {
        var order = Order.place(command.customerId(), command.lines());
        orderRepository.save(order);
        return order;
    }
}
