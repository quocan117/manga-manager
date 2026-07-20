package com.example.backend.service;

import com.example.backend.dto.EditorialBoardDtos.CreateUserRequest;
import com.example.backend.dto.EditorialBoardDtos.AssignEditorRequest;
import com.example.backend.dto.EditorialBoardDtos.BoardDecisionRequest;
import com.example.backend.dto.EditorialBoardDtos.BoardDecisionResponse;
import com.example.backend.dto.EditorialBoardDtos.BoardMemberAssignmentResponse;
import com.example.backend.dto.EditorialBoardDtos.ImportReaderFeedbackRequest;
import com.example.backend.dto.EditorialBoardDtos.ReaderFeedbackImportResponse;
import com.example.backend.dto.EditorialBoardDtos.ReaderVoteResponse;
import com.example.backend.dto.EditorialBoardDtos.ReviewSeriesResponse;
import com.example.backend.dto.EditorialBoardDtos.SeriesVoteSummaryResponse;
import com.example.backend.dto.EditorialBoardDtos.UpdateUserRequest;
import com.example.backend.dto.EditorialBoardDtos.UserResponse;
import com.example.backend.dto.MangakaDtos.NotificationResponse;
import com.example.backend.dto.MangakaDtos.RankingResponse;
import com.example.backend.dto.MangakaDtos.UploadedFileResponse;
import com.example.backend.dto.ReviewRegistrationRequest;
import com.example.backend.dto.TantouEditorDtos.ScheduleRequest;
import com.example.backend.dto.TantouEditorDtos.ScheduleResponse;
import com.example.backend.dto.EditorialBoardDtos.SeriesTotalVotesResponse;
import com.example.backend.model.BoardDecision;
import com.example.backend.model.Chapter;
import com.example.backend.model.ChapterLikeLog;
import com.example.backend.model.GuestAccessLog;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.SeriesBoardAssignment;
import com.example.backend.model.SeriesEditorRejection;
import com.example.backend.model.SeriesFile;
import com.example.backend.model.RegistrationRequest;
import com.example.backend.model.ReaderFeedbackImport;
import com.example.backend.model.Role;
import com.example.backend.model.SeriesRanking;
import com.example.backend.model.User;
import com.example.backend.model.Notification;
import com.example.backend.repository.BoardDecisionRepository;
import com.example.backend.repository.ChapterLikeLogRepository;
import com.example.backend.repository.MangaSeriesRepository;
import com.example.backend.repository.RegistrationRequestRepository;
import com.example.backend.repository.ReaderFeedbackImportRepository;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.SeriesRankingRepository;
import com.example.backend.repository.SeriesBoardAssignmentRepository;
import com.example.backend.repository.SeriesEditorRejectionRepository;
import com.example.backend.repository.SeriesFileRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.PublishScheduleRepository;
import com.example.backend.model.PublishSchedule;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class EditorialBoardService {
    private static final String BOARD_ROLE = "EDITORIAL_BOARD";
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String REVIEWING_SERIES_STATUS = "REVIEWING";
    private static final String PUBLISHED_SERIES_STATUS = "Published";
    private static final String REVISION_REQUESTED_STATUS = "REVISION_REQUESTED";
    private static final String PENDING_EDITOR_STATUS = "PENDING_EDITOR";
    private static final String EDITOR_ASSIGNMENT_REQUIRED_STATUS = "EDITOR_ASSIGNMENT_REQUIRED";
    private static final String CANCELLED_SERIES_STATUS = "CANCELLED";
    private static final String APPROVE_DECISION = "APPROVE";
    private static final String REJECT_DECISION = "REJECT";
    private static final String CANCEL_DECISION = "CANCEL";
    private static final int BOARD_PANEL_SIZE = 3;

    private static final Set<String> MANAGED_ROLES = Set.of(
            "MANGAKA", "ASSISTANT", "TANTOU_EDITOR", BOARD_ROLE);
    private static final Set<String> USER_STATUSES = Set.of(
            "ACTIVE", "INACTIVE", "SUSPENDED", "DELETED");

    private final RegistrationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MangaSeriesRepository mangaSeriesRepository;
    private final BoardDecisionRepository boardDecisionRepository;
    private final NotificationRepository notificationRepository;
    private final PublishScheduleRepository publishScheduleRepository;
    private final ChapterLikeLogRepository chapterLikeLogRepository;
    private final SeriesRankingRepository seriesRankingRepository;
    private final ReaderFeedbackImportRepository readerFeedbackImportRepository;
    private final SeriesFileRepository seriesFileRepository;
    private final SeriesEditorRejectionRepository seriesEditorRejectionRepository;
    private final SeriesBoardAssignmentRepository seriesBoardAssignmentRepository;

    public EditorialBoardService(
            RegistrationRequestRepository requestRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            MangaSeriesRepository mangaSeriesRepository,
            BoardDecisionRepository boardDecisionRepository,
            NotificationRepository notificationRepository,
            PublishScheduleRepository publishScheduleRepository,
            ChapterLikeLogRepository chapterLikeLogRepository,
            SeriesRankingRepository seriesRankingRepository,
            ReaderFeedbackImportRepository readerFeedbackImportRepository,
            SeriesFileRepository seriesFileRepository,
            SeriesEditorRejectionRepository seriesEditorRejectionRepository,
            SeriesBoardAssignmentRepository seriesBoardAssignmentRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.mangaSeriesRepository = mangaSeriesRepository;
        this.boardDecisionRepository = boardDecisionRepository;
        this.notificationRepository = notificationRepository;
        this.publishScheduleRepository = publishScheduleRepository;
        this.chapterLikeLogRepository = chapterLikeLogRepository;
        this.seriesRankingRepository = seriesRankingRepository;
        this.readerFeedbackImportRepository = readerFeedbackImportRepository;
        this.seriesFileRepository = seriesFileRepository;
        this.seriesEditorRejectionRepository = seriesEditorRejectionRepository;
        this.seriesBoardAssignmentRepository = seriesBoardAssignmentRepository;
    }

    public List<RegistrationRequest> getAllRequests() {
        return requestRepository.findAll();
    }

    public List<UserResponse> getUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Transactional
    public List<ReviewSeriesResponse> getReviewingSeries() {
        User currentUser = currentEditorialBoard();
        return mangaSeriesRepository.findByStatusIgnoreCaseOrderBySubmittedAtDesc(REVIEWING_SERIES_STATUS)
                .stream()
                .map(series -> toReviewSeriesResponse(series, currentUser))
                .toList();
    }

    public List<ReviewSeriesResponse> getEditorAssignmentRequiredSeries() {
        User currentUser = currentEditorialBoard();
        return mangaSeriesRepository.findByStatusIgnoreCaseOrderBySubmittedAtDesc(EDITOR_ASSIGNMENT_REQUIRED_STATUS)
                .stream()
                .map(series -> toReviewSeriesResponse(series, currentUser))
                .toList();
    }

    @Transactional
    public ReviewSeriesResponse getSeriesReview(Long seriesId) {
        User currentUser = currentEditorialBoard();
        MangaSeries series = mangaSeriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Series not found"));
        return toReviewSeriesResponse(series, currentUser);
    }

    public List<BoardDecisionResponse> getSeriesDecisions(Long seriesId) {
        if (!mangaSeriesRepository.existsById(seriesId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Series not found");
        }
        return boardDecisionRepository.findPanelDecisionsBySeriesIdOrderByDecisionDateDesc(seriesId)
                .stream()
                .map(this::toBoardDecisionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getPublishSchedules() {
        return publishScheduleRepository.findAllByOrderByPublishDateAsc()
                .stream()
                .map(this::toScheduleResponse)
                .toList();
    }

    @Transactional
    public ScheduleResponse createSchedule(ScheduleRequest request) {
        PublishSchedule schedule = new PublishSchedule();
        applyScheduleRequest(schedule, request);
        PublishSchedule savedSchedule = publishScheduleRepository.save(schedule);
        notifyBoardScheduleChange(savedSchedule, "PUBLISH_SCHEDULE_CREATED");
        return toScheduleResponse(savedSchedule);
    }

    @Transactional
    public ScheduleResponse updateSchedule(Long scheduleId, ScheduleRequest request) {
        PublishSchedule schedule = publishScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));
        applyScheduleRequest(schedule, request);
        PublishSchedule savedSchedule = publishScheduleRepository.save(schedule);
        notifyBoardScheduleChange(savedSchedule, "PUBLISH_SCHEDULE_UPDATED");
        return toScheduleResponse(savedSchedule);
    }

    @Transactional
    public void deleteSchedule(Long scheduleId) {
        PublishSchedule schedule = publishScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));
        publishScheduleRepository.delete(schedule);
    }

    @Transactional(readOnly = true)
    public List<ReaderVoteResponse> getReaderVotes(LocalDateTime from, LocalDateTime to) {
        validatePeriodRange(from, to);
        return chapterLikeLogRepository.findByLikedAtBetweenOrderByLikedAtDesc(from, to)
                .stream()
                .map(this::toReaderVoteResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeriesVoteSummaryResponse> getReaderVoteSummary(LocalDateTime from, LocalDateTime to) {
        validatePeriodRange(from, to);
        return sortedVoteCounts(from, to)
                .stream()
                .map(voteCount -> new SeriesVoteSummaryResponse(
                        voteCount.getSeriesId(),
                        voteCount.getSeriesTitle(),
                        voteCount.getVoteCount() == null ? 0L : voteCount.getVoteCount()))
                .toList();
    }

    @Transactional
    public List<ReaderFeedbackImportResponse> importReaderFeedback(ImportReaderFeedbackRequest request) {
        validatePeriodRange(request.periodStart(), request.periodEnd());
        String period = request.period().trim();
        User boardUser = currentEditorialBoard();
        LocalDateTime now = LocalDateTime.now();
        List<ChapterLikeLogRepository.SeriesVoteCount> voteCounts = sortedVoteCounts(
                request.periodStart(), request.periodEnd());

        return voteCounts.stream()
                .map(voteCount -> saveReaderFeedbackImport(voteCount, period, boardUser, now,
                        voteCounts.indexOf(voteCount) + 1))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReaderFeedbackImportResponse> getReaderFeedbackImports() {
        return readerFeedbackImportRepository.findAllByOrderByImportedAtDesc()
                .stream()
                .map(this::toReaderFeedbackImportResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RankingResponse> getRankings(String period) {
        List<SeriesRanking> source = (period == null || period.isBlank())
                ? seriesRankingRepository.findAllByOrderByCalculatedAtDesc()
                : seriesRankingRepository.findByPeriodOrderByRankingPositionAsc(period.trim());
        return source.stream()
                .map(this::toRankingResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getRankingPeriods() {
        return seriesRankingRepository.findDistinctPeriods();
    }

    @Transactional(readOnly = true)
    public List<SeriesTotalVotesResponse> getSeriesTotalVotes() {
        return seriesRankingRepository.sumVotesAllPeriodsGroupBySeries()
                .stream()
                .map(v -> new SeriesTotalVotesResponse(
                        v.getSeriesId(),
                        v.getSeriesTitle(),
                        v.getTotalVotes() == null ? 0L : v.getTotalVotes()))
                .sorted(Comparator.comparingLong(SeriesTotalVotesResponse::totalVotes).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications() {
        return notificationRepository.findByUserEmailOrderByCreatedAtDesc(currentEmail())
                .stream()
                .map(this::toNotificationResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markNotificationRead(Long notificationId) {
        Notification notification = notificationRepository
                .findByNotificationIdAndUserEmail(notificationId, currentEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setIsRead(true);
        return toNotificationResponse(notificationRepository.save(notification));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        if (userRepository.existsByUsername(request.username().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        User user = new User();
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setAvatarUrl(blankToNull(request.avatarUrl()));
        user.setStatus(normalizeStatus(request.status(), "ACTIVE"));
        user.setRole(resolveManagedRole(request.role()));
        user.setCreatedBy(currentEditorialBoard());
        user.setCreatedAt(LocalDateTime.now());

        return toUserResponse(userRepository.save(user));
    }

    @Transactional
    public ReviewSeriesResponse assignEditor(Long seriesId, AssignEditorRequest request) {
        User boardMember = currentEditorialBoard();
        MangaSeries series = mangaSeriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Series not found"));
        User editor = userRepository.findById(request.editorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Editor not found"));
        if (editor.getRole() == null || !"TANTOU_EDITOR".equals(editor.getRole().getRoleName())
                || editor.getStatus() == null || !ACTIVE_STATUS.equalsIgnoreCase(editor.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected user must be an active tantou editor");
        }

        series.setTantouEditor(editor);
        series.setStatus(PENDING_EDITOR_STATUS);
        series.setEditorAssignmentLocked(true);
        series.setEditorAssignedAt(LocalDateTime.now());
        mangaSeriesRepository.save(series);

        notify(editor, "FORCED_EDITOR_ASSIGNMENT", seriesId,
                "Editorial Board assigned you to series \"" + series.getTitle()
                        + "\". This assignment cannot be rejected.");
        notify(series.getAuthor(), "EDITOR_ASSIGNED_BY_BOARD", seriesId,
                "Editorial Board assigned editor " + editor.getUsername()
                        + " to series \"" + series.getTitle() + "\".");
        return toReviewSeriesResponse(series, boardMember);
    }

    @Transactional
    public ReviewSeriesResponse voteSeries(Long seriesId, BoardDecisionRequest request) {
        User boardMember = currentEditorialBoard();
        MangaSeries series = mangaSeriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Series not found"));
        if (!REVIEWING_SERIES_STATUS.equalsIgnoreCase(series.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only reviewing series can be voted on");
        }
        assignBoardPanel(series);
        if (seriesBoardAssignmentRepository
                .findBySeriesSeriesIdAndBoardMemberUserId(seriesId, boardMember.getUserId())
                .isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Ban khong thuoc ban tham dinh duoc phan cong cho tac pham nay");
        }

        String decisionType = normalizeBoardDecision(request.decisionType(), false);
        BoardDecision decision = boardDecisionRepository
                .findBySeriesSeriesIdAndBoardMemberUserId(seriesId, boardMember.getUserId())
                .orElseGet(BoardDecision::new);
        decision.setSeries(series);
        decision.setBoardMember(boardMember);
        decision.setDecisionType(decisionType);
        decision.setReason(blankToNull(request.reason()));
        decision.setDecisionDate(LocalDateTime.now());
        boardDecisionRepository.save(decision);

        applyVotingResult(series);
        return toReviewSeriesResponse(series, boardMember);
    }

    @Transactional
    public ReviewSeriesResponse cancelSeries(Long seriesId, BoardDecisionRequest request) {
        User boardMember = currentEditorialBoard();
        MangaSeries series = mangaSeriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Series not found"));
        if (CANCELLED_SERIES_STATUS.equalsIgnoreCase(series.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Series is already cancelled");
        }

        BoardDecision decision = boardDecisionRepository
                .findBySeriesSeriesIdAndBoardMemberUserId(seriesId, boardMember.getUserId())
                .orElseGet(BoardDecision::new);
        decision.setSeries(series);
        decision.setBoardMember(boardMember);
        decision.setDecisionType(CANCEL_DECISION);
        decision.setReason(blankToNull(request == null ? null : request.reason()));
        decision.setDecisionDate(LocalDateTime.now());
        boardDecisionRepository.save(decision);

        series.setStatus(CANCELLED_SERIES_STATUS);
        mangaSeriesRepository.save(series);
        return toReviewSeriesResponse(series, boardMember);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.email() != null && !request.email().isBlank()) {
            String newEmail = request.email().trim();
            userRepository.findByEmail(newEmail)
                    .filter(existing -> !existing.getUserId().equals(userId))
                    .ifPresent(existing -> {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
                    });
            user.setEmail(newEmail);
        }
        if (request.username() != null && !request.username().isBlank()) {
            String newUsername = request.username().trim();
            userRepository.findByUsername(newUsername)
                    .filter(existing -> !existing.getUserId().equals(userId))
                    .ifPresent(existing -> {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
                    });
            user.setUsername(newUsername);
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.role() != null && !request.role().isBlank()) {
            user.setRole(resolveManagedRole(request.role()));
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(blankToNull(request.avatarUrl()));
        }
        if (request.status() != null && !request.status().isBlank()) {
            user.setStatus(normalizeStatus(request.status(), user.getStatus()));
        }

        return toUserResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        User currentUser = currentEditorialBoard();
        if (currentUser.getUserId() != null && currentUser.getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        }
        user.setStatus("DELETED");
        return toUserResponse(userRepository.save(user));
    }

    @Transactional
    public RegistrationRequest approve(Long requestId, ReviewRegistrationRequest dto) {
        RegistrationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Registration request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request already reviewed");
        }

        User editorialBoard = currentEditorialBoard();

        String requestedRole = request.getRequestedRole() == null
                ? "MANGAKA"
                : request.getRequestedRole();
        Role role = resolveManagedRole(requestedRole);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("A user with this email already exists");
        }

        User user = new User();
        user.setUsername(request.getEmail());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getTempPassword()));
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy(editorialBoard);
        user.setRole(role);

        userRepository.save(user);

        request.setStatus("APPROVED");
        request.setReviewNote(dto.getReviewNote());
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedBy(editorialBoard);

        return requestRepository.save(request);
    }

    public RegistrationRequest reject(Long requestId, ReviewRegistrationRequest dto) {
        RegistrationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Registration request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request already reviewed");
        }

        User editorialBoard = currentEditorialBoard();

        request.setStatus("REJECTED");
        request.setReviewNote(dto.getReviewNote());
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedBy(editorialBoard);

        return requestRepository.save(request);
    }

    private ReaderFeedbackImportResponse saveReaderFeedbackImport(
            ChapterLikeLogRepository.SeriesVoteCount voteCount,
            String period,
            User boardUser,
            LocalDateTime calculatedAt,
            int rankingPosition) {
        MangaSeries series = mangaSeriesRepository.findById(voteCount.getSeriesId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Series not found"));
        int votes = Math.toIntExact(voteCount.getVoteCount() == null ? 0L : voteCount.getVoteCount());

        ReaderFeedbackImport feedbackImport = readerFeedbackImportRepository
                .findBySeriesSeriesIdAndPeriod(series.getSeriesId(), period)
                .orElseGet(ReaderFeedbackImport::new);
        feedbackImport.setSeries(series);
        feedbackImport.setImportedBy(boardUser);
        feedbackImport.setPeriod(period);
        feedbackImport.setVoteCount(votes);
        feedbackImport.setAvgScore((float) votes);
        feedbackImport.setSourceNote("Tổng hợp tự động từ lượt thích độc giả");
        feedbackImport.setImportedAt(calculatedAt);
        ReaderFeedbackImport savedImport = readerFeedbackImportRepository.save(feedbackImport);

        SeriesRanking ranking = seriesRankingRepository
                .findBySeriesSeriesIdAndPeriod(series.getSeriesId(), period)
                .orElseGet(SeriesRanking::new);
        ranking.setSeries(series);
        ranking.setRankingPosition(rankingPosition);
        ranking.setScore((float) votes);
        ranking.setVoteCount(votes);
        ranking.setPeriod(period);
        ranking.setCalculatedAt(calculatedAt);
        seriesRankingRepository.save(ranking);

        return toReaderFeedbackImportResponse(savedImport);
    }

    private List<ChapterLikeLogRepository.SeriesVoteCount> sortedVoteCounts(LocalDateTime from, LocalDateTime to) {
        return chapterLikeLogRepository.countVotesBySeriesBetween(from, to)
                .stream()
                .sorted(Comparator
                        .comparing(ChapterLikeLogRepository.SeriesVoteCount::getVoteCount,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ChapterLikeLogRepository.SeriesVoteCount::getSeriesTitle,
                                Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    private void validatePeriodRange(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from and to are required");
        }
        if (!from.isBefore(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "periodStart must be before periodEnd");
        }
    }

    @Transactional
    public List<SeriesBoardAssignment> assignBoardPanel(MangaSeries series) {
        if (series == null || series.getSeriesId() == null) {
            return List.of();
        }
        List<SeriesBoardAssignment> existing = seriesBoardAssignmentRepository
                .findBySeriesSeriesIdOrderByAssignedAtAsc(series.getSeriesId());
        if (!existing.isEmpty()) {
            return existing;
        }
        if (!REVIEWING_SERIES_STATUS.equalsIgnoreCase(series.getStatus())) {
            return List.of();
        }

        List<User> activeBoardMembers = new ArrayList<>(userRepository
                .findByRoleRoleNameAndStatusOrderByUsernameAsc(BOARD_ROLE, ACTIVE_STATUS));
        if (activeBoardMembers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No active editorial board member is available");
        }

        if (activeBoardMembers.size() > BOARD_PANEL_SIZE) {
            List<Long> boardMemberIds = activeBoardMembers.stream()
                    .map(User::getUserId)
                    .toList();
            Map<Long, Long> assignmentCounts = new HashMap<>();
            seriesBoardAssignmentRepository.countAssignmentsByBoardMemberIds(boardMemberIds)
                    .forEach(count -> assignmentCounts.put(
                            count.getBoardMemberId(), count.getAssignmentCount()));

            // Shuffle first so members with the same workload are selected randomly.
            Collections.shuffle(activeBoardMembers);
            activeBoardMembers.sort(Comparator.comparingLong(
                    member -> assignmentCounts.getOrDefault(member.getUserId(), 0L)));
        }

        LocalDateTime now = LocalDateTime.now();
        List<SeriesBoardAssignment> created = activeBoardMembers.stream()
                .limit(BOARD_PANEL_SIZE)
                .map(boardMember -> {
                    SeriesBoardAssignment assignment = new SeriesBoardAssignment();
                    assignment.setSeries(series);
                    assignment.setBoardMember(boardMember);
                    assignment.setAssignedAt(now);
                    notify(boardMember, "SERIES_VOTE_ASSIGNED", series.getSeriesId(),
                            "You were assigned to vote on series \"" + series.getTitle() + "\".");
                    return seriesBoardAssignmentRepository.save(assignment);
                })
                .toList();
        return created;
    }

    private void applyScheduleRequest(PublishSchedule schedule, ScheduleRequest request) {
        MangaSeries series = mangaSeriesRepository.findById(request.seriesId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Series not found"));
        schedule.setSeries(series);
        schedule.setPublishDate(request.publishDate());
        schedule.setFrequency(request.frequency().trim().toUpperCase(Locale.ROOT));
        schedule.setStatus(blankToDefault(request.status(), "PLANNED"));
    }

    private void notifyBoardScheduleChange(PublishSchedule schedule, String type) {
        MangaSeries series = schedule.getSeries();
        String message = "Publish schedule for \"" + (series == null ? "" : series.getTitle())
                + "\" is " + schedule.getFrequency() + " at " + schedule.getPublishDate();
        userRepository.findByRoleRoleNameAndStatusOrderByUsernameAsc(BOARD_ROLE, ACTIVE_STATUS)
                .forEach(board -> notify(board, type, schedule.getScheduleId(), message));
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private User currentEditorialBoard() {
        return userRepository.findByEmail(currentEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Editorial board user not found"));
    }

    private Role resolveManagedRole(String roleName) {
        String normalizedRole = normalizeRole(roleName);
        return roleRepository.findByRoleName(normalizedRole)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName(normalizedRole);
                    return roleRepository.save(role);
                });
    }

    private String normalizeRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "role is required");
        }
        String normalizedRole = roleName.trim()
                .toUpperCase(Locale.ROOT)
                .replace("-", "_")
                .replace(" ", "_");
        if (!MANAGED_ROLES.contains(normalizedRole)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role must be MANGAKA, ASSISTANT, TANTOU_EDITOR, or EDITORIAL_BOARD");
        }
        return normalizedRole;
    }

    private String normalizeBoardDecision(String decisionType, boolean allowCancel) {
        if (decisionType == null || decisionType.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "decisionType is required");
        }
        String normalizedDecision = decisionType.trim()
                .toUpperCase(Locale.ROOT)
                .replace("-", "_")
                .replace(" ", "_");
        if ("APPROVED".equals(normalizedDecision)) {
            normalizedDecision = APPROVE_DECISION;
        } else if ("REJECTED".equals(normalizedDecision)) {
            normalizedDecision = REJECT_DECISION;
        } else if ("CANCELLED".equals(normalizedDecision)) {
            normalizedDecision = CANCEL_DECISION;
        }
        boolean validDecision = APPROVE_DECISION.equals(normalizedDecision)
                || REJECT_DECISION.equals(normalizedDecision)
                || (allowCancel && CANCEL_DECISION.equals(normalizedDecision));
        if (!validDecision) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    allowCancel
                            ? "decisionType must be APPROVE, REJECT, or CANCEL"
                            : "decisionType must be APPROVE or REJECT");
        }
        return normalizedDecision;
    }

    private void applyVotingResult(MangaSeries series) {
        long requiredVotes = requiredVotes(series.getSeriesId());
        long approveVotes = countDecisions(series.getSeriesId(), APPROVE_DECISION);
        long rejectVotes = countDecisions(series.getSeriesId(), REJECT_DECISION);

        if (approveVotes >= requiredVotes) {
            series.setStatus(PUBLISHED_SERIES_STATUS);
            mangaSeriesRepository.save(series);
            notifyBoardResult(series, "SERIES_APPROVED",
                    "Series \"" + series.getTitle() + "\" đã được Hội đồng Biên tập duyệt");
        } else if (rejectVotes >= requiredVotes) {
            series.setStatus(REVISION_REQUESTED_STATUS);
            mangaSeriesRepository.save(series);
            notifyBoardResult(series, "SERIES_REJECTED",
                    "Series \"" + series.getTitle() + "\" bị Hội đồng Biên tập từ chối, cần chỉnh sửa");
        }
    }

    private void notifyBoardResult(MangaSeries series, String type, String message) {
        notify(series.getAuthor(), type, series.getSeriesId(), message);
        notify(series.getTantouEditor(), type, series.getSeriesId(), message);
    }

    private void notify(User user, String type, Long refId, String message) {
        if (user == null)
            return;
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setReferenceId(refId);
        n.setMessage(message);
        n.setIsRead(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(n);
    }

    private long requiredVotes(Long seriesId) {
        long assignedBoardMembers = totalAssignedBoardMembers(seriesId);
        return assignedBoardMembers == 0 ? 0 : assignedBoardMembers / 2 + 1;
    }

    private long totalEligibleBoardMembers() {
        return userRepository.countByRoleRoleNameAndStatus(BOARD_ROLE, ACTIVE_STATUS);
    }

    private long totalAssignedBoardMembers(Long seriesId) {
        return seriesBoardAssignmentRepository.countBySeriesSeriesId(seriesId);
    }

    private long countDecisions(Long seriesId, String decisionType) {
        return boardDecisionRepository.countPanelDecisionsBySeriesIdAndDecisionType(seriesId, decisionType);
    }

    private String normalizeStatus(String status, String defaultStatus) {
        if (status == null || status.isBlank()) {
            return defaultStatus;
        }
        String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
        if (!USER_STATUSES.contains(normalizedStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be ACTIVE, INACTIVE, SUSPENDED, or DELETED");
        }
        return normalizedStatus;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank()
                ? defaultValue
                : value.trim();
    }

    private UserResponse toUserResponse(User user) {
        User createdBy = user.getCreatedBy();
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getStatus(),
                user.getRole() == null ? null : user.getRole().getRoleName(),
                createdBy == null ? null : createdBy.getUserId(),
                createdBy == null ? null : createdBy.getUsername(),
                user.getCreatedAt());
    }

    private ReviewSeriesResponse toReviewSeriesResponse(MangaSeries series, User currentUser) {
        List<SeriesBoardAssignment> assignments = assignBoardPanel(series);
        List<BoardDecisionResponse> decisions = boardDecisionRepository
                .findPanelDecisionsBySeriesIdOrderByDecisionDateDesc(series.getSeriesId())
                .stream()
                .map(this::toBoardDecisionResponse)
                .toList();
        boolean currentUserAssigned = assignments.stream()
                .map(SeriesBoardAssignment::getBoardMember)
                .anyMatch(boardMember -> boardMember != null
                        && currentUser.getUserId() != null
                        && currentUser.getUserId().equals(boardMember.getUserId()));
        String currentUserDecision = decisions.stream()
                .filter(decision -> currentUser.getUserId() != null
                        && currentUser.getUserId().equals(decision.boardMemberId()))
                .findFirst()
                .map(BoardDecisionResponse::decisionType)
                .orElse(null);
        String author = series.getAuthor() == null
                ? null
                : series.getAuthor().getUsername();
        User editor = series.getTantouEditor();

        return new ReviewSeriesResponse(
                series.getSeriesId(),
                series.getTitle(),
                author,
                parseGenres(series.getGenre()),
                series.getCoverImage(),
                series.getDescription(),
                series.getStatus(),
                series.getStoryboardUrl(),
                series.getSubmittedAt(),
                countDecisions(series.getSeriesId(), APPROVE_DECISION),
                countDecisions(series.getSeriesId(), REJECT_DECISION),
                totalAssignedBoardMembers(series.getSeriesId()),
                requiredVotes(series.getSeriesId()),
                currentUserDecision,
                currentUserAssigned,
                decisions,
                editor == null ? null : editor.getUserId(),
                editor == null ? null : editor.getUsername(),
                editor == null ? null : editor.getEmail(),
                Boolean.TRUE.equals(series.getEditorAssignmentLocked()),
                seriesEditorRejectionRepository.countBySeriesSeriesId(series.getSeriesId()),
                userRepository.countByRoleRoleNameAndStatus("TANTOU_EDITOR", ACTIVE_STATUS),
                series.getPublicationType(),
                series.getArtStyle(),
                series.getCreatedAt(),
                totalEligibleBoardMembers(),
                assignments.stream()
                        .map(this::toBoardMemberAssignmentResponse)
                        .toList(),
                seriesFileRepository.findBySeriesSeriesIdOrderByUploadedAtDesc(series.getSeriesId())
                        .stream()
                        .map(this::toUploadedFileResponse)
                        .toList());
    }

    private BoardDecisionResponse toBoardDecisionResponse(BoardDecision decision) {
        User boardMember = decision.getBoardMember();
        MangaSeries series = decision.getSeries();
        return new BoardDecisionResponse(
                decision.getDecisionId(),
                series == null ? null : series.getSeriesId(),
                boardMember == null ? null : boardMember.getUserId(),
                boardMember == null ? null : boardMember.getUsername(),
                decision.getDecisionType(),
                decision.getReason(),
                decision.getDecisionDate());
    }

    private BoardMemberAssignmentResponse toBoardMemberAssignmentResponse(SeriesBoardAssignment assignment) {
        User boardMember = assignment.getBoardMember();
        return new BoardMemberAssignmentResponse(
                boardMember == null ? null : boardMember.getUserId(),
                boardMember == null ? null : boardMember.getUsername(),
                boardMember == null ? null : boardMember.getEmail(),
                assignment.getAssignedAt());
    }

    private UploadedFileResponse toUploadedFileResponse(SeriesFile file) {
        MangaSeries series = file.getSeries();
        return new UploadedFileResponse(
                file.getFileId(),
                series == null ? null : series.getSeriesId(),
                file.getFileName(),
                file.getOriginalFileName(),
                file.getFileUrl(),
                file.getContentType(),
                file.getFileSize(),
                file.getFileType(),
                file.getUploadedAt());
    }

    private ScheduleResponse toScheduleResponse(PublishSchedule schedule) {
        MangaSeries series = schedule.getSeries();
        boolean overdue = "PLANNED".equalsIgnoreCase(schedule.getStatus())
                && schedule.getPublishDate() != null
                && schedule.getPublishDate().isBefore(LocalDateTime.now());
        return new ScheduleResponse(
                schedule.getScheduleId(),
                series == null ? null : series.getSeriesId(),
                series == null ? null : series.getTitle(),
                schedule.getPublishDate(),
                schedule.getFrequency(),
                schedule.getStatus(),
                overdue);
    }

    private ReaderVoteResponse toReaderVoteResponse(ChapterLikeLog likeLog) {
        Chapter chapter = likeLog.getChapter();
        MangaSeries series = chapter == null ? null : chapter.getSeries();
        GuestAccessLog guestLog = likeLog.getGuestLog();
        return new ReaderVoteResponse(
                likeLog.getLikeId(),
                series == null ? null : series.getSeriesId(),
                series == null ? null : series.getTitle(),
                chapter == null ? null : chapter.getChapterNumber(),
                chapter == null ? null : chapter.getTitle(),
                guestLog == null ? null : guestLog.getSessionToken(),
                likeLog.getLikedAt());
    }

    private ReaderFeedbackImportResponse toReaderFeedbackImportResponse(ReaderFeedbackImport feedbackImport) {
        MangaSeries series = feedbackImport.getSeries();
        return new ReaderFeedbackImportResponse(
                feedbackImport.getImportId(),
                series == null ? null : series.getSeriesId(),
                series == null ? null : series.getTitle(),
                feedbackImport.getPeriod(),
                feedbackImport.getVoteCount(),
                feedbackImport.getImportedAt());
    }

    private RankingResponse toRankingResponse(SeriesRanking ranking) {
        MangaSeries series = ranking.getSeries();
        return new RankingResponse(
                ranking.getRankingId(),
                series == null ? null : series.getSeriesId(),
                series == null ? null : series.getTitle(),
                ranking.getRankingPosition(),
                ranking.getScore(),
                ranking.getVoteCount(),
                ranking.getPeriod(),
                ranking.getCalculatedAt());
    }

    private NotificationResponse toNotificationResponse(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getType(),
                notification.getReferenceId(),
                notification.getMessage(),
                notification.getIsRead(),
                notification.getCreatedAt());
    }

    private List<String> parseGenres(String genre) {
        if (genre == null || genre.isBlank()) {
            return List.of();
        }
        return Arrays.stream(genre.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}
