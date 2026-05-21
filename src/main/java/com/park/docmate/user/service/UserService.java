package com.park.docmate.user.service;

import com.park.docmate.exception.DuplicateUsernameException;
import com.park.docmate.user.User;
import com.park.docmate.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(User user) {
        // ユーザー名の重複をチェックする
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateUsernameException("このユーザー名は既に使用されています。");
        }

        // パスワードを暗号化して保存する
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }
}
