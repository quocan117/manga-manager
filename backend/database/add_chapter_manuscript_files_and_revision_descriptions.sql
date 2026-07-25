SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('series_files', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('series_files', 'chapter_id') IS NULL
    BEGIN
        ALTER TABLE series_files ADD chapter_id BIGINT NULL;
    END;

    IF OBJECT_ID('chapters', 'U') IS NOT NULL
       AND NOT EXISTS (
           SELECT 1
           FROM sys.foreign_keys AS fk
           JOIN sys.foreign_key_columns AS fkc
             ON fkc.constraint_object_id = fk.object_id
           WHERE fk.parent_object_id = OBJECT_ID('series_files')
             AND fk.referenced_object_id = OBJECT_ID('chapters')
             AND COL_NAME(fkc.parent_object_id, fkc.parent_column_id) = 'chapter_id'
       )
    BEGIN
        ALTER TABLE series_files
            ADD CONSTRAINT FK_series_files_chapters
                FOREIGN KEY (chapter_id) REFERENCES chapters(chapter_id);
    END;

    IF NOT EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'IX_series_files_chapter_purpose'
          AND object_id = OBJECT_ID('series_files')
    )
    BEGIN
        CREATE INDEX IX_series_files_chapter_purpose
            ON series_files(chapter_id, purpose, active, uploaded_at);
    END;
END;

IF OBJECT_ID('chapter_revision_notes', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('chapter_revision_notes', 'description') IS NULL
    BEGIN
        ALTER TABLE chapter_revision_notes ADD description NVARCHAR(MAX) NULL;
    END
    ELSE
    BEGIN
        ALTER TABLE chapter_revision_notes ALTER COLUMN description NVARCHAR(MAX) NULL;
    END;
END;

IF OBJECT_ID('chapters', 'U') IS NOT NULL
   AND COL_LENGTH('chapters', 'manuscript_url') IS NOT NULL
BEGIN
    ALTER TABLE chapters DROP COLUMN manuscript_url;
END;

COMMIT TRANSACTION;
