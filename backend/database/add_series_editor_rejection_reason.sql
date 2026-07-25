SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('series_editor_rejections', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('series_editor_rejections', 'reason') IS NULL
    BEGIN
        ALTER TABLE series_editor_rejections ADD reason NVARCHAR(MAX) NULL;
    END
    ELSE
    BEGIN
        ALTER TABLE series_editor_rejections ALTER COLUMN reason NVARCHAR(MAX) NULL;
    END;
END;

COMMIT TRANSACTION;
