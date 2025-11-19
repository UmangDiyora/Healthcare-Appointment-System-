package com.healthcare.security;

import com.healthcare.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Custom UserDetails implementation for Spring Security
 * Represents authenticated user in security context
 */
@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails, OAuth2User {

    private Long id;
    private String email;
    private String password;
    private User.UserType userType;
    private Boolean isActive;
    private Boolean isEmailVerified;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    /**
     * Create UserPrincipal from User entity
     */
    public static UserPrincipal create(User user) {
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getUserType().name());

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getUserType(),
                user.getIsActive(),
                user.getIsEmailVerified(),
                Collections.singletonList(authority),
                null
        );
    }

    /**
     * Create UserPrincipal from User entity with OAuth2 attributes
     */
    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = create(user);
        return new UserPrincipal(
                userPrincipal.getId(),
                userPrincipal.getEmail(),
                userPrincipal.getPassword(),
                userPrincipal.getUserType(),
                userPrincipal.getIsActive(),
                userPrincipal.getIsEmailVerified(),
                userPrincipal.getAuthorities(),
                attributes
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive && isEmailVerified;
    }

    // OAuth2User methods
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }

    /**
     * Get User entity reference
     */
    public User getUser() {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setUserType(userType);
        user.setIsActive(isActive);
        user.setIsEmailVerified(isEmailVerified);
        return user;
    }
}
