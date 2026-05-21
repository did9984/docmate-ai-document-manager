package com.park.docmate.document;

import com.park.docmate.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 元のファイル名
    private String originalFileName;

    // サーバーに保存されたファイル名
    private String storedFileName;

    // ファイル保存パス
    private String filePath;

    // ファイルタイプ
    private String fileType;

    // ファイルサイズ
    private Long fileSize;

    // アップロード日時
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    protected Document(){
    }

    public Document(
            String originalFileName,
            String storedFileName,
            String filePath,
            String fileType,
            Long fileSize,
            User user
    ) {
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.user = user;
        this.uploadedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public User getUser() {
        return user;
    }

    // 画面表示用のファイルサイズ文字列を返す
    public String getReadableFileSize() {

        // ファイルサイズが保存されていない場合
        if (fileSize == null) {
            return "不明";
        }

        // 1KB未満の場合は bytes 表示
        if (fileSize < 1024) {
            return fileSize + " bytes";
        }

        // 1MB未満の場合は KB 表示
        double kb = fileSize / 1024.0;
        if (kb < 1024) {
            return String.format("%.1f KB", kb);
        }

        // 1MB以上の場合は MB 表示
        double mb = kb / 1024.0;
        return String.format("%.1f MB", mb);
    }

    // 画面表示用のファイルアイコンを返す
    public String getFileIcon() {

        // ファイルタイプが保存されていない場合
        if (fileType == null) {
            return "📎";
        }

        // PDFファイルの場合
        if (fileType.equals("application/pdf")) {
            return "📄";
        }

        // Wordファイルの場合
        if (fileType.equals("application/msword") ||
                fileType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return "📝";
        }

        // 画像ファイルの場合
        if (fileType.startsWith("image/")) {
            return "🖼️";
        }

        // その他のファイルの場合
        return "📎";
    }

    // AI要約・AIレビューの対象にできるtxtファイルかどうかを判定する
    public boolean isTextFile(){
        if (fileType != null && fileType.equalsIgnoreCase("text/plain")){
            return true;
        }

        return originalFileName !=null
                && originalFileName.toLowerCase().endsWith(".txt");
    }

    // 画面表示用のファイル種別名を返す
    public String getFileTypeLabel() {

        // ファイルタイプが保存されていない場合
        if (fileType == null) {
            return "その他";
        }

        // PDFファイルの場合
        if (fileType.equals("application/pdf")) {
            return "PDF";
        }

        // Wordファイルの場合
        if (fileType.equals("application/msword") ||
                fileType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return "Word";
        }

        // 画像ファイルの場合
        if (fileType.startsWith("image/")) {
            return "画像";
        }

        // テキストファイルの場合
        if (fileType.equals("text/plain")) {
            return "テキスト";
        }

        // その他のファイルの場合
        return "その他";
    }
}
