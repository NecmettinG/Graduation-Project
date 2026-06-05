package com.graduation.smarty_commerce.ui.Controller;

import com.graduation.smarty_commerce.Service.OrderService;
import com.graduation.smarty_commerce.shared.dto.OrderDto;
import com.graduation.smarty_commerce.ui.Model.Response.OrderRest;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Type;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class GlobalOrderController {

    @Autowired
    private OrderService orderService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<OrderRest> getAllOrders(@RequestParam(value = "page", defaultValue = "1") int page,
                                        @RequestParam(value = "limit", defaultValue = "25") int limit) {

        List<OrderDto> orders = orderService.getAllOrders(page, limit);

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        Type listType = new TypeToken<List<OrderRest>>() {}.getType();
        
        return modelMapper.map(orders, listType);
    }
}
