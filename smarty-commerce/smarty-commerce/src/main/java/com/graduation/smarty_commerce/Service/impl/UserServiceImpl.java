package com.graduation.smarty_commerce.Service.impl;

import com.graduation.smarty_commerce.Security.UserPrincipal;
import com.graduation.smarty_commerce.Service.UserService;
import com.graduation.smarty_commerce.io.Entity.UserEntity;
import com.graduation.smarty_commerce.io.Repository.PasswordResetTokenRepository;
import com.graduation.smarty_commerce.io.Repository.RoleRepository;
import com.graduation.smarty_commerce.io.Repository.UserRepository;
import com.graduation.smarty_commerce.shared.Utils;
import com.graduation.smarty_commerce.shared.dto.UserDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    Utils utils;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

//    @Autowired
//    AmazonSES amazonSES;

    @Autowired
    RoleRepository roleRepository;


    @Override
    public UserDto getUserByUserId(String userId){

        UserEntity userEntity = userRepository.findByUserId(userId);

        if(userEntity == null){

            throw new UsernameNotFoundException("User not found: "+ userId);
        }

        UserDto returnValue;

        ModelMapper modelMapper = new ModelMapper();

        returnValue = modelMapper.map(userEntity, UserDto.class);

        return returnValue;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity userEntity = userRepository.findByEmail(username);

        if(userEntity == null){

            throw new UsernameNotFoundException("email not found:" + username);
        }


        return new UserPrincipal(userEntity);
    }
}
