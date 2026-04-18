package com.graduation.smarty_commerce.shared.dto;

import com.graduation.smarty_commerce.io.Entity.CartEntity;
import com.graduation.smarty_commerce.io.Entity.OrderEntity;
import com.graduation.smarty_commerce.io.Entity.RoleEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class UserDto implements Serializable {

    private static final long serialVersionUID = 5313493418756412318L;


    private long id;

    private String userId;

    private String firstName;

    private String lastName;

    private String email;

    private String preferences;

    private String password;

    private String emailVerificationToken;

    private Boolean emailVerificationStatus = false;

    private Collection<String> roles;

    private CartDto cart;

    private List<OrderDto> orders;

    private List<AddressDto> addresses;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }

    public void setEmailVerificationToken(String emailVerificationToken) {
        this.emailVerificationToken = emailVerificationToken;
    }

    public Boolean getEmailVerificationStatus() {
        return emailVerificationStatus;
    }

    public void setEmailVerificationStatus(Boolean emailVerificationStatus) {
        this.emailVerificationStatus = emailVerificationStatus;
    }

    public Collection<String> getRoles() {
        return roles;
    }

    public void setRoles(Collection<String> roles) {
        this.roles = roles;
    }

    public CartDto getCart() {
        return cart;
    }

    public void setCart(CartDto cart) {
        this.cart = cart;
    }

    public List<OrderDto> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderDto> orders) {
        this.orders = orders;
    }

    public List<AddressDto> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressDto> addresses) {
        this.addresses = addresses;
    }
}

