package com.park.docmate.summary.controller;

import com.park.docmate.summary.Summary;
import com.park.docmate.summary.service.SummaryService;
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
@RequestMapping("/summaries")
public class SummaryController {

    private final SummaryService summaryService;
    private final UserRepository userRepository;

    public SummaryController(SummaryService summaryService, UserRepository userRepository) {
        this.summaryService = summaryService;
        this.userRepository = userRepository;
    }

    //指定された書類IDをもとに、ログイン中ユーザーの書類をAI要約する
    @PostMapping("/documents/{documentId}")
    public String createSummary(@PathVariable Long documentId, Authentication authentication, Model model,RedirectAttributes redirectAttributes){
        try{
            // 現在ログインしているユーザー名を取得する
            String username = authentication.getName();

            // ユーザー名をもとに、ログイン中ユーザー情報を取得する
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません。"));

            // 書類IDとログインユーザーをもとに、AI要約を作成する
            Summary summary = summaryService.createSummary(documentId,user);

            //画面に表示する要約結果を渡す
            model.addAttribute("summary",summary);
            model.addAttribute("document", summary.getDocument());

            // AI要約結果画面を表示する
            return "summary/summary";
        }catch (IllegalArgumentException | IllegalStateException e){
            // ユーザーに表示するエラーメッセージを一覧画面へ渡す
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            // 書類一覧画面へ戻る
            return "redirect:/documents";
        }
    }
}
