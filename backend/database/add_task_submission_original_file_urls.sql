SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('tasks', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('tasks', 'original_file_url') IS NULL
    BEGIN
        ALTER TABLE tasks ADD original_file_url NVARCHAR(MAX) NULL;
    END
    ELSE
    BEGIN
        ALTER TABLE tasks ALTER COLUMN original_file_url NVARCHAR(MAX) NULL;
    END
END;

IF OBJECT_ID('submissions', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('submissions', 'original_file_url') IS NULL
    BEGIN
        ALTER TABLE submissions ADD original_file_url NVARCHAR(MAX) NULL;
    END
    ELSE
    BEGIN
        ALTER TABLE submissions ALTER COLUMN original_file_url NVARCHAR(MAX) NULL;
    END
END;

COMMIT TRANSACTION;
