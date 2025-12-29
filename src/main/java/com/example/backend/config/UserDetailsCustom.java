package com.example.backend.config;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.example.backend.service.UserService;

@Component("userDetailsService") // dùng để tạo ra UserDetailsService tự động chuyển u->U
public class UserDetailsCustom implements UserDetailsService {

    private final UserService userService;

    public UserDetailsCustom(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // dùng cách này để tránh nhầm với của Security
        com.example.backend.domain.entity.User user = this.userService.handleGetUserByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("Username/password không hợp lệ");
        }

        // LẤY PERMISSION
        Collection<SimpleGrantedAuthority> authorities;

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
        if (isAdmin) {
            // Nếu là ADMIN thì full quyền
            authorities = List.of(new SimpleGrantedAuthority("ALL"));
        } else {
            // Các role khác như VIEW/SATFF... phải dựa vào permission
            authorities = user.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                    .collect(Collectors.toSet());
        }

        return new User(user.getEmail(), user.getPassword(), authorities);
    }

}