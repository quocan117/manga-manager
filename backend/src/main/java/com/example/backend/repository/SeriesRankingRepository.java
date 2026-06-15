package com.example.backend.repository;

import com.example.backend.model.SeriesRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeriesRankingRepository extends JpaRepository<SeriesRanking, Long> {
}
