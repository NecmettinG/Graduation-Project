package com.graduation.smarty_commerce.Service.impl;

import com.graduation.smarty_commerce.Service.OrderService;
import com.graduation.smarty_commerce.Exceptions.OrderServiceException;
import com.graduation.smarty_commerce.io.Entity.*;
import com.graduation.smarty_commerce.io.Repository.*;
import com.graduation.smarty_commerce.shared.OrderStatus;
import com.graduation.smarty_commerce.shared.Utils;
import com.graduation.smarty_commerce.shared.dto.OrderDto;
import com.graduation.smarty_commerce.ui.Model.Request.OrderRequestModel;
import com.graduation.smarty_commerce.ui.Model.Response.ErrorMessages;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private Utils utils;

    @Override
    public OrderDto createOrder(String userId, OrderRequestModel orderDetails) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new OrderServiceException(ErrorMessages.USER_NOT_FOUND.getErrorMessage());

        CartEntity cartEntity = userEntity.getCart();
        if (cartEntity == null || cartEntity.getCartItems() == null || cartEntity.getCartItems().isEmpty()) {
            throw new OrderServiceException(ErrorMessages.CART_IS_EMPTY.getErrorMessage());
        }

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId(utils.generateId(30));
        orderEntity.setUser(userEntity);
        orderEntity.setOrderDate(new Date());
        orderEntity.setOrderStatus(OrderStatus.PENDING);
        orderEntity.setShippingAddress(orderDetails.getShippingAddress());

        List<OrderItemEntity> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItemEntity cartItem : cartEntity.getCartItems()) {
            ProductEntity product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new OrderServiceException(ErrorMessages.NOT_ENOUGH_STOCK.getErrorMessage() + ": " + product.getProductName());
            }

            // Deduct stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setOrderItemId(utils.generateId(30));
            orderItem.setProduct(product);
            orderItem.setOrder(orderEntity);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(product.getPrice());

            // Calculate total for this item
            BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            orderItems.add(orderItem);
        }

        orderEntity.setOrderItems(orderItems);
        orderEntity.setTotalAmount(totalAmount);

        // Save order
        OrderEntity storedOrder = orderRepository.save(orderEntity);

        // Clear user's cart
        cartEntity.getCartItems().clear();
        cartEntity.setCurrentTotal(BigDecimal.ZERO);
        cartRepository.save(cartEntity);

        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(storedOrder, OrderDto.class);
    }

    @Override
    public OrderDto getOrder(String orderId) {
        OrderEntity orderEntity = orderRepository.findByOrderId(orderId);
        if (orderEntity == null) throw new OrderServiceException(ErrorMessages.ORDER_NOT_FOUND.getErrorMessage());

        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(orderEntity, OrderDto.class);
    }

    @Override
    public List<OrderDto> getOrders(String userId) {
        List<OrderEntity> orderEntities = orderRepository.findByUserUserId(userId);

        ModelMapper modelMapper = new ModelMapper();
        Type listType = new TypeToken<List<OrderDto>>() {}.getType();
        return modelMapper.map(orderEntities, listType);
    }

    @Override
    public OrderDto cancelOrder(String userId, String orderId) {
        OrderEntity orderEntity = orderRepository.findByOrderId(orderId);
        if (orderEntity == null) throw new OrderServiceException(ErrorMessages.ORDER_NOT_FOUND.getErrorMessage());

        if (!orderEntity.getUser().getUserId().equals(userId)) {
            throw new OrderServiceException(ErrorMessages.UNAUTHORIZED_ORDER_ACTION.getErrorMessage());
        }

        if (orderEntity.getOrderStatus() != OrderStatus.PENDING) {
            throw new OrderServiceException(ErrorMessages.ORDER_CANCELLATION_NOT_ALLOWED.getErrorMessage());
        }

        orderEntity.setOrderStatus(OrderStatus.CANCELLED);

        // Restore stock
        for (OrderItemEntity orderItem : orderEntity.getOrderItems()) {
            ProductEntity product = orderItem.getProduct();
            product.setStock(product.getStock() + orderItem.getQuantity());
            productRepository.save(product);
        }

        OrderEntity updatedOrder = orderRepository.save(orderEntity);

        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(updatedOrder, OrderDto.class);
    }

    @Override
    public void deleteOrder(String orderId) {
        OrderEntity orderEntity = orderRepository.findByOrderId(orderId);
        if (orderEntity == null) throw new OrderServiceException(ErrorMessages.ORDER_NOT_FOUND.getErrorMessage());

        orderRepository.delete(orderEntity);
    }
}
