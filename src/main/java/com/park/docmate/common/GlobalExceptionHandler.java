package com.park.docmate.common;

import com.park.docmate.exception.DuplicateUsernameException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

// アプリ全体で発生した例外をまとめて処理するクラス
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    //IllegalArgumentException が発生した場合の共通処理
    @ExceptionHandler(DuplicateUsernameException.class)
    public String handleIllegalArgumentException(DuplicateUsernameException e, HttpServletRequest request, Model model){
        // 会員登録時のユーザー名重複エラーをログに出力する
        log.warn("Business error occurred: uri={}, message={}",request.getRequestURI(),e.getMessage());

        model.addAttribute("errorMessage",e.getMessage());

        //templates/error.html を表示する
        return "error";
        }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e,HttpServletRequest request,Model model){
        //アップロードサイズ超過エラーをログに出力する
        log.warn("Upload size exceeded: uri={}, message={}",request.getRequestURI(),e.getMessage());

        model.addAttribute("errorMessage", "ファイルサイズは10MB以下にしてください。");

        return "error";
    }

    //その他のよきしないエラー発生した場合の共通処理
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleException(IllegalArgumentException e,Model model,HttpServletRequest request){
        //想定外のエラーをログに出力する
        log.error("Unexpected error occurred: uri={}",request.getRequestURI(),e);

        model.addAttribute("errorMessage", e.getMessage());

        //templates/error.html を表示する
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e,HttpServletRequest request,Model model){
        // 想定外のエラーをログに出力する
        log.error("Unexpected error occurred: uri={}",request.getRequestURI(),e);

        model.addAttribute("errorMessage", "予期しないエラーが発生しました。");

        return "error";
    }
}
