SET XACT_ABORT ON;
SET LOCK_TIMEOUT 10000;
BEGIN TRANSACTION;

DECLARE @LikeTargets TABLE (
    chapter_id BIGINT PRIMARY KEY,
    target_likes INT NOT NULL
);

-- The API counts likes from chapter_like_logs, so seed real like rows instead
-- of writing a display number into chapters.
INSERT INTO @LikeTargets (chapter_id, target_likes)
SELECT
    chapters.chapter_id,
    ABS(CHECKSUM(CONCAT('manga-manager-like-seed:', chapters.chapter_id))) % 900 + 100
FROM chapters
JOIN manga_series AS series
    ON series.series_id = chapters.series_id
JOIN users
    ON users.user_id = series.author_id
WHERE users.email LIKE '%@manga.test';

DECLARE @MaxTargetLikes INT;
SELECT @MaxTargetLikes = ISNULL(MAX(target_likes), 0)
FROM @LikeTargets;

;WITH Numbers AS (
    SELECT 1 AS number
    UNION ALL
    SELECT number + 1
    FROM Numbers
    WHERE number < @MaxTargetLikes
)
INSERT INTO guest_access_logs (
    session_token,
    ip_address,
    user_agent,
    created_at,
    last_active_at
)
SELECT
    CONCAT('seed-like-chapter-', targets.chapter_id, '-', numbers.number),
    '127.0.0.1',
    'Seeded manga like data',
    DATEADD(MINUTE, -numbers.number, SYSDATETIME()),
    DATEADD(MINUTE, -numbers.number, SYSDATETIME())
FROM @LikeTargets AS targets
JOIN Numbers AS numbers
    ON numbers.number <= targets.target_likes
WHERE NOT EXISTS (
    SELECT 1
    FROM guest_access_logs AS guests
    WHERE guests.session_token = CONCAT('seed-like-chapter-', targets.chapter_id, '-', numbers.number)
)
OPTION (MAXRECURSION 0);

;WITH Numbers AS (
    SELECT 1 AS number
    UNION ALL
    SELECT number + 1
    FROM Numbers
    WHERE number < @MaxTargetLikes
)
INSERT INTO chapter_like_logs (
    log_id,
    chapter_id,
    liked_at
)
SELECT
    guests.log_id,
    targets.chapter_id,
    DATEADD(MINUTE, -numbers.number, SYSDATETIME())
FROM @LikeTargets AS targets
JOIN Numbers AS numbers
    ON numbers.number <= targets.target_likes
JOIN guest_access_logs AS guests
    ON guests.session_token = CONCAT('seed-like-chapter-', targets.chapter_id, '-', numbers.number)
WHERE NOT EXISTS (
    SELECT 1
    FROM chapter_like_logs AS likes
    WHERE likes.log_id = guests.log_id
      AND likes.chapter_id = targets.chapter_id
)
OPTION (MAXRECURSION 0);

COMMIT TRANSACTION;

SELECT
    chapters.chapter_id,
    series.title AS series_title,
    chapters.title AS chapter_title,
    COUNT(likes.like_id) AS likes
FROM chapters
JOIN manga_series AS series
    ON series.series_id = chapters.series_id
JOIN users
    ON users.user_id = series.author_id
LEFT JOIN chapter_like_logs AS likes
    ON likes.chapter_id = chapters.chapter_id
WHERE users.email LIKE '%@manga.test'
GROUP BY chapters.chapter_id, series.title, chapters.title
ORDER BY series.title, chapters.chapter_id;
