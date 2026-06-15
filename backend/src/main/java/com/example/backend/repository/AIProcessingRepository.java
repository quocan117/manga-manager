package com.example.backend.repository;

import com.example.backend.model.AIProcessing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AIProcessingRepository extends JpaRepository<AIProcessing, Long> {
}
