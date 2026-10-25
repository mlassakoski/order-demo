package com.demo.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

/** Acceptance: cancelling a Shipped order is refused and the order stays Shipped (ORD-23). */
@SpringBootTest
@AutoConfigureMockMvc
class RefuseCancellationIT {

    private static final String SHIPPED_ORDER_ID = "3f2b9c1d-7e4a-4b6c-8d2e-1a9f0b7c6d53";

    private final MockMvc mockMvc;
    private final OrderRepositoryPort orders;

    RefuseCancellationIT(@Autowired MockMvc mockMvc, @Autowired OrderRepositoryPort orders) {
        this.mockMvc = mockMvc;
        this.orders = orders;
    }

    @Test
    void should_return_409_and_keep_the_order_shipped_when_customer_cancels_a_shipped_order() throws Exception {
        // No ship API yet: seed the Shipped order through the storage port (arrange only).
        orders.save(Order.rehydrate(
                new OrderId(SHIPPED_ORDER_ID),
                new CustomerId("customer-1"),
                List.of(new OrderLine(new ProductId("product-1"), 2)),
                OrderStatus.SHIPPED));

        mockMvc.perform(post("/orders/3f2b9c1d-7e4a-4b6c-8d2e-1a9f0b7c6d53/cancel"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("Order 3f2b9c1d-7e4a-4b6c-8d2e-1a9f0b7c6d53 cannot be cancelled because it is SHIPPED"))
                .andExpect(jsonPath("$.currentState").value("SHIPPED"));

        mockMvc.perform(get("/orders/3f2b9c1d-7e4a-4b6c-8d2e-1a9f0b7c6d53"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }
}
