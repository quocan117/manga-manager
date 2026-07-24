SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('manga_series', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('manga_series', 'publication_coordinator_id') IS NULL
    BEGIN
        ALTER TABLE manga_series ADD publication_coordinator_id BIGINT NULL;
    END;

    IF COL_LENGTH('manga_series', 'coordinator_assigned_at') IS NULL
    BEGIN
        ALTER TABLE manga_series ADD coordinator_assigned_at DATETIME2 NULL;
    END;

    IF OBJECT_ID('users', 'U') IS NOT NULL
       AND NOT EXISTS (
           SELECT 1
           FROM sys.foreign_keys AS fk
           JOIN sys.foreign_key_columns AS fkc
             ON fkc.constraint_object_id = fk.object_id
           WHERE fk.parent_object_id = OBJECT_ID('manga_series')
             AND fk.referenced_object_id = OBJECT_ID('users')
             AND COL_NAME(fkc.parent_object_id, fkc.parent_column_id) =
                 'publication_coordinator_id'
       )
    BEGIN
        ALTER TABLE manga_series
            ADD CONSTRAINT FK_manga_series_publication_coordinator
                FOREIGN KEY (publication_coordinator_id) REFERENCES users(user_id);
    END;

    IF NOT EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'IX_manga_series_publication_coordinator'
          AND object_id = OBJECT_ID('manga_series')
    )
    BEGIN
        CREATE INDEX IX_manga_series_publication_coordinator
            ON manga_series(publication_coordinator_id, status);
    END;

    UPDATE manga_series
    SET status = 'PUBLISHED'
    WHERE UPPER(status) = 'PUBLISHED';
END;

COMMIT TRANSACTION;
