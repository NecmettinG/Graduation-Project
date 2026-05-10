package com.graduation.smarty_commerce.Service;

import com.graduation.smarty_commerce.shared.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    public UserDto getUserByUserId(String userId);
    public UserDto createUser(UserDto userDto);
    public UserDto getUser(String email);
    public List<UserDto> getUsers(int page, int limit);
    public UserDto updateUser(String userId,UserDto user);
    public void deleteUser(String userId);
    public boolean verifyEmailToken(String token);
    public boolean requestPasswordReset(String email);
    public boolean resetPassword(String token, String password);
}
