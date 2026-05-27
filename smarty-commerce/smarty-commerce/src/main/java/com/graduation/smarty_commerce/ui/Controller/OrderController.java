package com.graduation.smarty_commerce.ui.Controller;

import com.graduation.smarty_commerce.Service.OrderService;
import com.graduation.smarty_commerce.Service.impl.OrderServiceImpl;
import com.graduation.smarty_commerce.shared.dto.OrderDto;
import com.graduation.smarty_commerce.ui.Model.Request.OrderRequestModel;
import com.graduation.smarty_commerce.ui.Model.Response.OrderRest;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/orders")
public class OrderController {

    @Autowired
    private OrderServiceImpl orderService;

    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    @PostMapping
    public ResponseEntity<OrderRest> createOrder(@PathVariable String userId, @RequestBody OrderRequestModel orderDetails) {
        OrderDto createdOrder = orderService.createOrder(userId, orderDetails);

        ModelMapper modelMapper = new ModelMapper();
        OrderRest returnValue = modelMapper.map(createdOrder, OrderRest.class);

        return new ResponseEntity<>(returnValue, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    @GetMapping(path = "/{orderId}")
    public OrderRest getOrder(@PathVariable String userId, @PathVariable String orderId) {
        // Technically userId could be validated against the order here
        OrderDto orderDto = orderService.getOrder(orderId);

        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(orderDto, OrderRest.class);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    @GetMapping
    public List<OrderRest> getOrders(@PathVariable String userId) {
        List<OrderDto> orders = orderService.getOrders(userId);

        ModelMapper modelMapper = new ModelMapper();
        Type listType = new TypeToken<List<OrderRest>>() {}.getType();
        return modelMapper.map(orders, listType);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    @PutMapping(path = "/{orderId}/cancel")
    public OrderRest cancelOrder(@PathVariable String userId, @PathVariable String orderId) {
        OrderDto cancelledOrder = orderService.cancelOrder(userId, orderId);

        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(cancelledOrder, OrderRest.class);
    }
}
