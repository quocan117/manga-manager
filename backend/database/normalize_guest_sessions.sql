SET XACT_ABORT ON;
SET LOCK_TIMEOUT 10000;
BEGIN TRANSACTION;

UPDATE guest_access_logs
SET session_token = CONCAT('legacy-', log_id)
WHERE session_token IS NULL OR LTRIM(RTRIM(session_token)) = '';

;WITH DuplicateLikes AS (
    SELECT likes.like_id,
           ROW_NUMBER() OVER (
               PARTITION BY guests.session_token, likes.chapter_id
               ORDER BY likes.like_id) AS row_number
    FROM chapter_like_logs AS likes
    JOIN guest_access_logs AS guests ON guests.log_id = likes.log_id
)
DELETE likes
FROM chapter_like_logs AS likes
JOIN DuplicateLikes AS duplicates ON duplicates.like_id = likes.like_id
WHERE duplicates.row_number > 1;

;WITH CanonicalGuests AS (
    SELECT session_token, MIN(log_id) AS canonical_log_id
    FROM guest_access_logs
    GROUP BY session_token
)
UPDATE likes
SET log_id = canonical.canonical_log_id
FROM chapter_like_logs AS likes
JOIN guest_access_logs AS guests ON guests.log_id = likes.log_id
JOIN CanonicalGuests AS canonical ON canonical.session_token = guests.session_token
WHERE likes.log_id <> canonical.canonical_log_id;

;WITH DuplicateGuests AS (
    SELECT log_id,
           ROW_NUMBER() OVER (
               PARTITION BY session_token
               ORDER BY log_id) AS row_number
    FROM guest_access_logs
)
DELETE guests
FROM guest_access_logs AS guests
JOIN DuplicateGuests AS duplicates ON duplicates.log_id = guests.log_id
WHERE duplicates.row_number > 1;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'ux_guest_access_session_token'
      AND object_id = OBJECT_ID('guest_access_logs')
)
BEGIN
    CREATE UNIQUE INDEX ux_guest_access_session_token
        ON guest_access_logs (session_token);
END;

COMMIT TRANSACTION;
