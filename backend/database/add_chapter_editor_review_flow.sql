SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('chapters', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('chapters', 'manuscript_url') IS NULL
    BEGIN
        ALTER TABLE chapters ADD manuscript_url NVARCHAR(MAX) NULL;
    END
    ELSE
    BEGIN
        ALTER TABLE chapters ALTER COLUMN manuscript_url NVARCHAR(MAX) NULL;
    END
END;

IF OBJECT_ID('chapter_revision_notes', 'U') IS NULL
BEGIN
    CREATE TABLE chapter_revision_notes (
        note_id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        chapter_id BIGINT NULL,
        editor_id BIGINT NULL,
        image_url NVARCHAR(MAX) NULL,
        canvas_data NVARCHAR(MAX) NULL,
        order_index INT NULL,
        created_at DATETIME2 NULL,
        CONSTRAINT fk_chapter_revision_notes_chapter
            FOREIGN KEY (chapter_id) REFERENCES chapters(chapter_id),
        CONSTRAINT fk_chapter_revision_notes_editor
            FOREIGN KEY (editor_id) REFERENCES users(user_id)
    );
END
ELSE
BEGIN
    IF COL_LENGTH('chapter_revision_notes', 'image_url') IS NOT NULL
    BEGIN
        ALTER TABLE chapter_revision_notes ALTER COLUMN image_url NVARCHAR(MAX) NULL;
    END;

    IF COL_LENGTH('chapter_revision_notes', 'canvas_data') IS NOT NULL
    BEGIN
        ALTER TABLE chapter_revision_notes ALTER COLUMN canvas_data NVARCHAR(MAX) NULL;
    END;
END;

COMMIT TRANSACTION;
