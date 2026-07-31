SET XACT_ABORT ON;
SET NOCOUNT ON;

BEGIN TRANSACTION;

IF OBJECT_ID('series_review_history', 'U') IS NULL
BEGIN
    CREATE TABLE series_review_history (
        history_id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        series_id BIGINT NOT NULL,
        actor_id BIGINT NULL,
        actor_role NVARCHAR(50) NULL,
        action NVARCHAR(100) NOT NULL,
        previous_status NVARCHAR(50) NULL,
        new_status NVARCHAR(50) NULL,
        reason NVARCHAR(MAX) NULL,
        reference_id BIGINT NULL,
        created_at DATETIME2 NOT NULL,
        CONSTRAINT FK_series_review_history_series
            FOREIGN KEY (series_id) REFERENCES manga_series(series_id),
        CONSTRAINT FK_series_review_history_actor
            FOREIGN KEY (actor_id) REFERENCES users(user_id)
    );

    CREATE INDEX IX_series_review_history_series_created
        ON series_review_history(series_id, created_at DESC);
END;

IF OBJECT_ID('chapter_page_history', 'U') IS NULL
BEGIN
    CREATE TABLE chapter_page_history (
        history_id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        page_id BIGINT NOT NULL,
        submission_id BIGINT NULL,
        approved_by BIGINT NULL,
        previous_image_url NVARCHAR(MAX) NULL,
        new_image_url NVARCHAR(MAX) NOT NULL,
        created_at DATETIME2 NOT NULL,
        CONSTRAINT FK_chapter_page_history_page
            FOREIGN KEY (page_id) REFERENCES pages(page_id),
        CONSTRAINT FK_chapter_page_history_submission
            FOREIGN KEY (submission_id) REFERENCES submissions(submission_id),
        CONSTRAINT FK_chapter_page_history_approved_by
            FOREIGN KEY (approved_by) REFERENCES users(user_id)
    );

    CREATE INDEX IX_chapter_page_history_page_created
        ON chapter_page_history(page_id, created_at DESC);
END;

INSERT INTO series_review_history (
    series_id,
    actor_id,
    actor_role,
    action,
    previous_status,
    new_status,
    reason,
    reference_id,
    created_at
)
SELECT
    decision.series_id,
    decision.board_member_id,
    role.role_name,
    CONCAT('BOARD_VOTE_', UPPER(decision.decision_type)),
    NULL,
    NULL,
    decision.reason,
    decision.decision_id,
    COALESCE(decision.decision_date, SYSDATETIME())
FROM board_decisions AS decision
LEFT JOIN users AS actor ON actor.user_id = decision.board_member_id
LEFT JOIN roles AS role ON role.role_id = actor.role_id
WHERE NOT EXISTS (
    SELECT 1
    FROM series_review_history AS history
    WHERE history.reference_id = decision.decision_id
      AND history.action = CONCAT('BOARD_VOTE_', UPPER(decision.decision_type))
);

COMMIT TRANSACTION;
