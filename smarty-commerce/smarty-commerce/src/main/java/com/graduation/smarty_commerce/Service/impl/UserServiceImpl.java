package com.graduation.smarty_commerce.Service.impl;

import com.graduation.smarty_commerce.Exceptions.UserServiceException;
import com.graduation.smarty_commerce.Security.UserPrincipal;
import com.graduation.smarty_commerce.Service.UserService;
import com.graduation.smarty_commerce.io.Entity.RoleEntity;
import com.graduation.smarty_commerce.io.Entity.UserEntity;
import com.graduation.smarty_commerce.io.Repository.PasswordResetTokenRepository;
import com.graduation.smarty_commerce.io.Repository.RoleRepository;
import com.graduation.smarty_commerce.io.Repository.UserRepository;
import com.graduation.smarty_commerce.shared.Utils;
import com.graduation.smarty_commerce.shared.dto.AddressDto;
import com.graduation.smarty_commerce.shared.dto.UserDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;

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
    public UserDto createUser(UserDto user){

        if(userRepository.findByEmail(user.getEmail()) != null){

            throw new UserServiceException("Email already exists!");
        }

        for(int i = 0; i < user.getAddresses().size(); i++){

            AddressDto addressDto = user.getAddresses().get(i);
            addressDto.setUserDetails(user);


            addressDto.setAddressId(utils.generateId(30));

            user.getAddresses().set(i, addressDto);
        }

        ModelMapper modelMapper = new ModelMapper();

        UserEntity userEntity = modelMapper.map(user, UserEntity.class);

        String publicUserId = utils.generateId(30);

        userEntity.setUserId(publicUserId);

        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        userEntity.setEmailVerificationToken(utils.generateEmailVerificationToken(publicUserId));

        userEntity.setEmailVerificationStatus(false);


        Collection<RoleEntity> roleEntities = new HashSet<>();

        for(String role : user.getRoles()){

            RoleEntity roleEntity = roleRepository.findByName(role);

            if(roleEntity != null){

                roleEntities.add(roleEntity);
            }
        }

        userEntity.setRoles(roleEntities);

        UserEntity storedUserDetails = userRepository.save(userEntity);


        UserDto returnValue = modelMapper.map(storedUserDetails, UserDto.class);


        //amazonSES.verifyEmail(returnValue);

        return returnValue;
    }

    @Override
    public UserDto getUser(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);

        if(userEntity == null){
            throw new UsernameNotFoundException("email not found:" + email);
        }

        UserDto returnValue = new UserDto();

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
