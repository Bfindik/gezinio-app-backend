package com.example.gezinio.auth.security;

import com.example.gezinio.auth.model.Role;
import com.example.gezinio.auth.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UserPrincipal implements UserDetails {
    // Amaç User entity'sini Spring Security'nin anlayacağı formata çevirmek
    private Long id;

    private String username;

    private String email;

    private String password;


    private Collection<? extends GrantedAuthority> authorities;

    private boolean enabled;
    private boolean accountNonLocked;

    // ====================================================================
    // CONSTRUCTOR
    // ====================================================================


    private UserPrincipal(Long id, String username, String email, String password,
                          Collection<? extends GrantedAuthority> authorities,
                          boolean enabled, boolean accountNonLocked) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
    }

    public static UserPrincipal create(User user) { // factory pattern
        // 1. AUTHORİTİES OLUŞTUR
        Collection<GrantedAuthority> authorities = getAuthorities(user);

        // 2. UserPrincipal OLUŞTUR
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.isEnabled(),
                user.isAccountNonLocked()
        );
    }

    private static Collection<GrantedAuthority> getAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // User'ın tüm rollerini dolaş
        for (Role role : user.getRoles()) {
            // 1. ROL AUTHORITY'Sİ EKLE (ROLE_ prefix ile)
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

            // 2. ROL'ÜN TÜM PERMİSSİON'LARINI EKLE
            role.getPermissions().forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(permission.getName().name()))
            );
        }

        return authorities;
    }

    // ====================================================================
    // UserDetails INTERFACE METHODS (Spring Security gereksinimi)
    // ====================================================================


    @Override
    public String getUsername() {
        return username;
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
        return true; // Hesap expire olmaz
    }


    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }


    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Şifre expire olmaz
    }


    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}