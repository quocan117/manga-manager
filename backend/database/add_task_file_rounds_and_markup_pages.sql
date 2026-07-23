SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('tasks', 'U') IS NOT NULL
   AND COL_LENGTH('tasks', 'round_number') IS NULL
BEGIN
    ALTER TABLE tasks
        ADD round_number INT NOT NULL
            CONSTRAINT DF_tasks_round_number DEFAULT 1;
END;

IF OBJECT_ID('submissions', 'U') IS NOT NULL
   AND COL_LENGTH('submissions', 'round_number') IS NULL
BEGIN
    ALTER TABLE submissions
        ADD round_number INT NOT NULL
            CONSTRAINT DF_submissions_round_number DEFAULT 1;
END;

IF OBJECT_ID('series_files', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('series_files', 'task_id') IS NULL
    BEGIN
        ALTER TABLE series_files ADD task_id BIGINT NULL;
    END;

    IF COL_LENGTH('series_files', 'round_number') IS NULL
    BEGIN
        ALTER TABLE series_files
            ADD round_number INT NOT NULL
                CONSTRAINT DF_series_files_round_number DEFAULT 1;
    END;

    IF COL_LENGTH('series_files', 'purpose') IS NULL
    BEGIN
        ALTER TABLE series_files ADD purpose NVARCHAR(50) NULL;
    END;

    IF OBJECT_ID('tasks', 'U') IS NOT NULL
       AND NOT EXISTS (
           SELECT 1
           FROM sys.foreign_keys AS fk
           JOIN sys.foreign_key_columns AS fkc
             ON fkc.constraint_object_id = fk.object_id
           WHERE fk.parent_object_id = OBJECT_ID('series_files')
             AND fk.referenced_object_id = OBJECT_ID('tasks')
             AND COL_NAME(fkc.parent_object_id, fkc.parent_column_id) = 'task_id'
       )
    BEGIN
        ALTER TABLE series_files
            ADD CONSTRAINT FK_series_files_tasks
                FOREIGN KEY (task_id) REFERENCES tasks(task_id);
    END;

    IF NOT EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'IX_series_files_task_round_purpose'
          AND object_id = OBJECT_ID('series_files')
    )
    BEGIN
        CREATE INDEX IX_series_files_task_round_purpose
            ON series_files(task_id, round_number, purpose, active);
    END;
END;

IF OBJECT_ID('task_markup_pages', 'U') IS NULL
BEGIN
    CREATE TABLE task_markup_pages (
        markup_page_id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        task_id BIGINT NOT NULL,
        round_number INT NOT NULL,
        image_url NVARCHAR(MAX) NULL,
        canvas_data NVARCHAR(MAX) NULL,
        order_index INT NULL,
        created_at DATETIME2 NULL,
        CONSTRAINT FK_task_markup_pages_tasks
            FOREIGN KEY (task_id) REFERENCES tasks(task_id)
    );

END;

IF OBJECT_ID('task_markup_pages', 'U') IS NOT NULL
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'IX_task_markup_pages_task_round'
          AND object_id = OBJECT_ID('task_markup_pages')
    )
    BEGIN
        CREATE INDEX IX_task_markup_pages_task_round
            ON task_markup_pages(task_id, round_number, order_index);
    END;

    IF OBJECT_ID('tasks', 'U') IS NOT NULL
       AND NOT EXISTS (
           SELECT 1
           FROM sys.foreign_keys AS fk
           JOIN sys.foreign_key_columns AS fkc
             ON fkc.constraint_object_id = fk.object_id
           WHERE fk.parent_object_id = OBJECT_ID('task_markup_pages')
             AND fk.referenced_object_id = OBJECT_ID('tasks')
             AND COL_NAME(fkc.parent_object_id, fkc.parent_column_id) = 'task_id'
       )
    BEGIN
        ALTER TABLE task_markup_pages
            ADD CONSTRAINT FK_task_markup_pages_tasks
                FOREIGN KEY (task_id) REFERENCES tasks(task_id);
    END;
END;

COMMIT TRANSACTION;
