package com.demo.order.application.service;

import com.demo.order.application.port.in.GetOrderQuery;
import com.demo.order.application.port.in.GetOrderUseCase;
import com.demo.order.application.port.in.OrderNotFoundException;
import com.demo.order.application.port.out.OrderRepositoryPort;
import com.demo.order.domain.Order;
import org.springframework.stereotype.Service;

@Service
public class GetOrderService implements GetOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    public GetOrderService(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order get(GetOrderQuery query) {
        return orderRepository.findById(query.orderId()).orElseThrow(() -> new OrderNotFoundException(query.orderId()));
    }
}
