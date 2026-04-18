package com.graduation.smarty_commerce.ui.Controller;

import com.graduation.smarty_commerce.Service.impl.AddressServiceImpl;
import com.graduation.smarty_commerce.Service.impl.UserServiceImpl;
import com.graduation.smarty_commerce.shared.dto.UserDto;
import com.graduation.smarty_commerce.ui.Model.Response.UserRest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserServiceImpl userService;

//    @Autowired
//    AddressServiceImpl addressService;

    //@PostAuthorize("hasRole('ADMIN') or returnObject.userId == principal.userId")
    @GetMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserRest getUser(@PathVariable("id") String id){

        UserRest returnValue;

        UserDto userDto = userService.getUserByUserId(id);

        ModelMapper modelMapper = new ModelMapper();

        returnValue = modelMapper.map(userDto, UserRest.class);

        return returnValue;
    }
}
