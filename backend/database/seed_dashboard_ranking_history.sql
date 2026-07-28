/*
 * Demo history for the Editorial Board ranking dashboard.
 *
 * - Uses only existing test series whose author email ends with @manga.test.
 * - Uses only chapters that already belong to those series.
 * - Creates eight completed periods based on the latest publish schedule:
 *     DAILY = 1 day, WEEKLY = 7 days, MONTHLY = 30 days.
 *     Series without a schedule use WEEKLY periods.
 * - Stores real chapter_like_logs so the existing APIs return matching totals.
 * - Safe to run repeatedly. Rows created by this script have dedicated markers.
 *
 * Run cleanup_dashboard_ranking_seed.sql to remove only this demo history.
 */

SET XACT_ABORT ON;
SET NOCOUNT ON;
SET LOCK_TIMEOUT 10000;

BEGIN TRANSACTION;

DECLARE @SourceNote NVARCHAR(255) = N'Dữ liệu mẫu demo Dashboard';
DECLARE @SessionPrefix VARCHAR(64) = 'seed-dashboard-ranking-';
DECLARE @Today DATE = CONVERT(DATE, SYSDATETIME());
DECLARE @ImportedBy BIGINT;

IF OBJECT_ID('users', 'U') IS NULL
   OR OBJECT_ID('roles', 'U') IS NULL
   OR OBJECT_ID('manga_series', 'U') IS NULL
   OR OBJECT_ID('chapters', 'U') IS NULL
   OR OBJECT_ID('publish_schedules', 'U') IS NULL
   OR OBJECT_ID('guest_access_logs', 'U') IS NULL
   OR OBJECT_ID('chapter_like_logs', 'U') IS NULL
   OR OBJECT_ID('reader_feedback_imports', 'U') IS NULL
   OR OBJECT_ID('series_rankings', 'U') IS NULL
BEGIN
    ;THROW 51000, N'Thiếu bảng cần thiết để seed dữ liệu Dashboard.', 1;
END;

IF COL_LENGTH('reader_feedback_imports', 'period_start') IS NULL
   OR COL_LENGTH('reader_feedback_imports', 'period_end') IS NULL
   OR COL_LENGTH('series_rankings', 'period_start') IS NULL
   OR COL_LENGTH('series_rankings', 'period_end') IS NULL
BEGIN
    ;THROW 51001, N'Hãy chạy replace_feedback_period_with_range.sql trước.', 1;
END;

SELECT TOP (1)
    @ImportedBy = users.user_id
FROM users
JOIN roles ON roles.role_id = users.role_id
WHERE UPPER(roles.role_name) = 'EDITORIAL_BOARD'
  AND UPPER(COALESCE(users.status, '')) = 'ACTIVE'
ORDER BY users.user_id;

IF @ImportedBy IS NULL
BEGIN
    ;THROW 51002, N'Cần ít nhất một tài khoản EDITORIAL_BOARD đang ACTIVE.', 1;
END;

DECLARE @TargetSeries TABLE (
    series_id BIGINT PRIMARY KEY,
    days_per_period INT NOT NULL,
    anchor_end DATETIME2(0) NOT NULL
);

INSERT INTO @TargetSeries (series_id, days_per_period, anchor_end)
SELECT
    series.series_id,
    frequency.days_per_period,
    CASE frequency.days_per_period
        WHEN 1 THEN CONVERT(DATETIME2(0), @Today)
        WHEN 30 THEN CONVERT(
            DATETIME2(0),
            DATEADD(
                DAY,
                (DATEDIFF(DAY, CONVERT(DATE, '20000101', 112), @Today) / 30) * 30,
                CONVERT(DATE, '20000101', 112)
            )
        )
        ELSE CONVERT(
            DATETIME2(0),
            DATEADD(
                DAY,
                (DATEDIFF(DAY, CONVERT(DATE, '19000101', 112), @Today) / 7) * 7,
                CONVERT(DATE, '19000101', 112)
            )
        )
    END
FROM manga_series AS series
JOIN users AS author ON author.user_id = series.author_id
OUTER APPLY (
    SELECT TOP (1)
        UPPER(COALESCE(schedule.frequency, '')) AS frequency
    FROM publish_schedules AS schedule
    WHERE schedule.series_id = series.series_id
    ORDER BY
        COALESCE(schedule.publish_date, CONVERT(DATETIME2(0), '19000101', 112)) DESC,
        schedule.schedule_id DESC
) AS latest_schedule
CROSS APPLY (
    SELECT
        CASE latest_schedule.frequency
            WHEN 'DAILY' THEN 1
            WHEN 'MONTHLY' THEN 30
            ELSE 7
        END AS days_per_period
) AS frequency
WHERE author.email LIKE '%@manga.test'
  AND EXISTS (
      SELECT 1
      FROM chapters
      WHERE chapters.series_id = series.series_id
  );

IF NOT EXISTS (SELECT 1 FROM @TargetSeries)
BEGIN
    ;THROW 51003, N'Không tìm thấy series test có chapter để seed Dashboard.', 1;
END;

DECLARE @PeriodNumbers TABLE (
    period_number INT PRIMARY KEY
);

INSERT INTO @PeriodNumbers (period_number)
VALUES (1), (2), (3), (4), (5), (6), (7), (8);

DECLARE @SeriesPeriods TABLE (
    series_id BIGINT NOT NULL,
    period_start DATETIME2(0) NOT NULL,
    period_end DATETIME2(0) NOT NULL,
    target_like_count INT NOT NULL,
    PRIMARY KEY (series_id, period_start, period_end)
);

INSERT INTO @SeriesPeriods (
    series_id,
    period_start,
    period_end,
    target_like_count
)
SELECT
    target.series_id,
    DATEADD(
        DAY,
        -target.days_per_period * periods.period_number,
        target.anchor_end
    ),
    DATEADD(
        DAY,
        -target.days_per_period * (periods.period_number - 1),
        target.anchor_end
    ),
    CONVERT(
        INT,
        80
        + ABS(CONVERT(BIGINT, CHECKSUM(CONCAT(target.series_id, '-period-', periods.period_number)))) % 71
        + (9 - periods.period_number)
          * (
              CONVERT(
                  INT,
                  ABS(CONVERT(BIGINT, CHECKSUM(CONCAT(target.series_id, '-trend')))) % 9
              ) - 4
          )
    )
FROM @TargetSeries AS target
CROSS JOIN @PeriodNumbers AS periods;

DECLARE @SeriesChapters TABLE (
    series_id BIGINT NOT NULL,
    chapter_id BIGINT NOT NULL,
    chapter_ordinal INT NOT NULL,
    chapter_count INT NOT NULL,
    PRIMARY KEY (series_id, chapter_id)
);

INSERT INTO @SeriesChapters (
    series_id,
    chapter_id,
    chapter_ordinal,
    chapter_count
)
SELECT
    target.series_id,
    chapter.chapter_id,
    CONVERT(
        INT,
        ROW_NUMBER() OVER (
            PARTITION BY target.series_id
            ORDER BY chapter.chapter_number, chapter.chapter_id
        )
    ),
    CONVERT(
        INT,
        COUNT(*) OVER (PARTITION BY target.series_id)
    )
FROM @TargetSeries AS target
JOIN chapters AS chapter ON chapter.series_id = target.series_id;

DECLARE @MaxTargetLikes INT = (
    SELECT MAX(target_like_count)
    FROM @SeriesPeriods
);

DECLARE @DemoLikes TABLE (
    session_token VARCHAR(255) PRIMARY KEY,
    chapter_id BIGINT NOT NULL,
    liked_at DATETIME2(0) NOT NULL
);

;WITH Numbers AS (
    SELECT 1 AS number
    UNION ALL
    SELECT number + 1
    FROM Numbers
    WHERE number < @MaxTargetLikes
)
INSERT INTO @DemoLikes (session_token, chapter_id, liked_at)
SELECT
    CONCAT(
        @SessionPrefix,
        period.series_id,
        '-',
        target.days_per_period,
        '-',
        CONVERT(CHAR(8), period.period_start, 112),
        '-',
        RIGHT(CONCAT('0000', numbers.number), 4)
    ),
    chapter.chapter_id,
    DATEADD(
        SECOND,
        1 + (
            ABS(
                CONVERT(
                    BIGINT,
                    CHECKSUM(
                        CONCAT(
                            period.series_id,
                            '-',
                            CONVERT(CHAR(8), period.period_start, 112),
                            '-',
                            numbers.number
                        )
                    )
                )
            )
            % (DATEDIFF(SECOND, period.period_start, period.period_end) - 2)
        ),
        period.period_start
    )
FROM @SeriesPeriods AS period
JOIN @TargetSeries AS target ON target.series_id = period.series_id
JOIN Numbers AS numbers ON numbers.number <= period.target_like_count
JOIN @SeriesChapters AS chapter
  ON chapter.series_id = period.series_id
 AND chapter.chapter_ordinal = ((numbers.number - 1) % chapter.chapter_count) + 1
OPTION (MAXRECURSION 0);

/*
 * Remove demo rows that no longer belong to the current eight-period window.
 * Operational rows use a different source note/session prefix and are untouched.
 */
DELETE ranking
FROM series_rankings AS ranking
JOIN reader_feedback_imports AS feedback
  ON feedback.series_id = ranking.series_id
 AND feedback.period_start = ranking.period_start
 AND feedback.period_end = ranking.period_end
 AND feedback.imported_at = ranking.calculated_at
WHERE feedback.source_note = @SourceNote
  AND NOT EXISTS (
      SELECT 1
      FROM @SeriesPeriods AS current_period
      WHERE current_period.series_id = feedback.series_id
        AND current_period.period_start = feedback.period_start
        AND current_period.period_end = feedback.period_end
  );

DELETE feedback
FROM reader_feedback_imports AS feedback
WHERE feedback.source_note = @SourceNote
  AND NOT EXISTS (
      SELECT 1
      FROM @SeriesPeriods AS current_period
      WHERE current_period.series_id = feedback.series_id
        AND current_period.period_start = feedback.period_start
        AND current_period.period_end = feedback.period_end
  );

;WITH DuplicateFeedback AS (
    SELECT
        feedback.import_id,
        ROW_NUMBER() OVER (
            PARTITION BY feedback.series_id, feedback.period_start, feedback.period_end
            ORDER BY feedback.import_id
        ) AS duplicate_number
    FROM reader_feedback_imports AS feedback
    WHERE feedback.source_note = @SourceNote
)
DELETE FROM DuplicateFeedback
WHERE duplicate_number > 1;

;WITH DuplicateRankings AS (
    SELECT
        ranking.ranking_id,
        ROW_NUMBER() OVER (
            PARTITION BY ranking.series_id, ranking.period_start, ranking.period_end
            ORDER BY ranking.ranking_id
        ) AS duplicate_number
    FROM series_rankings AS ranking
    WHERE EXISTS (
        SELECT 1
        FROM reader_feedback_imports AS feedback
        WHERE feedback.source_note = @SourceNote
          AND feedback.series_id = ranking.series_id
          AND feedback.period_start = ranking.period_start
          AND feedback.period_end = ranking.period_end
          AND feedback.imported_at = ranking.calculated_at
    )
)
DELETE FROM DuplicateRankings
WHERE duplicate_number > 1;

DELETE seeded_like
FROM chapter_like_logs AS seeded_like
JOIN guest_access_logs AS guest ON guest.log_id = seeded_like.log_id
WHERE guest.session_token LIKE @SessionPrefix + '%'
  AND NOT EXISTS (
      SELECT 1
      FROM @DemoLikes AS current_like
      WHERE current_like.session_token = guest.session_token
  );

DELETE guest
FROM guest_access_logs AS guest
WHERE guest.session_token LIKE @SessionPrefix + '%'
  AND NOT EXISTS (
      SELECT 1
      FROM @DemoLikes AS current_like
      WHERE current_like.session_token = guest.session_token
  )
  AND NOT EXISTS (
      SELECT 1
      FROM chapter_like_logs AS remaining_like
      WHERE remaining_like.log_id = guest.log_id
  );

INSERT INTO guest_access_logs (
    session_token,
    ip_address,
    user_agent,
    created_at,
    last_active_at
)
SELECT
    demo.session_token,
    '127.0.0.1',
    N'Dữ liệu mẫu demo Dashboard',
    demo.liked_at,
    demo.liked_at
FROM @DemoLikes AS demo
WHERE NOT EXISTS (
    SELECT 1
    FROM guest_access_logs AS guest
    WHERE guest.session_token = demo.session_token
);

INSERT INTO chapter_like_logs (
    log_id,
    chapter_id,
    liked_at
)
SELECT
    guest.log_id,
    demo.chapter_id,
    demo.liked_at
FROM @DemoLikes AS demo
JOIN guest_access_logs AS guest ON guest.session_token = demo.session_token
WHERE NOT EXISTS (
    SELECT 1
    FROM chapter_like_logs AS existing_like
    WHERE existing_like.log_id = guest.log_id
      AND existing_like.chapter_id = demo.chapter_id
);

DECLARE @PeriodResults TABLE (
    series_id BIGINT NOT NULL,
    title NVARCHAR(255) NOT NULL,
    period_start DATETIME2(0) NOT NULL,
    period_end DATETIME2(0) NOT NULL,
    vote_count INT NOT NULL,
    calculated_at DATETIME2(0) NOT NULL,
    PRIMARY KEY (series_id, period_start, period_end)
);

INSERT INTO @PeriodResults (
    series_id,
    title,
    period_start,
    period_end,
    vote_count,
    calculated_at
)
SELECT
    period.series_id,
    series.title,
    period.period_start,
    period.period_end,
    CONVERT(INT, COUNT(chapter_like.like_id)),
    DATEADD(SECOND, -1, period.period_end)
FROM @SeriesPeriods AS period
JOIN manga_series AS series ON series.series_id = period.series_id
JOIN chapters AS chapter ON chapter.series_id = period.series_id
LEFT JOIN chapter_like_logs AS chapter_like
  ON chapter_like.chapter_id = chapter.chapter_id
 AND chapter_like.liked_at >= period.period_start
 AND chapter_like.liked_at < period.period_end
GROUP BY
    period.series_id,
    series.title,
    period.period_start,
    period.period_end;

UPDATE feedback
SET
    feedback.imported_by = @ImportedBy,
    feedback.vote_count = result.vote_count,
    feedback.avg_score = CONVERT(REAL, result.vote_count),
    feedback.imported_at = result.calculated_at
FROM reader_feedback_imports AS feedback
JOIN @PeriodResults AS result
  ON result.series_id = feedback.series_id
 AND result.period_start = feedback.period_start
 AND result.period_end = feedback.period_end
WHERE feedback.source_note = @SourceNote;

INSERT INTO reader_feedback_imports (
    series_id,
    imported_by,
    period_start,
    period_end,
    vote_count,
    avg_score,
    source_note,
    imported_at
)
SELECT
    result.series_id,
    @ImportedBy,
    result.period_start,
    result.period_end,
    result.vote_count,
    CONVERT(REAL, result.vote_count),
    @SourceNote,
    result.calculated_at
FROM @PeriodResults AS result
WHERE NOT EXISTS (
    SELECT 1
    FROM reader_feedback_imports AS feedback
    WHERE feedback.series_id = result.series_id
      AND feedback.period_start = result.period_start
      AND feedback.period_end = result.period_end
);

DECLARE @RankedResults TABLE (
    series_id BIGINT NOT NULL,
    period_start DATETIME2(0) NOT NULL,
    period_end DATETIME2(0) NOT NULL,
    vote_count INT NOT NULL,
    ranking_position INT NOT NULL,
    calculated_at DATETIME2(0) NOT NULL,
    PRIMARY KEY (series_id, period_start, period_end)
);

INSERT INTO @RankedResults (
    series_id,
    period_start,
    period_end,
    vote_count,
    ranking_position,
    calculated_at
)
SELECT
    result.series_id,
    result.period_start,
    result.period_end,
    result.vote_count,
    CONVERT(
        INT,
        ROW_NUMBER() OVER (
            PARTITION BY result.period_start, result.period_end
            ORDER BY result.vote_count DESC, result.title ASC
        )
    ),
    result.calculated_at
FROM @PeriodResults AS result;

UPDATE ranking
SET
    ranking.ranking_position = result.ranking_position,
    ranking.score = CONVERT(REAL, result.vote_count),
    ranking.vote_count = result.vote_count
FROM series_rankings AS ranking
JOIN @RankedResults AS result
  ON result.series_id = ranking.series_id
 AND result.period_start = ranking.period_start
 AND result.period_end = ranking.period_end
 AND result.calculated_at = ranking.calculated_at
WHERE EXISTS (
    SELECT 1
    FROM reader_feedback_imports AS feedback
    WHERE feedback.series_id = result.series_id
      AND feedback.period_start = result.period_start
      AND feedback.period_end = result.period_end
      AND feedback.source_note = @SourceNote
);

INSERT INTO series_rankings (
    series_id,
    ranking_position,
    score,
    calculated_at,
    period_start,
    period_end,
    vote_count
)
SELECT
    result.series_id,
    result.ranking_position,
    CONVERT(REAL, result.vote_count),
    result.calculated_at,
    result.period_start,
    result.period_end,
    result.vote_count
FROM @RankedResults AS result
WHERE EXISTS (
    SELECT 1
    FROM reader_feedback_imports AS feedback
    WHERE feedback.series_id = result.series_id
      AND feedback.period_start = result.period_start
      AND feedback.period_end = result.period_end
      AND feedback.source_note = @SourceNote
)
  AND NOT EXISTS (
    SELECT 1
    FROM series_rankings AS ranking
    WHERE ranking.series_id = result.series_id
      AND ranking.period_start = result.period_start
      AND ranking.period_end = result.period_end
);

COMMIT TRANSACTION;

SELECT
    feedback.period_start,
    feedback.period_end,
    COUNT(*) AS series_count,
    SUM(feedback.vote_count) AS total_votes
FROM reader_feedback_imports AS feedback
WHERE feedback.source_note = @SourceNote
GROUP BY feedback.period_start, feedback.period_end
ORDER BY feedback.period_start DESC, feedback.period_end DESC;
