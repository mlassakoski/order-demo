package com.demo.order.adapter.in.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demo.order.application.port.in.GetOrderQuery;
import com.demo.order.application.port.in.GetOrderUseCase;
import com.demo.order.application.port.in.OrderNotFoundException;
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

    @MockitoBean
    private GetOrderUseCase getOrder;

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

    @Test
    void should_return_200_with_the_order_when_id_is_known() throws Exception {
        var lines = List.of(new OrderLine(new ProductId("product-1"), 2), new OrderLine(new ProductId("product-2"), 1));
        when(getOrder.get(new GetOrderQuery(new OrderId("order-1"))))
                .thenReturn(Order.rehydrate(
                        new OrderId("order-1"), new CustomerId("customer-1"), lines, OrderStatus.PENDING));

        mockMvc.perform(get("/orders/order-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("order-1"))
                .andExpect(jsonPath("$.customerId").value("customer-1"))
                .andExpect(jsonPath("$.lines[0].productId").value("product-1"))
                .andExpect(jsonPath("$.lines[0].quantity").value(2))
                .andExpect(jsonPath("$.lines[1].productId").value("product-2"))
                .andExpect(jsonPath("$.lines[1].quantity").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void should_return_404_problem_detail_when_id_is_unknown() throws Exception {
        when(getOrder.get(new GetOrderQuery(new OrderId("unknown-order"))))
                .thenThrow(new OrderNotFoundException(new OrderId("unknown-order")));

        mockMvc.perform(get("/orders/unknown-order"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Order unknown-order was not found"))
                .andExpect(jsonPath("$.instance").value("/orders/unknown-order"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist())
                .andExpect(content().string(not(containsString("Exception"))))
                .andExpect(content().string(not(containsString("com.demo"))));
    }
}
