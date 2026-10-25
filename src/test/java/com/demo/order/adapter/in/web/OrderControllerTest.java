package com.demo.order.adapter.in.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demo.order.application.port.in.CancelOrderCommand;
import com.demo.order.application.port.in.CancelOrderUseCase;
import com.demo.order.application.port.in.GetOrderQuery;
import com.demo.order.application.port.in.GetOrderUseCase;
import com.demo.order.application.port.in.OrderNotFoundException;
import com.demo.order.application.port.in.PlaceOrderCommand;
import com.demo.order.application.port.in.PlaceOrderUseCase;
import com.demo.order.domain.CustomerId;
import com.demo.order.domain.EmptyOrderException;
import com.demo.order.domain.Order;
import com.demo.order.domain.OrderCannotBeCancelledException;
import com.demo.order.domain.OrderId;
import com.demo.order.domain.OrderLine;
import com.demo.order.domain.OrderStatus;
import com.demo.order.domain.ProductId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    private static final String ORDER_ID = "8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01";
    private static final String UNKNOWN_ORDER_ID = "00000000-0000-4000-8000-000000000000";

    private static final String VALID_BODY = """
            {"customerId": "customer-1", "lines": [{"productId": "product-1", "quantity": 1}]}
            """;

    private final MockMvc mockMvc;

    @MockitoBean
    private PlaceOrderUseCase placeOrder;

    @MockitoBean
    private GetOrderUseCase getOrder;

    @MockitoBean
    private CancelOrderUseCase cancelOrder;

    OrderControllerTest(@Autowired MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void should_return_201_with_location_and_pending_order_when_order_is_placed() throws Exception {
        var lines = List.of(new OrderLine(new ProductId("product-1"), 2), new OrderLine(new ProductId("product-2"), 1));
        when(placeOrder.place(any()))
                .thenReturn(Order.rehydrate(
                        new OrderId(ORDER_ID), new CustomerId("customer-1"), lines, OrderStatus.PENDING));

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
                .andExpect(header().string("Location", "/orders/8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01"))
                .andExpect(jsonPath("$.id").value("8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01"))
                .andExpect(jsonPath("$.customerId").value("customer-1"))
                .andExpect(jsonPath("$.lines[0].productId").value("product-1"))
                .andExpect(jsonPath("$.lines[0].quantity").value(2))
                .andExpect(jsonPath("$.lines[1].productId").value("product-2"))
                .andExpect(jsonPath("$.lines[1].quantity").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(placeOrder).place(new PlaceOrderCommand(new CustomerId("customer-1"), lines));
    }

    static Stream<Arguments> invalidBodies() {
        return Stream.of(
                Arguments.of("""
                        {"lines": [{"productId": "product-1", "quantity": 1}]}""", "customerId: must not be blank"),
                Arguments.of("""
                        {"customerId": "  ", "lines": [{"productId": "product-1", "quantity": 1}]}""", "customerId: must not be blank"),
                Arguments.of(
                        "{\"customerId\": \"" + "c".repeat(65)
                                + "\", \"lines\": [{\"productId\": \"product-1\", \"quantity\": 1}]}",
                        "customerId: must be at most 64 characters"),
                Arguments.of("""
                        {"customerId": "customer-1"}""", "lines: must not be empty"),
                Arguments.of("""
                        {"customerId": "customer-1", "lines": []}""", "lines: must not be empty"),
                Arguments.of(
                        "{\"customerId\": \"customer-1\", \"lines\": ["
                                + String.join(
                                        ",",
                                        Collections.nCopies(101, "{\"productId\": \"product-1\", \"quantity\": 1}"))
                                + "]}",
                        "lines: must have at most 100 order lines"),
                Arguments.of("""
                        {"customerId": "customer-1", "lines": [null]}""", "lines[0]: must not be null"),
                Arguments.of("""
                        {"customerId": "customer-1", "lines": [{"quantity": 1}]}""", "lines[0].productId: must not be blank"),
                Arguments.of("""
                        {"customerId": "customer-1", "lines": [{"productId": " ", "quantity": 1}]}""", "lines[0].productId: must not be blank"),
                Arguments.of(
                        "{\"customerId\": \"customer-1\", \"lines\": [{\"productId\": \"" + "p".repeat(65)
                                + "\", \"quantity\": 1}]}",
                        "lines[0].productId: must be at most 64 characters"),
                Arguments.of("""
                        {"customerId": "customer-1", "lines": [{"productId": "product-1"}]}""", "lines[0].quantity: must not be null"),
                Arguments.of("""
                        {"customerId": "customer-1", "lines": [{"productId": "product-1", "quantity": 0}]}""", "lines[0].quantity: must be at least 1"),
                Arguments.of("""
                        {"customerId": "customer-1", "lines": [{"productId": "product-1", "quantity": -1}]}""", "lines[0].quantity: must be at least 1"),
                Arguments.of("""
                        {"customerId": "customer-1", "lines": [{"productId": "product-1", "quantity": 1001}]}""", "lines[0].quantity: must be at most 1000"));
    }

    @ParameterizedTest
    @MethodSource("invalidBodies")
    void should_return_400_with_a_fixed_english_detail_and_not_place_order_when_request_body_is_invalid(
            String body, String expectedDetail) throws Exception {
        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(expectedDetail));

        verifyNoInteractions(placeOrder);
    }

    @Test
    void should_return_400_problem_detail_when_domain_refuses_the_order() throws Exception {
        when(placeOrder.place(any())).thenThrow(new EmptyOrderException());

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("An order needs at least one order line"))
                .andExpect(jsonPath("$.instance").value("/orders"))
                .andExpect(content().string(not(containsString("Exception"))))
                .andExpect(content().string(not(containsString("com.demo"))))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void should_return_400_problem_detail_naming_the_fields_when_request_body_is_invalid() throws Exception {
        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerId": " ", "lines": [{"productId": "product-1", "quantity": 0}]}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail")
                        .value("customerId: must not be blank; lines[0].quantity: must be at least 1"))
                .andExpect(content().string(not(containsString("Exception"))))
                .andExpect(content().string(not(containsString("com.demo"))));
    }

    @Test
    void should_return_400_problem_detail_when_request_body_is_malformed() throws Exception {
        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(content().string(not(containsString("Exception"))))
                .andExpect(content().string(not(containsString("com.demo"))));

        verifyNoInteractions(placeOrder);
    }

    @Test
    void should_return_200_with_the_order_when_id_is_known() throws Exception {
        var lines = List.of(new OrderLine(new ProductId("product-1"), 2), new OrderLine(new ProductId("product-2"), 1));
        when(getOrder.get(new GetOrderQuery(new OrderId(ORDER_ID))))
                .thenReturn(Order.rehydrate(
                        new OrderId(ORDER_ID), new CustomerId("customer-1"), lines, OrderStatus.PENDING));

        mockMvc.perform(get("/orders/8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01"))
                .andExpect(jsonPath("$.customerId").value("customer-1"))
                .andExpect(jsonPath("$.lines[0].productId").value("product-1"))
                .andExpect(jsonPath("$.lines[0].quantity").value(2))
                .andExpect(jsonPath("$.lines[1].productId").value("product-2"))
                .andExpect(jsonPath("$.lines[1].quantity").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void should_return_404_problem_detail_when_id_is_unknown() throws Exception {
        when(getOrder.get(new GetOrderQuery(new OrderId(UNKNOWN_ORDER_ID))))
                .thenThrow(new OrderNotFoundException(new OrderId(UNKNOWN_ORDER_ID)));

        mockMvc.perform(get("/orders/00000000-0000-4000-8000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Order 00000000-0000-4000-8000-000000000000 was not found"))
                .andExpect(jsonPath("$.instance").value("/orders/00000000-0000-4000-8000-000000000000"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist())
                .andExpect(content().string(not(containsString("Exception"))))
                .andExpect(content().string(not(containsString("com.demo"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-uuid", "<script>alert(1)", " ", "8A1D0C5E-4B7F-4C1A-9D3E-2F6B7A8C9D01"})
    void should_return_404_without_echoing_the_id_when_id_is_malformed(String malformedId) throws Exception {
        mockMvc.perform(get("/orders/{id}", malformedId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Order was not found"))
                .andExpect(jsonPath("$.instance").value("/orders"))
                .andExpect(content().string(not(containsString("not-a-uuid"))))
                .andExpect(content().string(not(containsString("script"))))
                .andExpect(content().string(not(containsString("8A1D0C5E"))))
                .andExpect(content().string(not(containsString("Exception"))))
                .andExpect(content().string(not(containsString("com.demo"))));

        verifyNoInteractions(getOrder);
    }

    @Test
    void should_return_200_with_the_cancelled_order_when_order_is_cancelled() throws Exception {
        var lines = List.of(new OrderLine(new ProductId("product-1"), 2));
        when(cancelOrder.cancel(new CancelOrderCommand(new OrderId(ORDER_ID))))
                .thenReturn(Order.rehydrate(
                        new OrderId(ORDER_ID), new CustomerId("customer-1"), lines, OrderStatus.CANCELLED));

        mockMvc.perform(post("/orders/8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01"))
                .andExpect(jsonPath("$.customerId").value("customer-1"))
                .andExpect(jsonPath("$.lines[0].productId").value("product-1"))
                .andExpect(jsonPath("$.lines[0].quantity").value(2))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void should_return_404_problem_detail_when_cancelling_an_unknown_id() throws Exception {
        when(cancelOrder.cancel(new CancelOrderCommand(new OrderId(UNKNOWN_ORDER_ID))))
                .thenThrow(new OrderNotFoundException(new OrderId(UNKNOWN_ORDER_ID)));

        mockMvc.perform(post("/orders/00000000-0000-4000-8000-000000000000/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Order 00000000-0000-4000-8000-000000000000 was not found"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(content().string(not(containsString("Exception"))))
                .andExpect(content().string(not(containsString("com.demo"))));
    }

    static Stream<Arguments> refusedStates() {
        return Stream.of(
                Arguments.of(
                        OrderStatus.SHIPPED,
                        "Order 8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01 cannot be cancelled because it is SHIPPED",
                        "SHIPPED"),
                Arguments.of(
                        OrderStatus.CANCELLED,
                        "Order 8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01 cannot be cancelled because it is CANCELLED",
                        "CANCELLED"));
    }

    @ParameterizedTest
    @MethodSource("refusedStates")
    void should_return_409_problem_detail_with_current_state_when_order_cannot_be_cancelled(
            OrderStatus state, String expectedDetail, String expectedState) throws Exception {
        when(cancelOrder.cancel(new CancelOrderCommand(new OrderId(ORDER_ID))))
                .thenThrow(new OrderCannotBeCancelledException(new OrderId(ORDER_ID), state));

        mockMvc.perform(post("/orders/8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01/cancel"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value(expectedDetail))
                .andExpect(jsonPath("$.currentState").value(expectedState))
                .andExpect(jsonPath("$.instance").value("/orders/8a1d0c5e-4b7f-4c1a-9d3e-2f6b7a8c9d01/cancel"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist())
                .andExpect(content().string(not(containsString("Exception"))))
                .andExpect(content().string(not(containsString("com.demo"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-uuid", "<script>alert(1)", " ", "8A1D0C5E-4B7F-4C1A-9D3E-2F6B7A8C9D01"})
    void should_return_404_without_echoing_the_id_when_cancelling_a_malformed_id(String malformedId) throws Exception {
        mockMvc.perform(post("/orders/{id}/cancel", malformedId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Order was not found"))
                .andExpect(jsonPath("$.instance").value("/orders"))
                .andExpect(content().string(not(containsString("not-a-uuid"))))
                .andExpect(content().string(not(containsString("script"))))
                .andExpect(content().string(not(containsString("8A1D0C5E"))))
                .andExpect(content().string(not(containsString("Exception"))))
                .andExpect(content().string(not(containsString("com.demo"))));

        verifyNoInteractions(cancelOrder);
    }
}
