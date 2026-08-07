SET XACT_ABORT ON;
SET NOCOUNT ON;

BEGIN TRANSACTION;

IF OBJECT_ID('chapters', 'U') IS NOT NULL
   AND COL_LENGTH('chapters', 'expected_pages') IS NULL
BEGIN
    ALTER TABLE chapters ADD expected_pages INT NULL;
END;

IF OBJECT_ID('chapters', 'U') IS NOT NULL
   AND COL_LENGTH('chapters', 'expected_pages') IS NOT NULL
BEGIN
    UPDATE chapter
    SET expected_pages = CASE
        WHEN page_totals.page_count > 0 THEN page_totals.page_count
        ELSE 1
    END
    FROM chapters AS chapter
    OUTER APPLY (
        SELECT COUNT(*) AS page_count
        FROM pages AS page
        WHERE page.chapter_id = chapter.chapter_id
          AND (page.page_status IS NULL OR UPPER(page.page_status) <> 'DELETED')
    ) AS page_totals
    WHERE chapter.expected_pages IS NULL;

    ALTER TABLE chapters ALTER COLUMN expected_pages INT NOT NULL;
END;

COMMIT TRANSACTION;
