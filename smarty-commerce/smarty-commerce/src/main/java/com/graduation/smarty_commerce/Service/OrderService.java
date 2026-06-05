package com.graduation.smarty_commerce.Service;

import com.graduation.smarty_commerce.shared.dto.OrderDto;

import java.util.List;

public interface OrderService {
    OrderDto createOrder(String userId, OrderDto orderDetails);
    OrderDto getOrder(String orderId);
    List<OrderDto> getOrders(String userId);
    List<OrderDto> getAllOrders(int page, int limit);
    OrderDto cancelOrder(String userId, String orderId);
    void deleteOrder(String orderId);
}
