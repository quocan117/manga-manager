SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF EXISTS (
    SELECT 1
    FROM sys.key_constraints
    WHERE name = 'uk_page_drawings_page'
      AND parent_object_id = OBJECT_ID('page_drawings')
)
BEGIN
    ALTER TABLE page_drawings DROP CONSTRAINT uk_page_drawings_page;
END;

IF EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'uk_page_drawings_page'
      AND object_id = OBJECT_ID('page_drawings')
)
BEGIN
    DROP INDEX uk_page_drawings_page ON page_drawings;
END;

IF EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'uk_page_drawings_page_task'
      AND object_id = OBJECT_ID('page_drawings')
)
BEGIN
    DROP INDEX uk_page_drawings_page_task ON page_drawings;
END;

IF COL_LENGTH('page_drawings', 'task_id') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE name = 'ux_page_drawings_master'
         AND object_id = OBJECT_ID('page_drawings')
   )
BEGIN
    CREATE UNIQUE INDEX ux_page_drawings_master
        ON page_drawings (page_id)
        WHERE task_id IS NULL;
END;

IF COL_LENGTH('page_drawings', 'task_id') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE name = 'ux_page_drawings_task'
         AND object_id = OBJECT_ID('page_drawings')
   )
BEGIN
    CREATE UNIQUE INDEX ux_page_drawings_task
        ON page_drawings (task_id)
        WHERE task_id IS NOT NULL;
END;

COMMIT TRANSACTION;
