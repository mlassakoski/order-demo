package com.demo.order.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demo.order.application.port.in.PlaceOrderCommand;
import com.demo.order.application.port.in.PlaceOrderUseCase;
import com.demo.order.domain.CustomerId;
import com.demo.order.domain.Order;
import com.demo.order.domain.OrderId;
import com.demo.order.domain.OrderLine;
import com.demo.order.domain.OrderStatus;
import com.demo.order.domain.ProductId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    private final MockMvc mockMvc;

    @MockitoBean
    private PlaceOrderUseCase placeOrder;

    OrderControllerTest(@Autowired MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void should_return_201_with_location_and_pending_order_when_order_is_placed() throws Exception {
        var lines = List.of(new OrderLine(new ProductId("product-1"), 2), new OrderLine(new ProductId("product-2"), 1));
        when(placeOrder.place(any()))
                .thenReturn(Order.rehydrate(
                        new OrderId("order-1"), new CustomerId("customer-1"), lines, OrderStatus.PENDING));

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {
                          "customerId": "customer-1",
                          "lines": [
                            {"productId": "product-1", "quantity": 2},
                            {"productId": "product-2", "quantity": 1}
                          ]
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/orders/order-1"))
                .andExpect(jsonPath("$.id").value("order-1"))
                .andExpect(jsonPath("$.customerId").value("customer-1"))
                .andExpect(jsonPath("$.lines[0].productId").value("product-1"))
                .andExpect(jsonPath("$.lines[0].quantity").value(2))
                .andExpect(jsonPath("$.lines[1].productId").value("product-2"))
                .andExpect(jsonPath("$.lines[1].quantity").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(placeOrder).place(new PlaceOrderCommand(new CustomerId("customer-1"), lines));
    }
}
