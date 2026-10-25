package com.demo.order.application.service;

import com.demo.order.application.port.in.CancelOrderCommand;
import com.demo.order.application.port.in.CancelOrderUseCase;
import com.demo.order.application.port.in.OrderNotFoundException;
import com.demo.order.application.port.out.OrderRepositoryPort;
import com.demo.order.domain.Order;
import org.springframework.stereotype.Service;

@Service
public class CancelOrderService implements CancelOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    public CancelOrderService(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order cancel(CancelOrderCommand command) {
        var order = orderRepository
                .findById(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));
        order.cancel();
        orderRepository.save(order);
        return order;
    }
}
