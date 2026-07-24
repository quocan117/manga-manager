SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('chapter_board_reviews', 'U') IS NULL
BEGIN
    CREATE TABLE chapter_board_reviews (
        review_id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        chapter_id BIGINT NOT NULL,
        board_member_id BIGINT NOT NULL,
        confirmed BIT NULL,
        comment NVARCHAR(MAX) NULL,
        reviewed_at DATETIME2 NULL,
        CONSTRAINT UQ_chapter_board_reviews_chapter_member
            UNIQUE (chapter_id, board_member_id),
        CONSTRAINT FK_chapter_board_reviews_chapter
            FOREIGN KEY (chapter_id) REFERENCES chapters(chapter_id),
        CONSTRAINT FK_chapter_board_reviews_board_member
            FOREIGN KEY (board_member_id) REFERENCES users(user_id)
    );
END;

IF OBJECT_ID('chapter_board_reviews', 'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE name = 'IX_chapter_board_reviews_member_pending'
         AND object_id = OBJECT_ID('chapter_board_reviews')
   )
BEGIN
    CREATE INDEX IX_chapter_board_reviews_member_pending
        ON chapter_board_reviews(board_member_id, confirmed, chapter_id);
END;

COMMIT TRANSACTION;
