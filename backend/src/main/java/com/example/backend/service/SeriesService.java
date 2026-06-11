package com.example.backend.service;

import com.example.backend.dto.CreateSeriesRequest;
import com.example.backend.dto.SeriesResponse;
import com.example.backend.exception.DuplicateSeriesNameException;
import com.example.backend.exception.SeriesNotFoundException;
import com.example.backend.model.Draft;
import com.example.backend.model.Series;
import com.example.backend.model.SeriesCharacter;
import com.example.backend.model.SeriesStatus;
import com.example.backend.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public SeriesResponse createSeries(CreateSeriesRequest request, List<MultipartFile> drafts) {
        if (seriesRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new DuplicateSeriesNameException(request.getName().trim());
        }

        Series series = Series.builder()
                .name(request.getName().trim())
                .genre(request.getGenre().trim())
                .summary(request.getSummary().trim())
                .status(SeriesStatus.DRAFT)
                .build();

        if (request.getCharacters() != null) {
            for (var characterRequest : request.getCharacters()) {
                SeriesCharacter character = SeriesCharacter.builder()
                        .name(characterRequest.getName().trim())
                        .description(characterRequest.getDescription() != null
                                ? characterRequest.getDescription().trim()
                                : null)
                        .build();
                series.addCharacter(character);
            }
        }

        Series saved = seriesRepository.save(series);

        if (drafts != null) {
            for (MultipartFile file : drafts) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                FileStorageService.StoredFile stored = fileStorageService.storeDraft(saved.getId(), file);
                Draft draft = Draft.builder()
                        .fileName(stored.fileName())
                        .storedPath(stored.storedPath())
                        .contentType(stored.contentType())
                        .fileSize(stored.fileSize())
                        .build();
                saved.addDraft(draft);
            }
            saved = seriesRepository.save(saved);
        }

        return SeriesResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<SeriesResponse> getAllSeries() {
        return seriesRepository.findAll().stream()
                .map(SeriesResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SeriesResponse getSeriesById(Long id) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new SeriesNotFoundException(id));
        return SeriesResponse.from(series);
    }
}
