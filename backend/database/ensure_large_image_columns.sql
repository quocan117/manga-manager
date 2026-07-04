SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('pages', 'U') IS NOT NULL
   AND COL_LENGTH('pages', 'image_url') IS NOT NULL
BEGIN
    ALTER TABLE pages ALTER COLUMN image_url NVARCHAR(MAX) NULL;
END;

IF OBJECT_ID('page_drawings', 'U') IS NOT NULL
   AND COL_LENGTH('page_drawings', 'preview_image_url') IS NOT NULL
BEGIN
    ALTER TABLE page_drawings ALTER COLUMN preview_image_url NVARCHAR(MAX) NULL;
END;

IF OBJECT_ID('page_drawing_revisions', 'U') IS NOT NULL
   AND COL_LENGTH('page_drawing_revisions', 'preview_image_url') IS NOT NULL
BEGIN
    ALTER TABLE page_drawing_revisions ALTER COLUMN preview_image_url NVARCHAR(MAX) NULL;
END;

COMMIT TRANSACTION;
