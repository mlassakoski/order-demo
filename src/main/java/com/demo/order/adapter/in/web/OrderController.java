package com.demo.order.adapter.in.web;

import com.demo.order.application.port.in.GetOrderQuery;
import com.demo.order.application.port.in.GetOrderUseCase;
import com.demo.order.application.port.in.PlaceOrderUseCase;
import com.demo.order.domain.OrderId;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final PlaceOrderUseCase placeOrder;
    private final GetOrderUseCase getOrder;

    public OrderController(PlaceOrderUseCase placeOrder, GetOrderUseCase getOrder) {
        this.placeOrder = placeOrder;
        this.getOrder = getOrder;
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable String id) {
        return OrderResponse.from(getOrder.get(new GetOrderQuery(new OrderId(id))));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> place(@RequestBody PlaceOrderRequest request) {
        var order = placeOrder.place(request.toCommand());
        return ResponseEntity.created(URI.create("/orders/" + order.id().value()))
                .body(OrderResponse.from(order));
    }
}
