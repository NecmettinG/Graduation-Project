package com.graduation.smarty_commerce.Service.impl;

import com.graduation.smarty_commerce.Exceptions.UserServiceException;
import com.graduation.smarty_commerce.Security.UserPrincipal;
import com.graduation.smarty_commerce.Service.UserService;
import com.graduation.smarty_commerce.io.Entity.AddressEntity;
import com.graduation.smarty_commerce.io.Entity.PasswordResetTokenEntity;
import com.graduation.smarty_commerce.io.Entity.RoleEntity;
import com.graduation.smarty_commerce.io.Entity.UserEntity;
import com.graduation.smarty_commerce.io.Repository.PasswordResetTokenRepository;
import com.graduation.smarty_commerce.io.Repository.RoleRepository;
import com.graduation.smarty_commerce.io.Repository.UserRepository;
import com.graduation.smarty_commerce.shared.AmazonSES;
import com.graduation.smarty_commerce.shared.Utils;
import com.graduation.smarty_commerce.shared.dto.AddressDto;
import com.graduation.smarty_commerce.shared.dto.UserDto;
import com.graduation.smarty_commerce.ui.Model.Response.ErrorMessages;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

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

    @Autowired
    AmazonSES amazonSES;

    @Autowired
    RoleRepository roleRepository;


    @Override
    @Transactional
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

    //When you tried to login, ModelMapper was trying to map the UserEntity to UserDto in UserServiceImpl.getUser(String email) which is-
    //-called in AuthenticationFilter upon successful authentication. Because your UserEntity has related entities (like @OneToMany for-
    //-addresses and orders) combined with the default fetch type being LAZY, the ModelMapper couldn't access these collections because
    //-it was executing outside of an active database session. And LazyInitializationException was thrown, resulting in an HTTP 500 error.

    //By adding the @Transactional annotation, you ensure that the Hibernate session remains open for the duration of the method-
    // -so that the collections can be fully lazily initialized when ModelMapper builds out UserDto.
    @Override
    @Transactional
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
    public List<UserDto> getUsers(int page, int limit){

        List<UserDto> returnValue = new ArrayList<>();

        if(page>0){

            page -=1;
        }

        else{

            throw new UserServiceException(ErrorMessages.INVALID_PAGE_NUMBER.getErrorMessage());
        }

        Pageable pageableRequest = PageRequest.of(page, limit);

        Page<UserEntity> usersPage = userRepository.findAll(pageableRequest);

        List<UserEntity> users = usersPage.getContent();

        ModelMapper modelMapper = new ModelMapper();

        for(UserEntity userEntity : users){

            UserDto userDto = new UserDto();

            userDto = modelMapper.map(userEntity, UserDto.class);

            returnValue.add(userDto);
        }

        return returnValue;
    }

    @Override
    public UserDto updateUser(String userId, UserDto user){

        UserEntity userEntity = userRepository.findByUserId(userId);

        ModelMapper modelMapper = new ModelMapper();

        if(userEntity == null){

            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }

        if (user.getFirstName() != null) {
            userEntity.setFirstName(user.getFirstName());
        }
        if (user.getLastName() != null) {
            userEntity.setLastName(user.getLastName());
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        }

        if (user.getAddresses() != null) {
            userEntity.getAddresses().clear();

            for (AddressDto addressDto : user.getAddresses()) {
                addressDto.setAddressId(utils.generateId(30));
                addressDto.setUserDetails(modelMapper.map(userEntity, UserDto.class));
                AddressEntity addressEntity = modelMapper.map(addressDto, AddressEntity.class);
                addressEntity.setUserDetails(userEntity);
                userEntity.getAddresses().add(addressEntity);
            }
        }

        UserEntity updatedUserDetails = userRepository.save(userEntity);

        UserDto returnValue = modelMapper.map(updatedUserDetails, UserDto.class);

        return returnValue;
    }

    @Override
    public void deleteUser(String userId){

        UserEntity userEntity = userRepository.findByUserId(userId);

        if(userEntity == null){

            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }

        userRepository.delete(userEntity);
    }


    @Override
    public boolean verifyEmailToken(String token){

        boolean returnValue = false;

        UserEntity userEntity = userRepository.findUserByEmailVerificationToken(token);

        if(userEntity != null){

            boolean hasTokenExpired = Utils.hasTokenExpired(token);

            if(!hasTokenExpired){


                userEntity.setEmailVerificationToken(null);
                userEntity.setEmailVerificationStatus(Boolean.TRUE);
                userRepository.save(userEntity);
                returnValue = true;
            }
        }

        return returnValue;
    }


    @Override
    public boolean requestPasswordReset(String email){

        boolean returnValue = false;

        UserEntity userEntity = userRepository.findByEmail(email);

        if(userEntity == null){

            return returnValue;
        }

        String token = utils.generatePasswordResetToken(userEntity.getUserId());

        PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
        passwordResetTokenEntity.setToken(token);
        passwordResetTokenEntity.setUserDetails(userEntity);

        passwordResetTokenRepository.save(passwordResetTokenEntity);

        returnValue = new AmazonSES().sendPasswordResetRequest(
                userEntity.getFirstName(),
                userEntity.getEmail(),
                token
        );

        return returnValue;
    }

    @Override
    public boolean resetPassword(String token, String password) {

        boolean returnValue = false;

        PasswordResetTokenEntity passwordResetTokenEntity = passwordResetTokenRepository.findByToken(token);

        if (passwordResetTokenEntity == null){

            return returnValue;
        }

        UserEntity userEntity = passwordResetTokenEntity.getUserDetails();

        if (userEntity != null){

            boolean hasTokenExpired = Utils.hasTokenExpired(token);

            if(!hasTokenExpired){

                String encodedPassword = bCryptPasswordEncoder.encode(password);
                userEntity.setEncryptedPassword(encodedPassword);
                UserEntity savedUserEntity = userRepository.save(userEntity);

                if(savedUserEntity != null && savedUserEntity.getEncryptedPassword().equalsIgnoreCase(encodedPassword)) {
                    returnValue = true;
                }

                passwordResetTokenRepository.delete(passwordResetTokenEntity);
            }

        }

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
