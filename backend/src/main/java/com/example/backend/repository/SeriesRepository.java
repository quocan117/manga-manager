package com.example.backend.repository;

import com.example.backend.model.Series;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeriesRepository extends JpaRepository<Series, Long> {

    boolean existsByNameIgnoreCase(String name);
}
