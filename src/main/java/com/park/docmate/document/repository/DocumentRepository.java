package com.park.docmate.document.repository;

import com.park.docmate.document.Document;
import com.park.docmate.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document,Long> {

    // ログイン中のユーザーがアップロードした文書だけを取得する
    // 新しい文書が上に表示されるように、IDの降順で並べる
    List<Document> findByUserOrderByIdDesc(User user);

    //書類IDとユーザー情報をもとに、本人の書類だけを取得する
    //他のユーザーの書類をダウンロードできないようにするために使用する
    Optional<Document> findByIdAndUser(Long id, User user);
}
