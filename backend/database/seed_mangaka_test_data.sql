SET XACT_ABORT ON;
SET LOCK_TIMEOUT 10000;
BEGIN TRANSACTION;

ALTER TABLE manga_series ALTER COLUMN title NVARCHAR(255) NULL;
ALTER TABLE manga_series ALTER COLUMN genre NVARCHAR(255) NULL;
ALTER TABLE manga_series ALTER COLUMN description NVARCHAR(MAX) NULL;
ALTER TABLE chapters ALTER COLUMN title NVARCHAR(255) NULL;

DECLARE @MangakaRoleId BIGINT;

SELECT TOP (1) @MangakaRoleId = role_id
FROM roles
WHERE role_name = 'MANGAKA';

IF @MangakaRoleId IS NULL
BEGIN
    INSERT INTO roles (role_name) VALUES ('MANGAKA');
    SET @MangakaRoleId = SCOPE_IDENTITY();
END;

DECLARE @PasswordHash VARCHAR(255) = '$2a$10$0PF.OJUXjBjqgFaKUfF9lOcO7nOdWjOlmuYW4WmvLCl/zS7AEcI8e';

DECLARE @Mangaka TABLE (
    username NVARCHAR(255),
    email VARCHAR(255)
);

INSERT INTO @Mangaka (username, email)
VALUES
    (N'Eiichiro Oda', 'oda@manga.test'),
    (N'Gege Akutami', 'gege@manga.test'),
    (N'Koyoharu Gotouge', 'gotouge@manga.test'),
    (N'Gosho Aoyama', 'aoyama@manga.test');

MERGE users AS target
USING @Mangaka AS source
ON target.email = source.email
WHEN MATCHED THEN
    UPDATE SET
        target.username = source.username,
        target.password = @PasswordHash,
        target.status = 'ACTIVE',
        target.role_id = @MangakaRoleId
WHEN NOT MATCHED THEN
    INSERT (username, email, password, status, created_at, role_id)
    VALUES (source.username, source.email, @PasswordHash, 'ACTIVE', SYSDATETIME(), @MangakaRoleId);

DECLARE @Series TABLE (
    title NVARCHAR(255),
    author_email VARCHAR(255),
    genre NVARCHAR(255),
    cover_image VARCHAR(255),
    description NVARCHAR(MAX),
    status VARCHAR(50)
);

INSERT INTO @Series (title, author_email, genre, cover_image, description, status)
VALUES
    (N'Vua Hải Tặc', 'oda@manga.test', N'Hành động, Phiêu lưu, Hài hước',
     'https://vov2.vov.vn/sites/default/files/images/vuahaitac.jpg',
     N'Cuộc hành trình vĩ đại của Monkey D. Luffy và băng Mũ Rơm tiến vào Đại Trình Tuyến để tìm kiếm kho báu huyền thoại One Piece và trở thành Vua Hải Tặc.',
     'Published'),
    (N'Chú Thuật Hồi Chiến', 'gege@manga.test', N'Hành động, Siêu nhiên, Kinh dị',
     'https://i.redd.it/dk4x6yay4grg1.jpeg',
     N'Để cứu bạn bè khỏi Lời nguyền, nam sinh trung học Itadori Yuji đã nuốt ngón tay của Vua Lời Nguyền Ryomen Sukuna và bước chân vào thế giới của các Chú thuật sư.',
     'Reviewing'),
    (N'Thanh Gươm Diệt Quỷ', 'gotouge@manga.test', N'Hành động, Lịch sử, Siêu nhiên',
     'https://placehold.co/200x280/55efc4/ffffff?text=Demon+Slayer',
     N'Bi kịch giáng xuống gia đình Kamado Tanjirou khi cả nhà bị quỷ tàn sát, chỉ còn em gái Nezuko sống sót nhưng lại biến thành quỷ. Cậu quyết tâm gia nhập Sát Quỷ Đội để cứu em mình.',
     'Draft'),
    (N'Thám Tử Lừng Danh Conan', 'aoyama@manga.test', N'Trinh thám, Học đường, Hài hước',
     'https://placehold.co/200x280/ffeaa7/000000?text=Conan',
     N'Kudo Shinichi, một thám tử trung học tài ba bị Tổ chức Áo Đen ép uống thuốc độc khiến cơ thể teo nhỏ. Cậu sống dưới thân phận Edogawa Conan để truy lùng tổ chức này.',
     'Published');

-- Remove only malformed seed rows created before the Unicode column migration.
DELETE series
FROM manga_series AS series
JOIN users ON users.user_id = series.author_id
WHERE users.email LIKE '%@manga.test'
  AND NOT EXISTS (
      SELECT 1
      FROM @Series AS expected
      WHERE expected.author_email = users.email
        AND expected.title = series.title
  )
  AND NOT EXISTS (
      SELECT 1
      FROM chapters
      WHERE chapters.series_id = series.series_id
  );

MERGE manga_series AS target
USING (
    SELECT source.*, users.user_id AS author_id
    FROM @Series AS source
    JOIN users ON users.email = source.author_email
) AS source
ON target.title = source.title AND target.author_id = source.author_id
WHEN MATCHED THEN
    UPDATE SET
        target.genre = source.genre,
        target.cover_image = source.cover_image,
        target.description = source.description,
        target.status = source.status
WHEN NOT MATCHED THEN
    INSERT (title, author_id, genre, cover_image, description, status, created_at)
    VALUES (source.title, source.author_id, source.genre, source.cover_image,
            source.description, source.status, SYSDATETIME());

DECLARE @Chapters TABLE (
    series_title NVARCHAR(255),
    author_email VARCHAR(255),
    chapter_number INT,
    title NVARCHAR(255)
);

INSERT INTO @Chapters (series_title, author_email, chapter_number, title)
VALUES
    (N'Vua Hải Tặc', 'oda@manga.test', 1, N'Chương 1: Bình minh của cuộc phiêu lưu'),
    (N'Vua Hải Tặc', 'oda@manga.test', 2, N'Chương 2: Chàng trai đội mũ rơm'),
    (N'Chú Thuật Hồi Chiến', 'gege@manga.test', 1, N'Chương 1: Ryomen Sukuna'),
    (N'Chú Thuật Hồi Chiến', 'gege@manga.test', 2, N'Chương 2: Án tử hình ngầm'),
    (N'Thanh Gươm Diệt Quỷ', 'gotouge@manga.test', 1, N'Chương 1: Sự tàn khốc'),
    (N'Thanh Gươm Diệt Quỷ', 'gotouge@manga.test', 2, N'Chương 2: Người lạ mặt'),
    (N'Thám Tử Lừng Danh Conan', 'aoyama@manga.test', 1, N'Chương 1: Thám tử Heisei'),
    (N'Thám Tử Lừng Danh Conan', 'aoyama@manga.test', 2, N'Chương 2: Thám tử bị teo nhỏ');

MERGE chapters AS target
USING (
    SELECT source.chapter_number, source.title, series.series_id
    FROM @Chapters AS source
    JOIN users ON users.email = source.author_email
    JOIN manga_series AS series
        ON series.title = source.series_title
        AND series.author_id = users.user_id
) AS source
ON target.series_id = source.series_id
   AND target.chapter_number = source.chapter_number
WHEN MATCHED THEN
    UPDATE SET target.title = source.title
WHEN NOT MATCHED THEN
    INSERT (series_id, chapter_number, title, status, created_at)
    VALUES (source.series_id, source.chapter_number, source.title, 'PUBLISHED', SYSDATETIME());

COMMIT TRANSACTION;

SELECT username, email, status
FROM users
WHERE email IN ('oda@manga.test', 'gege@manga.test', 'gotouge@manga.test', 'aoyama@manga.test')
ORDER BY email;
