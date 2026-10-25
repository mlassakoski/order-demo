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

/** Acceptance: a customer cancels a Pending or Paid order through the running application (ORD-22). */
@SpringBootTest
@AutoConfigureMockMvc
class CancelOrderIT {

    private static final String PAID_ORDER_ID = "5c7e2a9b-1d3f-4e6a-8b0c-9d2e4f6a8b17";

    private final MockMvc mockMvc;
    private final OrderRepositoryPort orders;

    CancelOrderIT(@Autowired MockMvc mockMvc, @Autowired OrderRepositoryPort orders) {
        this.mockMvc = mockMvc;
        this.orders = orders;
    }

    @Test
    void should_return_and_store_the_order_as_cancelled_when_customer_cancels_a_pending_order() throws Exception {
        var location = mockMvc.perform(
                        post("/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {
                          "customerId": "customer-1",
                          "lines": [{"productId": "product-1", "quantity": 2}]
                        }
                        """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");
        var id = location.substring("/orders/".length());

        mockMvc.perform(post(location + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.customerId").value("customer-1"))
                .andExpect(jsonPath("$.lines[0].productId").value("product-1"))
                .andExpect(jsonPath("$.lines[0].quantity").value(2))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void should_return_and_store_the_order_as_cancelled_when_customer_cancels_a_paid_order() throws Exception {
        // No pay API yet: seed the Paid order through the storage port (arrange only).
        orders.save(Order.rehydrate(
                new OrderId(PAID_ORDER_ID),
                new CustomerId("customer-1"),
                List.of(new OrderLine(new ProductId("product-1"), 2)),
                OrderStatus.PAID));

        mockMvc.perform(post("/orders/5c7e2a9b-1d3f-4e6a-8b0c-9d2e4f6a8b17/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("5c7e2a9b-1d3f-4e6a-8b0c-9d2e4f6a8b17"))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/orders/5c7e2a9b-1d3f-4e6a-8b0c-9d2e4f6a8b17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
