package com.park.docmate.document.controller;

import com.park.docmate.document.Document;
import com.park.docmate.document.service.DocumentService;
import com.park.docmate.user.User;
import com.park.docmate.user.repository.UserRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class DocumentController {

    private final DocumentService documentService;
    private final UserRepository userRepository;

    public DocumentController(DocumentService documentService, UserRepository userRepository) {
        this.documentService = documentService;
        this.userRepository = userRepository;
    }

    // 文書一覧画面を表示
    @GetMapping("/documents")
    public String listDocuments(Authentication authentication, Model model) {
        // ログイン中のユーザー名を取得する
        String username = authentication.getName();

        //ユーザー名を元にDBからユーザー情報を取得する
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません。"));

        //ログイン中のユーザーがアップロードした文書市らを取得する
        List<Document> documents = documentService.findDocumentsByUser(user);

        //画面に文書一覧を渡す
        model.addAttribute("documents", documents);

        return "document/list";
    }

    @GetMapping("/documents/upload")
    public String uploadForm(){
        return "document/upload";
    }

    @PostMapping("/documents/upload")
    public String uploadDocument(
            @RequestParam("file")MultipartFile file, Authentication authentication, RedirectAttributes redirectAttributes){
        // ログイン中のユーザ目を取得
        String username = authentication.getName();

        //DBからログイン中のユーザー情報取得
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("ユーザーが見つかりません。"));

        //ファイルが選択されたいない場合は、アップロード画面へ戻す
        if (file.isEmpty()){
            redirectAttributes.addFlashAttribute("errorMessage","ファイルを選択してください。");
            return "redirect:/documents/upload";
        }

        //ファイルを保存し、DBにも文書情報を保存
        documentService.saveFile(file,user);

        // 一度だけ表示される成功メッセージを設定する
        redirectAttributes.addFlashAttribute("successMessage", "書類をアップロードしました。");

        //アップロード後、文書一覧ページへ移動
        return "redirect:/documents";
    }

    @GetMapping("/documents/{id}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id,
            Authentication authentication
    ) {
        // 現在ログインしているユーザー名を取得する
        String username = authentication.getName();

        // ユーザー名をもとに、ログイン中のユーザー情報を取得する
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません。"));

        // 書類IDとユーザー情報をもとに、本人の書類だけを取得する
        Document document = documentService.findDownloadDocument(id, user);

        // DBに保存されているファイルパスをもとに、サーバー上の実ファイルを取得する
        Resource resource = new FileSystemResource(document.getFilePath());

        // ブラウザで文字化けしないように、元のファイル名をUTF-8でエンコードする
        String encodedFileName = URLEncoder.encode(
                document.getOriginalFileName(),
                StandardCharsets.UTF_8
        ).replaceAll("\\+", "%20");

        // ファイルをダウンロード形式で返す
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.parseMediaType(document.getFileType()))
                .body(resource);
    }

    @PostMapping("/documents/{id}/delete")
    public String deleteDocument(
            @PathVariable Long id, Authentication authentication,RedirectAttributes redirectAttributes){
        //現在ログインしているユーザー名を取得する
        String username = authentication.getName();

        //ユーザー名をもとに、ログイン中のユーザー情報を取得する
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません。"));

        //書類IDとログイン中のユーザー情報ををもとに、本人の書類だけを削除する
        documentService.deleteDocument(id,user);

        // 一度だけ表示される成功メッセージを設定する
        redirectAttributes.addFlashAttribute("successMessage", "書類を削除しました。");

        //削除後、書類一覧ページへ戻る
        return "redirect:/documents";
    }
}
