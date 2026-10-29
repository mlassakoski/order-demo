package com.demo.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demo.order.application.port.out.OrderRepositoryPort;
import com.demo.order.domain.CustomerId;
import com.demo.order.domain.Order;
import com.demo.order.domain.OrderId;
import com.demo.order.domain.OrderLine;
import com.demo.order.domain.OrderStatus;
import com.demo.order.domain.ProductId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Acceptance: a customer cancels an order that has not shipped yet, through the running application. */
@SpringBootTest
@AutoConfigureMockMvc
class CancelOrderIT {

    private static final String SHIPPED_ORDER_ID = "5c3f1e2a-7d4b-4e8f-a1b2-c3d4e5f60718";

    private final MockMvc mockMvc;
    private final OrderRepositoryPort orderRepository;

    CancelOrderIT(@Autowired MockMvc mockMvc, @Autowired OrderRepositoryPort orderRepository) {
        this.mockMvc = mockMvc;
        this.orderRepository = orderRepository;
    }

    @Test
    void should_cancel_a_placed_order_and_show_it_as_cancelled() throws Exception {
        var location = mockMvc.perform(
                        post("/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerId": "customer-1", "lines": [{"productId": "product-1", "quantity": 2}]}
                        """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(post(location + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(post(location + "/cancel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("An order that is CANCELLED cannot be cancelled"));
    }

    @Test
    void should_refuse_to_cancel_a_shipped_order_and_keep_it_shipped() throws Exception {
        // Nothing exposed over HTTP ships an order yet, so the shipped order is stored directly.
        orderRepository.save(Order.rehydrate(
                new OrderId(SHIPPED_ORDER_ID),
                new CustomerId("customer-1"),
                List.of(new OrderLine(new ProductId("product-1"), 1)),
                OrderStatus.SHIPPED));
        var location = "/orders/" + SHIPPED_ORDER_ID;

        mockMvc.perform(post(location + "/cancel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("An order that is SHIPPED cannot be cancelled"));

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }
}
