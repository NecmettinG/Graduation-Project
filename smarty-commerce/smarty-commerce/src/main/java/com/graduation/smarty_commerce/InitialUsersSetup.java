package com.graduation.smarty_commerce;

import com.graduation.smarty_commerce.io.Entity.AuthorityEntity;
import com.graduation.smarty_commerce.io.Entity.RoleEntity;
import com.graduation.smarty_commerce.io.Entity.UserEntity;
import com.graduation.smarty_commerce.io.Repository.AuthorityRepository;
import com.graduation.smarty_commerce.io.Repository.RoleRepository;
import com.graduation.smarty_commerce.io.Repository.UserRepository;
import com.graduation.smarty_commerce.shared.Roles;
import com.graduation.smarty_commerce.shared.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;

@Component
public class InitialUsersSetup {

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private Utils utils;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @EventListener
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event){

        System.out.println("From Application Ready Event...\nonApplication method is started.");

        AuthorityEntity readAuthority = createAuthority("READ_AUTHORITY");
        AuthorityEntity writeAuthority = createAuthority("WRITE_AUTHORITY");
        AuthorityEntity deleteAuthority = createAuthority("DELETE_AUTHORITY");

        createRole(Roles.ROLE_USER.name(), Arrays.asList(readAuthority, writeAuthority));
        RoleEntity roleAdmin = createRole(Roles.ROLE_ADMIN.name(), Arrays.asList(readAuthority, writeAuthority, deleteAuthority));

        if(roleAdmin == null){
            return;
        }

        UserEntity adminUser = new UserEntity();
        adminUser.setFirstName("Boss");
        adminUser.setLastName("Big Boss");
        adminUser.setEmail("TheBigBoss@gmail.com");
        adminUser.setEmailVerificationStatus(true);
        adminUser.setUserId(utils.generateId(30));
        adminUser.setEncryptedPassword(bCryptPasswordEncoder.encode("Diamond Dawgs"));
        adminUser.setRoles(Arrays.asList(roleAdmin));

        userRepository.save(adminUser);
    }

    @Transactional
    protected AuthorityEntity createAuthority(String name){

        AuthorityEntity authority = authorityRepository.findByName(name);

        if(authority == null){

            authority = new AuthorityEntity(name);
            authorityRepository.save(authority);
        }
        return authority;
    }

    @Transactional
    protected RoleEntity createRole(String name, Collection<AuthorityEntity> authorities){

        RoleEntity role = roleRepository.findByName(name);

        if(role == null){

            role = new RoleEntity(name);
            role.setAuthorities(authorities);
            roleRepository.save(role);
        }

        return role;
    }
}
