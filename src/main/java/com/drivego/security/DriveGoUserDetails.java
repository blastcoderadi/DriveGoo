package com.drivego.security;

import com.drivego.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

/**
 * Wraps DriveGo User as a Spring Security UserDetails.
 * Used for OTP-based session authentication.
 */
public class DriveGoUserDetails implements UserDetails {

    private final User user;

    public DriveGoUserDetails(User user) {
        this.user = user;
    }

    public User getUser() { return user; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    @Override
    public String getPassword() { return ""; } // No password – OTP auth

    @Override
    public String getUsername() {
        return user.getEmail() != null ? user.getEmail() :
               (user.getPhone() != null ? user.getPhone() : user.getId().toString());
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return user.isActive(); }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return user.isActive(); }
}
