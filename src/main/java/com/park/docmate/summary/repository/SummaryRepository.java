package com.park.docmate.summary.repository;

import com.park.docmate.document.Document;
import com.park.docmate.summary.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryRepository extends JpaRepository<Summary, Long> {

    // 指定された書類に紐づく要約データを削除する
    void deleteByDocument(Document document);
}
