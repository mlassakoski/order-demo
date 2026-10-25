package com.demo.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demo.order.application.port.out.OrderRepositoryPort;
import com.demo.order.domain.CustomerId;
import com.demo.order.domain.OrderId;
import com.demo.order.domain.OrderLine;
import com.demo.order.domain.OrderStatus;
import com.demo.order.domain.ProductId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Acceptance: a customer places an order through the running application (ORD-12). */
@SpringBootTest
@AutoConfigureMockMvc
class PlaceOrderIT {

    private final MockMvc mockMvc;
    private final OrderRepositoryPort orderRepository;

    PlaceOrderIT(@Autowired MockMvc mockMvc, @Autowired OrderRepositoryPort orderRepository) {
        this.mockMvc = mockMvc;
        this.orderRepository = orderRepository;
    }

    @Test
    void should_create_pending_order_and_store_it_when_customer_places_an_order() throws Exception {
        var result = mockMvc.perform(
                        post("/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {
                          "customerId": "customer-1",
                          "lines": [
                            {"productId": "product-1", "quantity": 2},
                            {"productId": "product-2", "quantity": 1}
                          ]
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/orders/[^/]+")))
                .andExpect(jsonPath("$.customerId").value("customer-1"))
                .andExpect(jsonPath("$.lines[0].productId").value("product-1"))
                .andExpect(jsonPath("$.lines[0].quantity").value(2))
                .andExpect(jsonPath("$.lines[1].productId").value("product-2"))
                .andExpect(jsonPath("$.lines[1].quantity").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        var location = result.getResponse().getHeader("Location");
        var id = location.substring("/orders/".length());
        assertThat(orderRepository.findById(new OrderId(id))).hasValueSatisfying(stored -> {
            assertThat(stored.customerId()).isEqualTo(new CustomerId("customer-1"));
            assertThat(stored.lines())
                    .containsExactly(
                            new OrderLine(new ProductId("product-1"), 2), new OrderLine(new ProductId("product-2"), 1));
            assertThat(stored.status()).isEqualTo(OrderStatus.PENDING);
        });
    }
}
