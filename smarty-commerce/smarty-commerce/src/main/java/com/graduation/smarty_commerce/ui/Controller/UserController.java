package com.graduation.smarty_commerce.ui.Controller;

import com.graduation.smarty_commerce.Exceptions.UserServiceException;
import com.graduation.smarty_commerce.Service.impl.AddressServiceImpl;
import com.graduation.smarty_commerce.Service.impl.UserServiceImpl;
import com.graduation.smarty_commerce.shared.Roles;
import com.graduation.smarty_commerce.shared.dto.UserDto;
import com.graduation.smarty_commerce.ui.Model.Request.UserDetailsRequestModel;
import com.graduation.smarty_commerce.ui.Model.Response.ErrorMessages;
import com.graduation.smarty_commerce.ui.Model.Response.UserRest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserServiceImpl userService;

//    @Autowired
//    AddressServiceImpl addressService;

    @PostAuthorize("hasRole('ADMIN') or returnObject.userId == principal.userId")
    @GetMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserRest getUser(@PathVariable("id") String id){

        UserRest returnValue;

        UserDto userDto = userService.getUserByUserId(id);

        ModelMapper modelMapper = new ModelMapper();

        returnValue = modelMapper.map(userDto, UserRest.class);

        return returnValue;
    }

    @PostMapping(
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) throws UserServiceException {

        UserRest returnValue = new UserRest();

        if(userDetails.getFirstName().isEmpty()){

            throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
        }

        ModelMapper modelMapper = new ModelMapper();

        UserDto userDto = modelMapper.map(userDetails, UserDto.class);

        userDto.setRoles(new HashSet<>(Arrays.asList(Roles.ROLE_USER.name())));

        UserDto createdUser = userService.createUser(userDto);

        returnValue = modelMapper.map(createdUser, UserRest.class);

        return returnValue;
    }
}
