package com.example.backend.service;

import com.example.backend.model.MangaSeries;
import com.example.backend.model.SeriesReviewHistory;
import com.example.backend.model.User;
import com.example.backend.repository.SeriesReviewHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeriesHistoryService {
    private final SeriesReviewHistoryRepository repository;

    public SeriesHistoryService(SeriesReviewHistoryRepository repository) {
        this.repository = repository;
    }

    public SeriesReviewHistory record(
            MangaSeries series,
            User actor,
            String action,
            String previousStatus,
            String newStatus,
            String reason,
            Long referenceId) {
        if (series == null || series.getSeriesId() == null) {
            return null;
        }
        SeriesReviewHistory history = new SeriesReviewHistory();
        history.setSeries(series);
        history.setActor(actor);
        history.setActorRole(actor == null || actor.getRole() == null
                ? null
                : actor.getRole().getRoleName());
        history.setAction(action);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setReason(blankToNull(reason));
        history.setReferenceId(referenceId);
        history.setCreatedAt(LocalDateTime.now());
        return repository.save(history);
    }

    public List<SeriesReviewHistory> getSeriesHistory(Long seriesId) {
        return repository.findBySeriesSeriesIdOrderByCreatedAtDesc(seriesId);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
