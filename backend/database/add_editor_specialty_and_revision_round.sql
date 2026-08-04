SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF COL_LENGTH('users', 'specialty') IS NULL
BEGIN
    ALTER TABLE users ADD specialty NVARCHAR(MAX) NULL;
END;

UPDATE users
SET specialty = N'Hành động, Phiêu lưu, Hài hước, Tình cảm, Kinh dị, Siêu nhiên, Trinh thám, Thể thao, Học đường, Lịch sử'
WHERE specialty IS NULL
  AND role_id IN (
      SELECT role_id
      FROM roles
      WHERE UPPER(role_name) = 'TANTOU_EDITOR'
  );

IF OBJECT_ID('chapter_revision_notes', 'U') IS NOT NULL
   AND COL_LENGTH('chapter_revision_notes', 'round_number') IS NULL
BEGIN
    ALTER TABLE chapter_revision_notes
        ADD round_number INT NOT NULL
            CONSTRAINT DF_chapter_revision_notes_round_number DEFAULT 1;
END;

COMMIT TRANSACTION;
