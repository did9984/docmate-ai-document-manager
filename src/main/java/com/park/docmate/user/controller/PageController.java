package com.park.docmate.user.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/mypage")
    public String mypage(Authentication authentication, Model model) {
        // ログイン中のユーザー名を画面へ渡す
        model.addAttribute("username", authentication.getName());
        return "mypage";
    }
}
