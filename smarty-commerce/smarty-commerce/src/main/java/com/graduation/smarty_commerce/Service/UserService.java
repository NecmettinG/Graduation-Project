package com.graduation.smarty_commerce.Service;

import com.graduation.smarty_commerce.shared.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    public UserDto getUserByUserId(String userId);
    public UserDto createUser(UserDto userDto);
}
