package com.example.backend.repository;

import com.example.backend.model.PublishSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublishScheduleRepository extends JpaRepository<PublishSchedule, Long> {
    List<PublishSchedule> findBySeriesSeriesIdOrderByPublishDateAsc(Long seriesId);

    List<PublishSchedule> findAllByOrderByPublishDateAsc();
}
