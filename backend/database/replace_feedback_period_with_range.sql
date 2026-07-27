SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('reader_feedback_imports', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('reader_feedback_imports', 'period_start') IS NULL
    BEGIN
        ALTER TABLE reader_feedback_imports ADD period_start DATETIME2 NULL;
    END;

    IF COL_LENGTH('reader_feedback_imports', 'period_end') IS NULL
    BEGIN
        ALTER TABLE reader_feedback_imports ADD period_end DATETIME2 NULL;
    END;

    IF COL_LENGTH('reader_feedback_imports', 'period') IS NOT NULL
    BEGIN
        ALTER TABLE reader_feedback_imports DROP COLUMN period;
    END;
END;

IF OBJECT_ID('series_rankings', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('series_rankings', 'period_start') IS NULL
    BEGIN
        ALTER TABLE series_rankings ADD period_start DATETIME2 NULL;
    END;

    IF COL_LENGTH('series_rankings', 'period_end') IS NULL
    BEGIN
        ALTER TABLE series_rankings ADD period_end DATETIME2 NULL;
    END;

    IF COL_LENGTH('series_rankings', 'period') IS NOT NULL
    BEGIN
        ALTER TABLE series_rankings DROP COLUMN period;
    END;
END;

COMMIT TRANSACTION;
