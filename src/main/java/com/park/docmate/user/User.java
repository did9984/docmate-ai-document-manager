package com.park.docmate.user;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ログインIDとして使用するユーザー名
    @Column(nullable = false, unique = true)
    private String username;

    // BCryptで暗号化されたパスワード
    @Column(nullable = false)
    private String password;

    public User(){
    }

    public User(String username, String password){
        this.username = username;
        this.password = password;
    }

    public Long getId(){
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
