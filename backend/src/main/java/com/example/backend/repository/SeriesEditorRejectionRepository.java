package com.example.backend.repository;

import com.example.backend.model.SeriesEditorRejection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeriesEditorRejectionRepository extends JpaRepository<SeriesEditorRejection, Long> {
    List<SeriesEditorRejection> findBySeriesSeriesId(Long seriesId);

    Optional<SeriesEditorRejection> findBySeriesSeriesIdAndEditorUserId(Long seriesId, Long editorId);
}
