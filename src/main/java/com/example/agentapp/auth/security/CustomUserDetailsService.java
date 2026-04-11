package com.example.agentapp.auth.security;

import com.example.agentapp.auth.model.User;
import com.example.agentapp.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CustomUserDetailsService implements UserDetailsService {
    // Spring Security'ye kullanıcıyı database'den nasıl
    //       yükleyeceğini öğretmek
    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true) // Database okuma işlemi
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // database den kullanıcıyı bul
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username or email: " + username
                ));

        // userı user principala çevirme
        return UserPrincipal.create(user);
        // spring security şifreleri kendi kontrol eder(otomatik)

    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {

        // Database'den user'ı ID ile bul
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with id: " + id
                ));

        // UserPrincipal'e çevir ve dön
        return UserPrincipal.create(user);
    }
}