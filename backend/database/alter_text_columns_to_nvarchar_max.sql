SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('board_decisions', 'U') IS NOT NULL
   AND COL_LENGTH('board_decisions', 'reason') IS NOT NULL
BEGIN
    ALTER TABLE board_decisions ALTER COLUMN reason NVARCHAR(MAX) NULL;
END;

IF OBJECT_ID('guest_access_logs', 'U') IS NOT NULL
   AND COL_LENGTH('guest_access_logs', 'user_agent') IS NOT NULL
BEGIN
    ALTER TABLE guest_access_logs ALTER COLUMN user_agent NVARCHAR(MAX) NULL;
END;

IF OBJECT_ID('registration_requests', 'U') IS NOT NULL
   AND COL_LENGTH('registration_requests', 'portfolio_url') IS NOT NULL
BEGIN
    ALTER TABLE registration_requests ALTER COLUMN portfolio_url NVARCHAR(MAX) NULL;
END;

IF OBJECT_ID('registration_requests', 'U') IS NOT NULL
   AND COL_LENGTH('registration_requests', 'introduction') IS NOT NULL
BEGIN
    ALTER TABLE registration_requests ALTER COLUMN introduction NVARCHAR(MAX) NULL;
END;

IF OBJECT_ID('registration_requests', 'U') IS NOT NULL
   AND COL_LENGTH('registration_requests', 'review_note') IS NOT NULL
BEGIN
    ALTER TABLE registration_requests ALTER COLUMN review_note NVARCHAR(MAX) NULL;
END;

IF OBJECT_ID('reader_feedback_imports', 'U') IS NOT NULL
   AND COL_LENGTH('reader_feedback_imports', 'source_note') IS NOT NULL
BEGIN
    ALTER TABLE reader_feedback_imports ALTER COLUMN source_note NVARCHAR(MAX) NULL;
END;

IF OBJECT_ID('tasks', 'U') IS NOT NULL
   AND COL_LENGTH('tasks', 'description') IS NOT NULL
BEGIN
    ALTER TABLE tasks ALTER COLUMN description NVARCHAR(MAX) NULL;
END;

IF OBJECT_ID('payments', 'U') IS NOT NULL
   AND COL_LENGTH('payments', 'description') IS NOT NULL
BEGIN
    ALTER TABLE payments ALTER COLUMN description NVARCHAR(MAX) NULL;
END;

IF OBJECT_ID('submissions', 'U') IS NOT NULL
   AND COL_LENGTH('submissions', 'note') IS NOT NULL
BEGIN
    ALTER TABLE submissions ALTER COLUMN note NVARCHAR(MAX) NULL;
END;

IF OBJECT_ID('submissions', 'U') IS NOT NULL
   AND COL_LENGTH('submissions', 'artifact_url') IS NOT NULL
BEGIN
    ALTER TABLE submissions ALTER COLUMN artifact_url NVARCHAR(MAX) NULL;
END;

IF OBJECT_ID('review_comments', 'U') IS NOT NULL
   AND COL_LENGTH('review_comments', 'comment_text') IS NOT NULL
BEGIN
    ALTER TABLE review_comments ALTER COLUMN comment_text NVARCHAR(MAX) NULL;
END;

COMMIT TRANSACTION;
