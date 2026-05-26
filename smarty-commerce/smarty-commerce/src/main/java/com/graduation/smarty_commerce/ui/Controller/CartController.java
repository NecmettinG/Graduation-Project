package com.graduation.smarty_commerce.ui.Controller;

import com.graduation.smarty_commerce.Service.CartService;
import com.graduation.smarty_commerce.Service.impl.CartServiceImpl;
import com.graduation.smarty_commerce.shared.dto.CartDto;
import com.graduation.smarty_commerce.ui.Model.Request.CartItemRequestModel;
import com.graduation.smarty_commerce.ui.Model.Response.CartRest;
import com.graduation.smarty_commerce.ui.Model.Response.OperationStatusModel;
import com.graduation.smarty_commerce.ui.Model.Response.RequestOperationName;
import com.graduation.smarty_commerce.ui.Model.Response.RequestOperationStatus;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/cart")
public class CartController {

    @Autowired
    CartServiceImpl cartService;

    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    @GetMapping
    public CartRest getCart(@PathVariable String userId) {
        CartDto cartDto = cartService.getCartByUserId(userId);
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper.map(cartDto, CartRest.class);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    @PostMapping("/items")
    public CartRest addCartItem(@PathVariable String userId, @RequestBody CartItemRequestModel cartItemDetails) {
        CartDto cartDto = cartService.addCartItem(userId, cartItemDetails);
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper.map(cartDto, CartRest.class);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    @PutMapping("/items/{cartItemId}")
    public CartRest updateCartItem(@PathVariable String userId, @PathVariable String cartItemId, @RequestBody CartItemRequestModel cartItemDetails) {
        CartDto cartDto = cartService.updateCartItem(userId, cartItemId, cartItemDetails);
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper.map(cartDto, CartRest.class);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    @DeleteMapping("/items/{cartItemId}")
    public OperationStatusModel removeCartItem(@PathVariable String userId, @PathVariable String cartItemId) {
        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.DELETE.name());

        cartService.removeCartItem(userId, cartItemId);

        returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        return returnValue;
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    @DeleteMapping
    public OperationStatusModel clearCart(@PathVariable String userId) {
        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.DELETE.name());

        cartService.clearCart(userId);

        returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        return returnValue;
    }
}
