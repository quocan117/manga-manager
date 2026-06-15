package com.example.backend.repository;

import com.example.backend.model.ReaderFeedbackImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReaderFeedbackImportRepository extends JpaRepository<ReaderFeedbackImport, Long> {
}
