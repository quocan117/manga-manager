/*
 * Removes only data created by seed_dashboard_ranking_history.sql.
 * Existing series, chapters, users, schedules, likes and operational ranking
 * imports are not removed.
 */

SET XACT_ABORT ON;
SET NOCOUNT ON;
SET LOCK_TIMEOUT 10000;

BEGIN TRANSACTION;

DECLARE @SourceNote NVARCHAR(255) = N'Dữ liệu mẫu demo Dashboard';
DECLARE @SessionPrefix VARCHAR(64) = 'seed-dashboard-ranking-';
DECLARE @DeletedRankings INT = 0;
DECLARE @DeletedImports INT = 0;
DECLARE @DeletedLikes INT = 0;
DECLARE @DeletedGuests INT = 0;

IF OBJECT_ID('series_rankings', 'U') IS NOT NULL
   AND OBJECT_ID('reader_feedback_imports', 'U') IS NOT NULL
   AND COL_LENGTH('series_rankings', 'period_start') IS NOT NULL
   AND COL_LENGTH('series_rankings', 'period_end') IS NOT NULL
   AND COL_LENGTH('reader_feedback_imports', 'period_start') IS NOT NULL
   AND COL_LENGTH('reader_feedback_imports', 'period_end') IS NOT NULL
BEGIN
    DELETE ranking
    FROM series_rankings AS ranking
    WHERE EXISTS (
        SELECT 1
        FROM reader_feedback_imports AS feedback
        WHERE feedback.source_note = @SourceNote
          AND feedback.series_id = ranking.series_id
          AND feedback.period_start = ranking.period_start
          AND feedback.period_end = ranking.period_end
          AND feedback.imported_at = ranking.calculated_at
    );

    SET @DeletedRankings = @@ROWCOUNT;
END;

IF OBJECT_ID('reader_feedback_imports', 'U') IS NOT NULL
BEGIN
    DELETE FROM reader_feedback_imports
    WHERE source_note = @SourceNote;

    SET @DeletedImports = @@ROWCOUNT;
END;

IF OBJECT_ID('chapter_like_logs', 'U') IS NOT NULL
   AND OBJECT_ID('guest_access_logs', 'U') IS NOT NULL
BEGIN
    DELETE seeded_like
    FROM chapter_like_logs AS seeded_like
    JOIN guest_access_logs AS guest ON guest.log_id = seeded_like.log_id
    WHERE guest.session_token LIKE @SessionPrefix + '%';

    SET @DeletedLikes = @@ROWCOUNT;

    DELETE guest
    FROM guest_access_logs AS guest
    WHERE guest.session_token LIKE @SessionPrefix + '%'
      AND NOT EXISTS (
          SELECT 1
          FROM chapter_like_logs AS remaining_like
          WHERE remaining_like.log_id = guest.log_id
      );

    SET @DeletedGuests = @@ROWCOUNT;
END;

COMMIT TRANSACTION;

SELECT
    @DeletedRankings AS deleted_rankings,
    @DeletedImports AS deleted_feedback_imports,
    @DeletedLikes AS deleted_chapter_likes,
    @DeletedGuests AS deleted_guest_sessions;
