package com.graduation.smarty_commerce.ui.Controller;

import com.graduation.smarty_commerce.Exceptions.UserServiceException;
import com.graduation.smarty_commerce.Service.AddressService;
import com.graduation.smarty_commerce.Service.impl.AddressServiceImpl;
import com.graduation.smarty_commerce.Service.impl.CommentServiceImpl;
import com.graduation.smarty_commerce.Service.impl.UserServiceImpl;
import com.graduation.smarty_commerce.shared.Roles;
import com.graduation.smarty_commerce.shared.dto.AddressDto;
import com.graduation.smarty_commerce.shared.dto.CommentDto;
import com.graduation.smarty_commerce.shared.dto.UserDto;
import com.graduation.smarty_commerce.ui.Model.Request.AddressRequestModel;
import com.graduation.smarty_commerce.ui.Model.Request.CommentRequestModel;
import com.graduation.smarty_commerce.ui.Model.Request.PasswordResetModel;
import com.graduation.smarty_commerce.ui.Model.Request.PasswordResetRequestModel;
import com.graduation.smarty_commerce.ui.Model.Request.UserDetailsRequestModel;
import com.graduation.smarty_commerce.ui.Model.Response.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.modelmapper.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserServiceImpl userService;

    @Autowired
    AddressServiceImpl addressService;

    @Autowired
    CommentServiceImpl commentService;

    @PostAuthorize("hasRole('ADMIN') or returnObject.userId == principal.userId")
    @GetMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserRest getUser(@PathVariable("id") String id){

        UserRest returnValue;

        UserDto userDto = userService.getUserByUserId(id);

        ModelMapper modelMapper = new ModelMapper();

        returnValue = modelMapper.map(userDto, UserRest.class);

        return returnValue;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "1") int page,
                                   @RequestParam(value = "limit", defaultValue = "25") int limit) {

        List<UserRest> returnValue = new ArrayList<>();

        List<UserDto> users = userService.getUsers(page, limit);

        ModelMapper modelMapper = new ModelMapper();

        for (UserDto userDto : users) {

            UserRest userModel = new UserRest();

            userModel = modelMapper.map(userDto, UserRest.class);

            returnValue.add(userModel);
        }

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

    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    @PutMapping(
            path = "/{id}",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public UserRest updateUser(@RequestBody UserDetailsRequestModel userDetails, @PathVariable("id") String id) {

        UserDto userDto = new UserDto();

        ModelMapper modelMapper = new ModelMapper();

        userDto = modelMapper.map(userDetails, UserDto.class);

        UserDto updatedUser = userService.updateUser(id, userDto);

        UserRest returnValue = modelMapper.map(updatedUser, UserRest.class);

        return returnValue;
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    @DeleteMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public OperationStatusModel deleteUser(@PathVariable("id") String id) {

        OperationStatusModel returnValue = new OperationStatusModel();


        returnValue.setOperationName(RequestOperationName.DELETE.name());

        userService.deleteUser(id);

        returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());

        return returnValue;
    }

    @GetMapping(path = "email-verification", produces ={MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public OperationStatusModel verifyEmailToken(@RequestParam(value = "token") String token){

        OperationStatusModel returnValue = new OperationStatusModel();

        returnValue.setOperationName(RequestOperationName.VERIFY_EMAIL.name());

        boolean isVerified = userService.verifyEmailToken(token);

        if(isVerified){

            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        }
        else{

            returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
        }

        return returnValue;
    }

    @PostMapping(path = "/password-reset-request",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public OperationStatusModel requestReset(@RequestBody PasswordResetRequestModel passwordResetRequestModel){

        OperationStatusModel returnValue = new OperationStatusModel();

        boolean operationResult = userService.requestPasswordReset(passwordResetRequestModel.getEmail());

        returnValue.setOperationName(RequestOperationName.REQUEST_PASSWORD_RESET.name());
        returnValue.setOperationResult(RequestOperationStatus.ERROR.name());

        if(operationResult){

            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        }

        return returnValue;
    }

    @PostMapping(path = "/password-reset",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public OperationStatusModel resetPassword(@RequestBody PasswordResetModel passwordResetModel){

        OperationStatusModel returnValue = new OperationStatusModel();

        boolean operationResult = userService.resetPassword(
                passwordResetModel.getToken(),
                passwordResetModel.getPassword()
        );

        returnValue.setOperationName(RequestOperationName.PASSWORD_RESET.name());
        returnValue.setOperationResult(RequestOperationStatus.ERROR.name());

        if(operationResult){

            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        }

        return returnValue;
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    @GetMapping(path = "/{id}/addresses", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<AddressRest> getUserAddresses(@PathVariable("id") String id) {

        List<AddressRest> returnValue = new ArrayList<>();

        List<AddressDto> addressDtos = addressService.getAddresses(id);

        if (addressDtos != null && !addressDtos.isEmpty()) {
            Type listType = new TypeToken<List<AddressRest>>() {}.getType();
            returnValue = new ModelMapper().map(addressDtos, listType);
        }

        return returnValue;
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    @GetMapping(path = "/{id}/addresses/{addressId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public AddressRest getUserAddress(@PathVariable("id") String id, @PathVariable("addressId") String addressId) {

        AddressDto addressDto = addressService.getAddress(addressId);

        ModelMapper modelMapper = new ModelMapper();

        return modelMapper.map(addressDto, AddressRest.class);
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    @PostMapping(path = "/{id}/addresses",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public AddressRest createAddress(@PathVariable("id") String id, @RequestBody AddressRequestModel addressRequestModel) {

        ModelMapper modelMapper = new ModelMapper();
        AddressDto addressDto = modelMapper.map(addressRequestModel, AddressDto.class);

        AddressDto createdAddress = addressService.createAddress(id, addressDto);

        return modelMapper.map(createdAddress, AddressRest.class);
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    @PutMapping(path = "/{id}/addresses/{addressId}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public AddressRest updateAddress(@PathVariable("id") String id,
                                     @PathVariable("addressId") String addressId,
                                     @RequestBody AddressRequestModel addressRequestModel) {

        ModelMapper modelMapper = new ModelMapper();
        AddressDto addressDto = modelMapper.map(addressRequestModel, AddressDto.class);

        AddressDto updatedAddress = addressService.updateAddress(addressId, addressDto);

        return modelMapper.map(updatedAddress, AddressRest.class);
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    @DeleteMapping(path = "/{id}/addresses/{addressId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public OperationStatusModel deleteAddress(@PathVariable("id") String id, @PathVariable("addressId") String addressId) {

        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.DELETE.name());

        addressService.deleteAddress(addressId);

        returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());

        return returnValue;
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    @GetMapping(path = "/{id}/comments", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<CommentRest> getUserComments(@PathVariable("id") String id) {

        List<CommentDto> comments = commentService.getUserComments(id);

        Type listType = new TypeToken<List<CommentRest>>() {}.getType();
        return new ModelMapper().map(comments, listType);
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    @PutMapping(path = "/{id}/comments/{commentId}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public CommentRest updateComment(@PathVariable("id") String id,
                                     @PathVariable("commentId") String commentId,
                                     @RequestBody CommentRequestModel commentDetails) {

        ModelMapper modelMapper = new ModelMapper();
        CommentDto commentDto = modelMapper.map(commentDetails, CommentDto.class);
        
        CommentDto updateComment = commentService.updateComment(commentId, id, commentDto);
        return modelMapper.map(updateComment, CommentRest.class);
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    @DeleteMapping(path = "/{id}/comments/{commentId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public OperationStatusModel deleteComment(@PathVariable("id") String id, @PathVariable("commentId") String commentId) {

        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.DELETE.name());

        commentService.deleteComment(commentId, id);

        returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());

        return returnValue;
    }
}
