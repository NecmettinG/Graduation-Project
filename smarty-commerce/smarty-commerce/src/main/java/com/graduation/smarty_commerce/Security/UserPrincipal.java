package com.graduation.smarty_commerce.Security;

import com.graduation.smarty_commerce.io.Entity.AuthorityEntity;
import com.graduation.smarty_commerce.io.Entity.RoleEntity;
import com.graduation.smarty_commerce.io.Entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;

public class UserPrincipal implements UserDetails {

    private static final long serialVersionUID = -7530187709860249942L;

    private UserEntity userEntity;

    private String userId;

    public UserPrincipal(UserEntity userEntity) {
        this.userEntity = userEntity;
        this.userId = userEntity.getUserId();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        //HashSet is used to prevent duplicated values, ensuring every role and authority in the collection is unique.
        Collection<GrantedAuthority> authorities = new HashSet<>();
        Collection<AuthorityEntity> authorityEntities = new HashSet<>();

        //Get User Roles:

        Collection<RoleEntity> roles = userEntity.getRoles();

        if(roles == null) return authorities;

        roles.forEach((role)-> {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
            authorityEntities.addAll(role.getAuthorities());
        });

        authorityEntities.forEach((authorityEntity)-> {
            authorities.add(new SimpleGrantedAuthority(authorityEntity.getName()));

        });

        return authorities;
    }

    @Override
    public String getPassword() {
        return this.userEntity.getEncryptedPassword();
    }

    @Override
    public String getUsername() {
        return this.userEntity.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.userEntity.getEmailVerificationStatus();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {

        this.userId = userId;
    }

}
