package com.park.docmate.user.controller;

import com.park.docmate.exception.DuplicateUsernameException;
import com.park.docmate.user.User;
import com.park.docmate.user.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute User user, Model model) {
        try {
            userService.register(user);
            return "redirect:/login?signupSuccess";
        } catch (DuplicateUsernameException e) {
            // 画面に重複エラーメッセージを表示する
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("user", user);
            return "signup";
        } catch (Exception e) {
            // 想定外エラー時の共通メッセージ
            model.addAttribute("errorMessage", "会員登録中にエラーが発生しました。");
            model.addAttribute("user", user);
            return "signup";
        }
    }
}