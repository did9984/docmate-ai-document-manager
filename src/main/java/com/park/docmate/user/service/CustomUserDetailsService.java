package com.park.docmate.user.service;

import com.park.docmate.user.User;

import com.park.docmate.user.repository.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 入力されたusernameでユーザーを検索する
        User user = userRepository.findByUsername(username).orElseThrow(()-> new UsernameNotFoundException("使用者を見つけません。"));

        
        // Spring Security が利用できる UserDetails オブジェクトに変換
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}

