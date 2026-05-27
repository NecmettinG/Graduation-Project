package com.graduation.smarty_commerce.Service;

import com.graduation.smarty_commerce.shared.dto.OrderDto;
import com.graduation.smarty_commerce.ui.Model.Request.OrderRequestModel;

import java.util.List;

public interface OrderService {
    OrderDto createOrder(String userId, OrderRequestModel orderDetails);
    OrderDto getOrder(String orderId);
    List<OrderDto> getOrders(String userId);
}

