package com.example.backend.repository;

import com.example.backend.model.ChapterPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterPageRepository extends JpaRepository<ChapterPage, Long> {
}
