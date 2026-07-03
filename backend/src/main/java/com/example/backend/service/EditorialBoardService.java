package com.example.backend.service;

import com.example.backend.dto.EditorialBoardDtos.CreateUserRequest;
import com.example.backend.dto.EditorialBoardDtos.BoardDecisionRequest;
import com.example.backend.dto.EditorialBoardDtos.BoardDecisionResponse;
import com.example.backend.dto.EditorialBoardDtos.ReviewSeriesResponse;
import com.example.backend.dto.EditorialBoardDtos.UpdateUserRequest;
import com.example.backend.dto.EditorialBoardDtos.UserResponse;
import com.example.backend.dto.ReviewRegistrationRequest;
import com.example.backend.model.BoardDecision;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.RegistrationRequest;
import com.example.backend.model.Role;
import com.example.backend.model.User;
import com.example.backend.model.Notification;
import com.example.backend.repository.BoardDecisionRepository;
import com.example.backend.repository.MangaSeriesRepository;
import com.example.backend.repository.RegistrationRequestRepository;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.NotificationRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class EditorialBoardService {
    private static final String BOARD_ROLE = "EDITORIAL_BOARD";
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String REVIEWING_SERIES_STATUS = "REVIEWING";
    private static final String PUBLISHED_SERIES_STATUS = "Published";
    private static final String REVISION_REQUESTED_STATUS = "REVISION_REQUESTED";
    private static final String CANCELLED_SERIES_STATUS = "CANCELLED";
    private static final String APPROVE_DECISION = "APPROVE";
    private static final String REJECT_DECISION = "REJECT";
    private static final String CANCEL_DECISION = "CANCEL";

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

    public EditorialBoardService(
            RegistrationRequestRepository requestRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            MangaSeriesRepository mangaSeriesRepository,
            BoardDecisionRepository boardDecisionRepository,
            NotificationRepository notificationRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.mangaSeriesRepository = mangaSeriesRepository;
        this.boardDecisionRepository = boardDecisionRepository;
        this.notificationRepository = notificationRepository;
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

    public List<ReviewSeriesResponse> getReviewingSeries() {
        User currentUser = currentEditorialBoard();
        return mangaSeriesRepository.findByStatusIgnoreCaseOrderBySubmittedAtDesc(REVIEWING_SERIES_STATUS)
                .stream()
                .map(series -> toReviewSeriesResponse(series, currentUser))
                .toList();
    }

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
        return boardDecisionRepository.findBySeriesSeriesIdOrderByDecisionDateDesc(seriesId)
                .stream()
                .map(this::toBoardDecisionResponse)
                .toList();
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
    public ReviewSeriesResponse voteSeries(Long seriesId, BoardDecisionRequest request) {
        User boardMember = currentEditorialBoard();
        MangaSeries series = mangaSeriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Series not found"));
        if (!REVIEWING_SERIES_STATUS.equalsIgnoreCase(series.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only reviewing series can be voted on");
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

    private User currentEditorialBoard() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
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
        long requiredVotes = requiredVotes();
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
        if (user == null) return;
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setReferenceId(refId);
        n.setMessage(message);
        n.setIsRead(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(n);
    }

    private long requiredVotes() {
        long activeBoardMembers = totalBoardMembers();
        return Math.max(1, activeBoardMembers / 2 + 1);
    }

    private long totalBoardMembers() {
        return userRepository.countByRoleRoleNameAndStatus(BOARD_ROLE, ACTIVE_STATUS);
    }

    private long countDecisions(Long seriesId, String decisionType) {
        return boardDecisionRepository.countBySeriesSeriesIdAndDecisionTypeIgnoreCase(seriesId, decisionType);
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
        List<BoardDecisionResponse> decisions = boardDecisionRepository
                .findBySeriesSeriesIdOrderByDecisionDateDesc(series.getSeriesId())
                .stream()
                .map(this::toBoardDecisionResponse)
                .toList();
        String currentUserDecision = decisions.stream()
                .filter(decision -> currentUser.getUserId() != null
                        && currentUser.getUserId().equals(decision.boardMemberId()))
                .findFirst()
                .map(BoardDecisionResponse::decisionType)
                .orElse(null);
        String author = series.getAuthor() == null
                ? null
                : series.getAuthor().getUsername();

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
                totalBoardMembers(),
                requiredVotes(),
                currentUserDecision,
                decisions);
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
