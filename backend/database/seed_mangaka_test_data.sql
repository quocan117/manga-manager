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
    (N'Gosho Aoyama', 'aoyama@manga.test'),
    (N'Masashi Kishimoto', 'kishimoto@manga.test'),
    (N'Akira Toriyama', 'toriyama@manga.test'),
    (N'Yoshihiro Togashi', 'togashi@manga.test'),
    (N'Hajime Isayama', 'isayama@manga.test'),
    (N'Tatsuki Fujimoto', 'fujimoto@manga.test'),
    (N'Naoki Urasawa', 'urasawa@manga.test'),
    (N'Tite Kubo', 'kubo@manga.test'),
    (N'Kohei Horikoshi', 'horikoshi@manga.test'),
    (N'Hiromu Arakawa', 'arakawa@manga.test'),
    (N'Tsugumi Ohba', 'ohba@manga.test'),
    (N'Hideaki Sorachi', 'sorachi@manga.test'),
    (N'Sui Ishida', 'ishida@manga.test'),
    (N'Haruichi Furudate', 'furudate@manga.test'),
    (N'Tadatoshi Fujimaki', 'fujimaki@manga.test'),
    (N'Hiro Mashima', 'mashima@manga.test'),
    (N'ONE', 'one@manga.test'),
    (N'Hirohiko Araki', 'araki@manga.test'),
    (N'Tatsuya Endo', 'endo@manga.test'),
    (N'Aka Akasaka', 'akasaka@manga.test'),
    (N'Kentaro Miura', 'miura@manga.test'),
    (N'Takehiko Inoue', 'inoue@manga.test'),
    (N'Makoto Yukimura', 'yukimura@manga.test'),
    (N'Yuki Tabata', 'tabata@manga.test'),
    (N'Riichiro Inagaki', 'inagaki@manga.test'),
    (N'Muneyuki Kaneshiro', 'kaneshiro@manga.test'),
    (N'Kaiu Shirai', 'shirai@manga.test'),
    (N'Yusei Matsui', 'matsui@manga.test'),
    (N'Atsushi Ohkubo', 'ohkubo@manga.test'),
    (N'Katsura Hoshino', 'hoshino@manga.test'),
    (N'Hiroya Oku', 'oku@manga.test'),
    (N'Rumiko Takahashi', 'takahashi@manga.test');

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
    (N'Vua Hải Tặc', 'oda@manga.test', N'Hành động, Phiêu lưu, Hài hước', 'Vua_Hai_Tac.jpg', N'Monkey D. Luffy cùng băng Mũ Rơm tiến vào Đại Hải Trình để tìm kho báu One Piece.', 'Published'),
    (N'Chú Thuật Hồi Chiến', 'gege@manga.test', N'Hành động, Siêu nhiên, Kinh dị', 'Jujutsu_Kaisen_vol_1_cover.jpeg', N'Itadori Yuji bước vào thế giới chú thuật sau khi nuốt ngón tay của Ryomen Sukuna.', 'Reviewing'),
    (N'Thanh Gươm Diệt Quỷ', 'gotouge@manga.test', N'Hành động, Lịch sử, Siêu nhiên', 'knr.jpg', N'Kamado Tanjirou gia nhập Sát Quỷ Đội để cứu em gái Nezuko và tiêu diệt loài quỷ.', 'Draft'),
    (N'Thám Tử Lừng Danh Conan', 'aoyama@manga.test', N'Trinh thám, Học đường, Hài hước', 'conan.jpg', N'Kudo Shinichi bị teo nhỏ và sống dưới thân phận Edogawa Conan để truy tìm Tổ chức Áo Đen.', 'Published'),
    (N'Naruto', 'kishimoto@manga.test', N'Hành động, Ninja, Phiêu lưu', 'naruto.jpg', N'Uzumaki Naruto theo đuổi ước mơ trở thành Hokage và được mọi người công nhận.', 'Published'),
    (N'Dragon Ball', 'toriyama@manga.test', N'Võ thuật, Phiêu lưu, Viễn tưởng', 'db.jpg', N'Son Goku phiêu lưu khắp thế giới, tìm ngọc rồng và bảo vệ Trái Đất.', 'Published'),
    (N'Hunter x Hunter', 'togashi@manga.test', N'Hành động, Phiêu lưu, Kỳ ảo', 'hxh.png', N'Gon Freecss trở thành Hunter để tìm người cha đã rời xa mình.', 'Draft'),
    (N'Attack on Titan', 'isayama@manga.test', N'Hành động, Bi kịch, Hậu tận thế', 'aot.jpg', N'Nhân loại sống sau những bức tường khổng lồ để chống lại các Titan.', 'Published'),
    (N'Chainsaw Man', 'fujimoto@manga.test', N'Hành động, Kinh dị, Siêu nhiên', 'csm.jpg', N'Denji hợp nhất với Pochita và trở thành thợ săn quỷ cưa máy.', 'Published'),
    (N'Monster', 'urasawa@manga.test', N'Trinh thám, Tâm lý, Giật gân', 'monster.jpg', N'Bác sĩ Tenma bị cuốn vào bí ẩn đen tối sau khi cứu sống Johan.', 'Published'),
    (N'Bleach', 'kubo@manga.test', N'Hành động, Siêu nhiên', 'bleach.jpg', N'Kurosaki Ichigo nhận sức mạnh tử thần và chiến đấu với Hollow.', 'Published'),
    (N'My Hero Academia', 'horikoshi@manga.test', N'Hành động, Siêu anh hùng', 'mha.jpg', N'Izuku Midoriya không có siêu năng lực nhưng vẫn mơ trở thành anh hùng.', 'Published'),
    (N'Fullmetal Alchemist', 'arakawa@manga.test', N'Phiêu lưu, Kỳ ảo, Khoa học viễn tưởng', 'fma.jpg', N'Hai anh em Elric tìm Hòn đá Triết gia để khôi phục cơ thể.', 'Published'),
    (N'Death Note', 'ohba@manga.test', N'Tâm lý, Siêu nhiên, Trinh thám', 'dn.jpg', N'Yagami Light nhặt được cuốn sổ tử thần có thể giết người bằng tên.', 'Published'),
    (N'Gintama', 'sorachi@manga.test', N'Hài hước, Hành động, Khoa học viễn tưởng', 'gintama.jpg', N'Sakata Gintoki sống qua những ngày kỳ quặc trong Edo bị người ngoài hành tinh chiếm đóng.', 'Published'),
    (N'Tokyo Ghoul', 'ishida@manga.test', N'Hành động, Kinh dị, Tâm lý', 'tg.jpg', N'Kaneki Ken trở thành bán ghoul và phải học cách sống trong thế giới ngạ quỷ.', 'Published'),
    (N'Haikyuu!!', 'furudate@manga.test', N'Thể thao, Học đường', 'haikyuu.jpg', N'Hinata Shoyo cùng đội bóng chuyền Karasuno vươn tới đỉnh cao.', 'Published'),
    (N'Kuroko no Basket', 'fujimaki@manga.test', N'Thể thao, Học đường', 'kuroko.jpg', N'Đội bóng rổ Seirin cùng Kuroko đối đầu Thế hệ Kỳ tích.', 'Published'),
    (N'Fairy Tail', 'mashima@manga.test', N'Kỳ ảo, Phiêu lưu, Hành động', 'ft.jpg', N'Lucy Heartfilia cùng Natsu Dragneel phiêu lưu trong hội pháp sư Fairy Tail.', 'Published'),
    (N'One Punch Man', 'one@manga.test', N'Hành động, Hài hước, Siêu anh hùng', 'opm.jpg', N'Saitama có thể hạ mọi kẻ địch bằng một cú đấm và tìm kiếm thử thách mới.', 'Published'),
    (N'JoJo Bizarre Adventure', 'araki@manga.test', N'Hành động, Phiêu lưu, Siêu nhiên', 'jojo.jpg', N'Dòng họ Joestar trải qua những trận chiến kỳ lạ qua nhiều thế hệ.', 'Published'),
    (N'Spy x Family', 'endo@manga.test', N'Hành động, Hài hước, Gia đình', 'spy.jpg', N'Một điệp viên, sát thủ và cô bé ngoại cảm tạo thành gia đình giả để làm nhiệm vụ.', 'Published'),
    (N'Kaguya-sama', 'akasaka@manga.test', N'Tình cảm, Hài hước, Học đường', 'kaguya.jpg', N'Hai thiên tài hội học sinh đấu trí để khiến đối phương tỏ tình trước.', 'Published'),
    (N'Berserk', 'miura@manga.test', N'Hành động, Kỳ ảo đen tối', 'berserk.jpg', N'Kiếm sĩ Đen Guts đi qua hành trình trả thù trong một thế giới tàn khốc.', 'Published'),
    (N'Vagabond', 'inoue@manga.test', N'Hành động, Lịch sử, Võ thuật', 'Vagabond.png', N'Câu chuyện về Miyamoto Musashi trên con đường trở thành kiếm khách vĩ đại.', 'Draft'),
    (N'Vinland Saga', 'yukimura@manga.test', N'Hành động, Lịch sử, Phiêu lưu', 'vinland.jpg', N'Thorfinn sống cùng băng chiến binh đã giết cha mình để chờ cơ hội báo thù.', 'Published'),
    (N'Black Clover', 'tabata@manga.test', N'Hành động, Kỳ ảo', 'bc.jpg', N'Asta sinh ra không có ma pháp nhưng quyết tâm trở thành Ma Pháp Vương.', 'Published'),
    (N'Dr. Stone', 'inagaki@manga.test', N'Khoa học viễn tưởng, Phiêu lưu', 'drstone.jpg', N'Senku dùng khoa học để tái thiết văn minh sau khi nhân loại bị hóa đá.', 'Published'),
    (N'Blue Lock', 'kaneshiro@manga.test', N'Thể thao, Tâm lý', 'blueblock.jpg', N'Dự án Blue Lock đào tạo tiền đạo ích kỷ nhất để đưa Nhật Bản vô địch.', 'Published'),
    (N'The Promised Neverland', 'shirai@manga.test', N'Khoa học viễn tưởng, Giật gân, Trinh thám', 'tpn.jpg', N'Những đứa trẻ ở Grace Field phát hiện bí mật kinh hoàng và lên kế hoạch trốn thoát.', 'Published'),
    (N'Assassination Classroom', 'matsui@manga.test', N'Hành động, Hài hước, Học đường', 'assassination.jpg', N'Lớp 3-E được giao nhiệm vụ ám sát giáo viên chủ nhiệm Koro-sensei.', 'Published'),
    (N'Mob Psycho 100', 'one@manga.test', N'Hành động, Hài hước, Siêu nhiên', 'mob.jpg', N'Shigeo Kageyama có sức mạnh tâm linh lớn nhưng chỉ muốn sống bình thường.', 'Published'),
    (N'Fire Force', 'ohkubo@manga.test', N'Hành động, Siêu nhiên, Khoa học viễn tưởng', 'fireforce.jpg', N'Shinra Kusakabe gia nhập đội cứu hỏa đặc biệt để điều tra hiện tượng tự bốc cháy.', 'Published'),
    (N'Soul Eater', 'ohkubo@manga.test', N'Hành động, Kỳ ảo', 'souleater.jpg', N'Học viện Shibusen đào tạo vũ khí và người sử dụng vũ khí chống lại linh hồn xấu.', 'Published'),
    (N'D.Gray-man', 'hoshino@manga.test', N'Hành động, Kỳ ảo đen tối', 'dgrayman.jpg', N'Allen Walker gia nhập Giáo hội Đen để tiêu diệt các Akuma.', 'Draft'),
    (N'Gantz', 'oku@manga.test', N'Hành động, Khoa học viễn tưởng, Kinh dị', 'gantz.jpg', N'Những người vừa chết bị buộc tham gia nhiệm vụ tiêu diệt người ngoài hành tinh.', 'Published'),
    (N'Inuyasha', 'takahashi@manga.test', N'Phiêu lưu, Lãng mạn, Kỳ ảo', 'Inuyasha.jpg', N'Kagome xuyên không về thời Chiến Quốc và cùng Inuyasha thu thập mảnh ngọc.', 'Published'),
    (N'Yu Yu Hakusho', 'togashi@manga.test', N'Hành động, Siêu nhiên', 'yuyu.jpg', N'Urameshi Yusuke được hồi sinh để trở thành thám tử Linh giới.', 'Published'),
    (N'Slam Dunk', 'inoue@manga.test', N'Thể thao, Học đường', 'slamdunk.jpg', N'Sakuragi Hanamichi gia nhập đội bóng rổ Shohoku và tìm thấy đam mê mới.', 'Published'),
    (N'20th Century Boys', 'urasawa@manga.test', N'Khoa học viễn tưởng, Trinh thám, Tâm lý', '20cb.jpg', N'Một nhóm bạn phải đối mặt với giáo phái nguy hiểm gắn với ký ức tuổi thơ.', 'Published');

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
    (N'Thám Tử Lừng Danh Conan', 'aoyama@manga.test', 2, N'Chương 2: Thám tử bị teo nhỏ'),
    (N'Naruto', 'kishimoto@manga.test', 1, N'Chương 1: Uzumaki Naruto!!'),
    (N'Naruto', 'kishimoto@manga.test', 2, N'Chương 2: Konohamaru!!'),
    (N'Dragon Ball', 'toriyama@manga.test', 1, N'Chương 1: Bulma và Son Goku'),
    (N'Dragon Ball', 'toriyama@manga.test', 2, N'Chương 2: Quả cầu biến mất'),
    (N'Hunter x Hunter', 'togashi@manga.test', 1, N'Chương 1: Lên đường'),
    (N'Hunter x Hunter', 'togashi@manga.test', 2, N'Chương 2: Bài thi bắt đầu'),
    (N'Attack on Titan', 'isayama@manga.test', 1, N'Chương 1: Gửi em của 2000 năm sau'),
    (N'Attack on Titan', 'isayama@manga.test', 2, N'Chương 2: Ngày tàn'),
    (N'Chainsaw Man', 'fujimoto@manga.test', 1, N'Chương 1: Chó và Cưa'),
    (N'Chainsaw Man', 'fujimoto@manga.test', 2, N'Chương 2: Địa điểm công việc mới'),
    (N'Monster', 'urasawa@manga.test', 1, N'Chương 1: Bác sĩ Tenma'),
    (N'Monster', 'urasawa@manga.test', 2, N'Chương 2: Cậu bé bí ẩn'),
    (N'Bleach', 'kubo@manga.test', 1, N'Chương 1: Kẻ thay thế Tử thần'),
    (N'Bleach', 'kubo@manga.test', 2, N'Chương 2: Khởi đầu công việc'),
    (N'My Hero Academia', 'horikoshi@manga.test', 1, N'Chương 1: Izuku Midoriya'),
    (N'My Hero Academia', 'horikoshi@manga.test', 2, N'Chương 2: Tiếng rống thét'),
    (N'Fullmetal Alchemist', 'arakawa@manga.test', 1, N'Chương 1: Hai anh em'),
    (N'Fullmetal Alchemist', 'arakawa@manga.test', 2, N'Chương 2: Linh hồn kim loại'),
    (N'Death Note', 'ohba@manga.test', 1, N'Chương 1: Sự nhàm chán'),
    (N'Death Note', 'ohba@manga.test', 2, N'Chương 2: L'),
    (N'Gintama', 'sorachi@manga.test', 1, N'Chương 1: Người tóc xoăn tốt bụng'),
    (N'Gintama', 'sorachi@manga.test', 2, N'Chương 2: Chủ nhân của chiếc xích lô'),
    (N'Tokyo Ghoul', 'ishida@manga.test', 1, N'Chương 1: Bi kịch'),
    (N'Tokyo Ghoul', 'ishida@manga.test', 2, N'Chương 2: Điều bất thường'),
    (N'Haikyuu!!', 'furudate@manga.test', 1, N'Chương 1: Kẻ kết thúc và kẻ khởi đầu'),
    (N'Haikyuu!!', 'furudate@manga.test', 2, N'Chương 2: Đội bóng Karasuno'),
    (N'Kuroko no Basket', 'fujimaki@manga.test', 1, N'Chương 1: Kuroko là ai?'),
    (N'Kuroko no Basket', 'fujimaki@manga.test', 2, N'Chương 2: Tớ là cái bóng'),
    (N'Fairy Tail', 'mashima@manga.test', 1, N'Chương 1: Đuôi tiên'),
    (N'Fairy Tail', 'mashima@manga.test', 2, N'Chương 2: Kẻ mạo danh'),
    (N'One Punch Man', 'one@manga.test', 1, N'Chương 1: Một đấm'),
    (N'One Punch Man', 'one@manga.test', 2, N'Chương 2: Genos'),
    (N'JoJo Bizarre Adventure', 'araki@manga.test', 1, N'Chương 1: DIO the Invader'),
    (N'JoJo Bizarre Adventure', 'araki@manga.test', 2, N'Chương 2: Chuyến tàu tới tăm tối'),
    (N'Spy x Family', 'endo@manga.test', 1, N'Chương 1: Nhiệm vụ Strix'),
    (N'Spy x Family', 'endo@manga.test', 2, N'Chương 2: Tìm mẹ'),
    (N'Kaguya-sama', 'akasaka@manga.test', 1, N'Chương 1: Cuộc chiến học viện'),
    (N'Kaguya-sama', 'akasaka@manga.test', 2, N'Chương 2: Ai là người tỏ tình?'),
    (N'Berserk', 'miura@manga.test', 1, N'Chương 1: Kiếm sĩ Đen'),
    (N'Berserk', 'miura@manga.test', 2, N'Chương 2: Thương ấn'),
    (N'Vagabond', 'inoue@manga.test', 1, N'Chương 1: Takezo'),
    (N'Vagabond', 'inoue@manga.test', 2, N'Chương 2: Làng Miyamoto'),
    (N'Vinland Saga', 'yukimura@manga.test', 1, N'Chương 1: Normanni'),
    (N'Vinland Saga', 'yukimura@manga.test', 2, N'Chương 2: Lời hứa'),
    (N'Black Clover', 'tabata@manga.test', 1, N'Chương 1: Cậu bé không ma pháp'),
    (N'Black Clover', 'tabata@manga.test', 2, N'Chương 2: Kì thi Ma pháp thư'),
    (N'Dr. Stone', 'inagaki@manga.test', 1, N'Chương 1: Thế giới đá'),
    (N'Dr. Stone', 'inagaki@manga.test', 2, N'Chương 2: Vua khoa học'),
    (N'Blue Lock', 'kaneshiro@manga.test', 1, N'Chương 1: Ước mơ rực cháy'),
    (N'Blue Lock', 'kaneshiro@manga.test', 2, N'Chương 2: Tầng hầm ngục'),
    (N'The Promised Neverland', 'shirai@manga.test', 1, N'Chương 1: Grace Field'),
    (N'The Promised Neverland', 'shirai@manga.test', 2, N'Chương 2: Sự thật'),
    (N'Assassination Classroom', 'matsui@manga.test', 1, N'Chương 1: Lớp học ám sát'),
    (N'Assassination Classroom', 'matsui@manga.test', 2, N'Chương 2: Koro-sensei'),
    (N'Mob Psycho 100', 'one@manga.test', 1, N'Chương 1: Siêu năng lực gia'),
    (N'Mob Psycho 100', 'one@manga.test', 2, N'Chương 2: 100%'),
    (N'Fire Force', 'ohkubo@manga.test', 1, N'Chương 1: Ác quỷ'),
    (N'Fire Force', 'ohkubo@manga.test', 2, N'Chương 2: Đội cứu hỏa số 8'),
    (N'Soul Eater', 'ohkubo@manga.test', 1, N'Chương 1: Sự cộng hưởng linh hồn'),
    (N'Soul Eater', 'ohkubo@manga.test', 2, N'Chương 2: Death the Kid'),
    (N'D.Gray-man', 'hoshino@manga.test', 1, N'Chương 1: Allen Walker'),
    (N'D.Gray-man', 'hoshino@manga.test', 2, N'Chương 2: Giáo hội Đen'),
    (N'Gantz', 'oku@manga.test', 1, N'Chương 1: Căn phòng chết chóc'),
    (N'Gantz', 'oku@manga.test', 2, N'Chương 2: Nhiệm vụ đầu tiên'),
    (N'Inuyasha', 'takahashi@manga.test', 1, N'Chương 1: Thiếu nữ xuyên không'),
    (N'Inuyasha', 'takahashi@manga.test', 2, N'Chương 2: Bán yêu phong ấn'),
    (N'Yu Yu Hakusho', 'togashi@manga.test', 1, N'Chương 1: Cái chết của Yusuke'),
    (N'Yu Yu Hakusho', 'togashi@manga.test', 2, N'Chương 2: Linh giới'),
    (N'Slam Dunk', 'inoue@manga.test', 1, N'Chương 1: Kẻ thất tình Hanamichi'),
    (N'Slam Dunk', 'inoue@manga.test', 2, N'Chương 2: Cậu có thích bóng rổ không?'),
    (N'20th Century Boys', 'urasawa@manga.test', 1, N'Chương 1: Lời tiên tri'),
    (N'20th Century Boys', 'urasawa@manga.test', 2, N'Chương 2: Bạn bè');

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

SELECT users.username, users.email, users.status, COUNT(series.series_id) AS series_count
FROM users
LEFT JOIN manga_series AS series
    ON series.author_id = users.user_id
WHERE users.email LIKE '%@manga.test'
GROUP BY users.username, users.email, users.status
ORDER BY users.email;
