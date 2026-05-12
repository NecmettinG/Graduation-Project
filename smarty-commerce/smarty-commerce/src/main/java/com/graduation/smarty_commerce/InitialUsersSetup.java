package com.graduation.smarty_commerce;

import com.graduation.smarty_commerce.io.Entity.*;
import com.graduation.smarty_commerce.io.Repository.AuthorityRepository;
import com.graduation.smarty_commerce.io.Repository.MainCategoryRepository;
import com.graduation.smarty_commerce.io.Repository.RoleRepository;
import com.graduation.smarty_commerce.io.Repository.UserRepository;
import com.graduation.smarty_commerce.io.Repository.CategoryRepository;
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

    @Autowired
    private MainCategoryRepository mainCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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

        if(userRepository.findByEmail("TheBigBoss@gmail.com") == null) {
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

        initializeMainCategories();
        initializeSubCategories();
    }

    @Transactional
    protected void initializeMainCategories() {
        String[] categories = {"Electronics", "Clothing(Male)", "Clothing(Female)", "Home and Furniture", "Cosmetic", "Supermarket", "Sports", "Stationary"};
        for (String categoryName : categories) {
            MainCategoryEntity category = mainCategoryRepository.findByCategoryName(categoryName);
            if (category == null) {
                category = new MainCategoryEntity();
                category.setCategoryId(utils.generateId(10));
                category.setCategoryName(categoryName);
                mainCategoryRepository.save(category);
            }
        }
    }

    @Transactional
    protected void initializeSubCategories() {
        MainCategoryEntity electronics = mainCategoryRepository.findByCategoryName("Electronics");
        if (electronics != null) {
            String[] subCategories = {"Phones", "Computers", "Wearable Tech", "TV and Sound", "Camera", "Peripheral Devices", "Consoles"};
            for (String subCategoryName : subCategories) {
                CategoryEntity category = categoryRepository.findByCategoryName(subCategoryName);
                if (category == null) {
                    category = new CategoryEntity();
                    category.setCategoryId(utils.generateId(10));
                    category.setCategoryName(subCategoryName);
                    category.setMainCategory(electronics);
                    categoryRepository.save(category);
                }
            }
        }

        MainCategoryEntity clothingMale = mainCategoryRepository.findByCategoryName("Clothing(Male)");
        if (clothingMale != null) {
            String[] subCategories = {"T-Shirt", "Shorts", "Shirt", "Tracksuit", "Trousers", "Jacket", "Suit"};
            for (String subCategoryName : subCategories) {
                CategoryEntity category = categoryRepository.findByCategoryName(subCategoryName);
                if (category == null) {
                    category = new CategoryEntity();
                    category.setCategoryId(utils.generateId(10));
                    category.setCategoryName(subCategoryName);
                    category.setMainCategory(clothingMale);
                    categoryRepository.save(category);
                }
            }
        }
        
        MainCategoryEntity clothingFemale = mainCategoryRepository.findByCategoryName("Clothing(Female)");
        if (clothingFemale != null) {
            String[] subCategories = {"T-Shirt", "Shorts", "Shirt", "Tracksuit", "Trousers", "Jacket", "Dress", "Skirt"};
            for (String subCategoryName : subCategories) {
                CategoryEntity category = categoryRepository.findByCategoryName(subCategoryName);
                if (category == null) {
                    category = new CategoryEntity();
                    category.setCategoryId(utils.generateId(10));
                    category.setCategoryName(subCategoryName);
                    category.setMainCategory(clothingFemale);
                    categoryRepository.save(category);
                }
            }
        }
        
        MainCategoryEntity homeAndFurniture = mainCategoryRepository.findByCategoryName("Home and Furniture");
        if (homeAndFurniture != null) {
            String[] subCategories = {"Bedroom", "Living Room", "Sofa", "Kitchen", "Home Decoration", "Home Textile"};
            for (String subCategoryName : subCategories) {
                CategoryEntity category = categoryRepository.findByCategoryName(subCategoryName);
                if (category == null) {
                    category = new CategoryEntity();
                    category.setCategoryId(utils.generateId(10));
                    category.setCategoryName(subCategoryName);
                    category.setMainCategory(homeAndFurniture);
                    categoryRepository.save(category);
                }
            }
        }
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
