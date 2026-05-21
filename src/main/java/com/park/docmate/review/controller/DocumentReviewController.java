package com.park.docmate.review.controller;

import com.park.docmate.review.DocumentReview;
import com.park.docmate.review.service.DocumentReviewService;
import com.park.docmate.user.User;
import com.park.docmate.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reviews")
public class DocumentReviewController {

    private final DocumentReviewService documentReviewService;
    private final UserRepository userRepository;

    public DocumentReviewController(DocumentReviewService documentReviewService, UserRepository userRepository) {
        this.documentReviewService = documentReviewService;
        this.userRepository = userRepository;
    }

    // 指定された書類IDをもとに、ログイン中ユーザー本人の書類だけAIレビューする
    @PostMapping("/documents/{documentId}")
    public String createReview(@PathVariable Long documentId, Authentication authentication, Model model,RedirectAttributes redirectAttributes){

        try{
            // 現在ログインしているユーザー名を取得する
            String username = authentication.getName();

            // ユーザー名をもとに、ログイン中ユーザー情報を取得する
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません。"));

            // 書類IDとログインユーザーをもとに、AIレビューを作成する
            DocumentReview review = documentReviewService.createReview(documentId, user);

            // 画面表示用にレビュー結果を渡す
            model.addAttribute("review", review);

            // 画面表示用に書類情報を渡す
            model.addAttribute("document", review.getDocument());

            // AIレビュー結果画面を表示する
            return "review";
        }catch (IllegalArgumentException | IllegalStateException e){
            // ユーザーに表示するエラーメッセージを一覧画面へ渡す
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            // 書類一覧画面へ戻る
            return "redirect:/documents";
        }
    }
}
