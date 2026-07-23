# Software Requirements Specification (SRS)

## 1. Giới thiệu

### 1.1 Mục đích

Tài liệu này mô tả yêu cầu phần mềm cho web app **Manga Manager**, một hệ thống quản lý quy trình sáng tác, xét duyệt, xuất bản và xếp hạng truyện manga. SRS dùng làm cơ sở thống nhất giữa nhóm phát triển, người dùng nghiệp vụ, giảng viên/đơn vị đánh giá và đội kiểm thử.

### 1.2 Phạm vi sản phẩm

Manga Manager hỗ trợ các nhóm người dùng chính:

- Khách/độc giả xem danh sách truyện, đọc thông tin series/chapter đã xuất bản, gửi lượt thích và tạo dữ liệu bình chọn.
- Mangaka tạo series, chapter, upload trang truyện, vẽ/chỉnh sửa trang, quản lý assistant, giao task và gửi bản thảo cho editor.
- Assistant nhận task, thao tác trên canvas, lưu revision và nộp sản phẩm.
- Tantou Editor kiểm duyệt series/chapter, tạo comment/revision note, quản lý lịch xuất bản và gửi hồ sơ lên Editorial Board.
- Editorial Board xét duyệt series, quản lý người dùng, phân công editor, nhập dữ liệu phản hồi độc giả, xem ranking và ra quyết định xuất bản/hủy/yêu cầu sửa.

### 1.3 Định nghĩa và viết tắt

| Thuật ngữ | Ý nghĩa |
| --- | --- |
| SRS | Software Requirements Specification |
| Mangaka | Tác giả/nhóm sáng tác manga |
| Tantou Editor | Biên tập viên phụ trách series/chapter |
| Editorial Board | Hội đồng biên tập có quyền xét duyệt, quản lý user và quyết định xuất bản |
| Assistant | Trợ lý hỗ trợ vẽ, hiệu ứng, chữ, nền hoặc công việc khác |
| Series | Bộ truyện manga |
| Chapter | Chương thuộc một series |
| Page | Trang truyện thuộc một chapter |
| Task | Công việc được Mangaka giao cho Assistant trên một vùng của page |
| Submission | Bài nộp của Assistant cho task |
| Dossier | Hồ sơ series để Tantou Editor/Editorial Board xét duyệt |
| JWT | JSON Web Token dùng cho xác thực |

### 1.4 Tài liệu tham chiếu

- Mã nguồn frontend React trong `frontend/src`.
- Mã nguồn backend Spring Boot trong `backend/src/main/java/com/example/backend`.
- Cấu hình database và upload trong `backend/src/main/resources/application.properties`.
- Script dữ liệu mẫu trong `backend/database`.

## 2. Mô tả tổng quan

### 2.1 Bối cảnh sản phẩm

Manga Manager là web app full-stack chạy cục bộ với:

- Frontend: React, React Router, Bootstrap, Fabric canvas, Google OAuth client.
- Backend: Spring Boot, Spring Security, JWT, JPA/Hibernate, Microsoft SQL Server.
- Database: SQL Server database tên `mangadb`.
- File storage: thư mục local cho cover, page image, revision note và series file.

### 2.2 Chức năng chính

- Trang công khai: home, ranking, chi tiết manga/chapter đã published, guest tracking và like chapter.
- Xác thực: đăng ký, đăng nhập bằng email/password, đăng nhập Google, phát JWT.
- Mangaka workspace: dashboard, quản lý manga, tạo series/chapter/page, upload file, vẽ canvas, quản lý assistant, giao task, duyệt submission, xem ranking/thông báo.
- Assistant workspace: dashboard, danh sách task, chi tiết task, canvas drawing, revision history, nộp submission, thông báo.
- Tantou Editor workspace: dashboard, review series/chapter, comment trên page, revision note bằng ảnh/canvas data, lịch xuất bản, tiến độ studio, thông báo.
- Editorial Board workspace: review hồ sơ series, vote approve/reject/cancel, phân công Tantou Editor, quản lý user, lịch xuất bản, reader votes, feedback import, ranking, thông báo.

### 2.3 Lớp người dùng

| Actor | Mô tả | Quyền chính |
| --- | --- | --- |
| Guest/Reader | Người dùng chưa đăng nhập | Xem series/chapter published, like chapter, tạo guest access log |
| Mangaka | Tác giả manga | Tạo và quản lý series/chapter/page/task/assistant, gửi duyệt |
| Assistant | Trợ lý tác giả | Nhận task, vẽ, lưu revision, nộp kết quả |
| Tantou Editor | Biên tập viên | Duyệt series/chapter được phân công, comment, revision note, lịch |
| Editorial Board | Hội đồng | Duyệt series, vote, phân công editor, quản lý user, ranking |

### 2.4 Môi trường vận hành

- Client chạy trên trình duyệt hiện đại.
- Frontend mặc định gọi API tại `http://localhost:8080`.
- Backend chạy Spring Boot Java 17.
- SQL Server chạy tại `localhost:1433`, database `mangadb`.
- File upload được lưu tại các thư mục cấu hình:
  - `./uploads/pages`
  - `./uploads/covers`
  - `./uploads/chapter-revision-notes`
  - `./uploads/series-files`

### 2.5 Ràng buộc thiết kế

- Phân quyền phải dựa trên role trong JWT và method-level security.
- Các endpoint private chỉ được truy cập khi có token hợp lệ.
- CORS đang cho phép mọi origin để hỗ trợ dev/local.
- File ảnh page/cover/revision note phải được kiểm tra định dạng và kích thước.
- Các route private frontend phải dùng `PrivateRoute` theo role.

## 3. Yêu cầu chức năng

### 3.1 Nhóm Public/Guest

| Mã | Yêu cầu |
| --- | --- |
| FR-PUB-01 | Hệ thống cho phép khách truy cập trang chủ và xem danh sách manga series đã hiển thị công khai. |
| FR-PUB-02 | Hệ thống cho phép khách xem chi tiết một manga series qua API `GET /manga-series/{id}`. |
| FR-PUB-03 | Hệ thống chỉ hiển thị chapter có trạng thái published cho luồng đọc công khai. |
| FR-PUB-04 | Hệ thống ghi nhận phiên khách qua `POST /guest-access` hoặc `POST /api/guest/access`. |
| FR-PUB-05 | Hệ thống cho phép khách like chapter qua `POST /chapters/{chapterId}/likes`. |
| FR-PUB-06 | Hệ thống chống ghi like trùng dựa trên guest log/session token theo logic backend. |
| FR-PUB-07 | Hệ thống cung cấp trang ranking công khai để độc giả xem truyện nổi bật. |

### 3.2 Xác thực và tài khoản

| Mã | Yêu cầu |
| --- | --- |
| FR-AUTH-01 | Hệ thống cho phép đăng ký tài khoản qua `POST /auth/register`. |
| FR-AUTH-02 | Hệ thống cho phép đăng nhập email/password qua `POST /auth/login`. |
| FR-AUTH-03 | Hệ thống cho phép đăng nhập bằng Google ID token qua `POST /auth/google-login`. |
| FR-AUTH-04 | Sau đăng nhập thành công, hệ thống trả về JWT và thông tin người dùng. |
| FR-AUTH-05 | Hệ thống mã hóa password bằng BCrypt. |
| FR-AUTH-06 | Hệ thống từ chối đăng nhập với tài khoản `INACTIVE`, `SUSPENDED` hoặc `DELETED`. |
| FR-AUTH-07 | Frontend phải lưu token/user vào local storage và tự xóa token khi API trả về 401. |

### 3.3 Mangaka workspace

| Mã | Yêu cầu |
| --- | --- |
| FR-MGK-01 | Mangaka có thể tạo series bằng JSON hoặc multipart form data. |
| FR-MGK-02 | Series phải có title và ít nhất một genre. |
| FR-MGK-03 | Mangaka có thể cung cấp cover URL hoặc upload cover image. |
| FR-MGK-04 | Cover image chỉ chấp nhận `.jpg`, `.jpeg`, `.png`, `.webp`, `.gif` và tối đa 5 MB. |
| FR-MGK-05 | Mangaka có thể xem danh sách series của mình qua `/mangaka/series` hoặc `/mangaka/my-series`. |
| FR-MGK-06 | Mangaka có thể upload file hồ sơ series khi submit, tối đa 20 file, tổng tối đa 200 MB. |
| FR-MGK-07 | File hồ sơ series hỗ trợ image, pdf, txt, md, doc, docx và zip; file thường tối đa 20 MB, zip tối đa 100 MB. |
| FR-MGK-08 | Mangaka có thể tạo chapter cho series của mình với chapter number dương và title bắt buộc. |
| FR-MGK-09 | Mangaka có thể tạo page bằng image URL hoặc upload nhiều ảnh page cho chapter. |
| FR-MGK-10 | Page image chỉ chấp nhận `.jpg`, `.jpeg`, `.png`, `.webp`, `.gif` và tối đa 5 MB mỗi ảnh. |
| FR-MGK-11 | Mangaka có thể xem chapter, danh sách page và revision note của chapter. |
| FR-MGK-12 | Mangaka có thể gửi chapter cho Tantou Editor kèm `manuscriptUrl`. |
| FR-MGK-13 | Mangaka có thể tạo assistant mới với username, email, password tối thiểu 8 ký tự và avatar URL tùy chọn. |
| FR-MGK-14 | Mangaka có thể xem danh sách assistant khả dụng và assistant tham gia từng series. |
| FR-MGK-15 | Mangaka có thể đổi trạng thái assistant trong tập `ACTIVE`, `INACTIVE`. |
| FR-MGK-16 | Mangaka có thể xóa mềm assistant bằng trạng thái `DELETED`. |
| FR-MGK-17 | Mangaka có thể giao task cho assistant trên một vùng page với task type `BACKGROUND`, `TEXT`, `EFFECTS`, `OTHER`. |
| FR-MGK-18 | Task phải có due date ở tương lai, file gốc, vùng chọn hợp lệ và assistant hợp lệ. |
| FR-MGK-19 | Mangaka có thể xem task và submission theo chapter. |
| FR-MGK-20 | Mangaka có thể review submission với quyết định `APPROVED` hoặc `REVISION_REQUESTED`. |
| FR-MGK-21 | Mangaka có thể xem ranking và notification của mình. |
| FR-MGK-22 | Mangaka có thể đánh dấu notification là đã đọc. |

### 3.4 Drawing/canvas cho Mangaka

| Mã | Yêu cầu |
| --- | --- |
| FR-DRW-01 | Mangaka có thể mở công cụ vẽ cho page thuộc chapter của mình. |
| FR-DRW-02 | Hệ thống lưu canvas data và preview image cho page drawing. |
| FR-DRW-03 | Khi lưu bản nháp, drawing có trạng thái `DRAFT`. |
| FR-DRW-04 | Khi finalize, drawing có trạng thái `FINALIZED` và page status chuyển thành `DRAWING_FINALIZED`. |
| FR-DRW-05 | Hệ thống lưu revision theo version và cho phép restore revision. |
| FR-DRW-06 | Hệ thống không cho user không sở hữu page thao tác drawing của page đó. |

### 3.5 Assistant workspace

| Mã | Yêu cầu |
| --- | --- |
| FR-AST-01 | Assistant có thể xem danh sách task được giao. |
| FR-AST-02 | Assistant có thể xem chi tiết task và các source file liên quan. |
| FR-AST-03 | Assistant có thể accept task, làm trạng thái chuyển sang `IN_PROGRESS`. |
| FR-AST-04 | Assistant có thể mở, lưu, finalize và restore drawing cho task được giao. |
| FR-AST-05 | Assistant có thể xem revision history của drawing. |
| FR-AST-06 | Assistant có thể nộp submission cho task với artifact URL/file URL và ghi chú. |
| FR-AST-07 | Khi submission được nộp, task chuyển sang `SUBMITTED`. |
| FR-AST-08 | Assistant chỉ được sửa task có trạng thái `ASSIGNED`, `IN_PROGRESS` hoặc `REVISION_REQUESTED`. |
| FR-AST-09 | Assistant không được accept task đã `SUBMITTED` hoặc `APPROVED`. |
| FR-AST-10 | Assistant có thể xem và đánh dấu notification là đã đọc. |

### 3.6 Tantou Editor workspace

| Mã | Yêu cầu |
| --- | --- |
| FR-TED-01 | Tantou Editor có thể xem series được phân công. |
| FR-TED-02 | Tantou Editor có thể xem series đang chờ editorial review. |
| FR-TED-03 | Tantou Editor có thể xem danh sách chapter đang chờ review. |
| FR-TED-04 | Tantou Editor có thể xem manuscript gồm series, chapters, progress và schedules. |
| FR-TED-05 | Tantou Editor có thể xem dossier của series. |
| FR-TED-06 | Tantou Editor có thể accept series ở trạng thái `PENDING_EDITOR`, chuyển sang `TANTOU_REVIEW`. |
| FR-TED-07 | Tantou Editor có thể reject series được phân công ở trạng thái `PENDING_EDITOR`. |
| FR-TED-08 | Tantou Editor có thể request revision cho series đang `TANTOU_REVIEW`, chuyển series sang `REVISION_REQUESTED`. |
| FR-TED-09 | Tantou Editor có thể submit series lên board, chuyển series sang `REVIEWING`. |
| FR-TED-10 | Tantou Editor có thể review chapter ở trạng thái `SUBMITTED_TO_EDITOR`. |
| FR-TED-11 | Tantou Editor có thể request revision chapter, chuyển chapter sang `REVISION_REQUESTED`. |
| FR-TED-12 | Tantou Editor có thể approve chapter, chuyển chapter sang `APPROVED`. |
| FR-TED-13 | Tantou Editor có thể publish chapter, chuyển chapter sang `PUBLISHED`. |
| FR-TED-14 | Tantou Editor có thể tạo revision note cho chapter bằng ảnh và canvas data. |
| FR-TED-15 | Revision note image chỉ chấp nhận image/jpeg, image/png, image/webp và tối đa 5 MB. |
| FR-TED-16 | Tantou Editor có thể comment trên page với type `CONTENT`, `DIALOGUE`, `SCRIPT`, `OTHER`. |
| FR-TED-17 | Comment có trạng thái `OPEN` hoặc `RESOLVED`. |
| FR-TED-18 | Tantou Editor có thể tạo, sửa, resolve và xóa comment. |
| FR-TED-19 | Tantou Editor có thể quản lý schedule theo series, publish date, frequency, status. |
| FR-TED-20 | Tantou Editor có thể xem tiến độ series/studio gồm chapter, page, task, comment, deadline và completion rate. |
| FR-TED-21 | Tantou Editor có thể xem và đánh dấu notification là đã đọc. |

### 3.7 Editorial Board workspace

| Mã | Yêu cầu |
| --- | --- |
| FR-BRD-01 | Editorial Board có thể xem registration request của user mới. |
| FR-BRD-02 | Editorial Board có thể approve hoặc reject registration request. |
| FR-BRD-03 | Editorial Board có thể xem series đang `REVIEWING`. |
| FR-BRD-04 | Editorial Board có thể xem series cần phân công editor với trạng thái `EDITOR_ASSIGNMENT_REQUIRED`. |
| FR-BRD-05 | Editorial Board có thể xem chi tiết review của series và danh sách quyết định. |
| FR-BRD-06 | Editorial Board member được phân công có thể vote `APPROVE` hoặc `REJECT`. |
| FR-BRD-07 | Khi đủ điều kiện phê duyệt, series có thể chuyển sang `Published`. |
| FR-BRD-08 | Khi bị yêu cầu sửa, series có thể chuyển sang `REVISION_REQUESTED`. |
| FR-BRD-09 | Editorial Board có thể cancel series bằng quyết định `CANCEL`, chuyển series sang `CANCELLED`. |
| FR-BRD-10 | Editorial Board có thể phân công Tantou Editor active cho series. |
| FR-BRD-11 | Sau khi phân công editor thành công, series chuyển sang `PENDING_EDITOR`. |
| FR-BRD-12 | Editorial Board có thể quản lý user với role `MANGAKA`, `ASSISTANT`, `TANTOU_EDITOR`, `EDITORIAL_BOARD`. |
| FR-BRD-13 | Editorial Board có thể tạo, cập nhật, xóa mềm user. |
| FR-BRD-14 | User status hợp lệ gồm `ACTIVE`, `INACTIVE`, `SUSPENDED`, `DELETED`. |
| FR-BRD-15 | Editorial Board có thể xem và quản lý publish schedule. |
| FR-BRD-16 | Editorial Board có thể xem reader votes theo khoảng thời gian. |
| FR-BRD-17 | Editorial Board có thể xem summary vote theo series. |
| FR-BRD-18 | Editorial Board có thể import reader feedback theo period, period start và period end. |
| FR-BRD-19 | Editorial Board có thể xem ranking, ranking periods và tổng vote theo series. |
| FR-BRD-20 | Editorial Board có thể xem và đánh dấu notification là đã đọc. |

### 3.8 Notification

| Mã | Yêu cầu |
| --- | --- |
| FR-NOT-01 | Hệ thống tạo notification khi có sự kiện quan trọng như task submitted, revision requested, chapter approved/published, series submitted to board. |
| FR-NOT-02 | Notification phải có type, referenceId, message, trạng thái đọc và thời điểm tạo. |
| FR-NOT-03 | Người dùng chỉ xem notification thuộc về mình hoặc thuộc role được phép. |

## 4. Quy trình nghiệp vụ

### 4.1 Quy trình tạo và duyệt series

1. Mangaka tạo series ở trạng thái ban đầu.
2. Mangaka bổ sung metadata, cover và file hồ sơ.
3. Mangaka submit series.
4. Hệ thống chọn hoặc yêu cầu phân công Tantou Editor.
5. Nếu cần phân công, Editorial Board chọn Tantou Editor active.
6. Series chuyển sang `PENDING_EDITOR`.
7. Tantou Editor accept series, series chuyển sang `TANTOU_REVIEW`.
8. Tantou Editor có thể request revision hoặc submit to board.
9. Khi submit to board, series chuyển sang `REVIEWING`.
10. Editorial Board vote approve/reject/cancel.
11. Kết quả cuối có thể là `Published`, `REVISION_REQUESTED`, `CANCELLED` hoặc quay lại bước xử lý editor.

### 4.2 Quy trình chapter

1. Mangaka tạo chapter cho series.
2. Mangaka tạo/upload page.
3. Mangaka hoặc Assistant vẽ/chỉnh sửa page.
4. Mangaka gửi chapter cho editor kèm manuscript URL, chapter chuyển `SUBMITTED_TO_EDITOR`.
5. Tantou Editor review chapter.
6. Tantou Editor có thể request revision, approve hoặc publish.
7. Chapter published được hiển thị cho độc giả.

### 4.3 Quy trình task assistant

1. Mangaka chọn page và vùng làm việc.
2. Mangaka tạo task với assistant, task type, deadline và file gốc.
3. Assistant accept task, task chuyển `IN_PROGRESS`.
4. Assistant chỉnh sửa canvas, lưu draft/finalize và nộp submission.
5. Task chuyển `SUBMITTED`.
6. Mangaka review submission.
7. Nếu approved, task hoàn tất. Nếu revision requested, assistant sửa lại và nộp lại.

### 4.4 Quy trình ranking và reader feedback

1. Khách truy cập được ghi guest access log.
2. Khách like chapter published.
3. Hệ thống lưu chapter like log gắn với guest/session.
4. Editorial Board xem votes theo khoảng thời gian.
5. Editorial Board import feedback theo period.
6. Ranking được tính/lưu theo period, position, score và vote count.

## 5. Yêu cầu dữ liệu

### 5.1 Thực thể chính

| Entity | Thuộc tính chính | Quan hệ |
| --- | --- | --- |
| User | id, username, email, password, avatarUrl, status, role, createdBy, createdAt | Nhiều user thuộc một role |
| Role | id, roleName | Một role có nhiều user |
| RegistrationRequest | username, email, password, reason, status, reviewer, reviewedAt | Được board approve/reject |
| MangaSeries | title, author, tantouEditor, genres, coverUrl, description, status, submittedAt, rankingScore | Có nhiều chapter, file, decision |
| Chapter | series, chapterNumber, title, manuscriptUrl, status, createdAt | Có nhiều page, task |
| ChapterPage | chapter, pageNumber, imageUrl, pageStatus | Có drawing, comment, task |
| PageDrawing | page, task, owner, canvasData, previewImageUrl, version, status | Có nhiều revision |
| PageDrawingRevision | drawing, createdBy, version, canvasData, previewImageUrl, status | Thuộc drawing |
| Task | title, description, taskType, page, assistant, chapter, dueDate, area, status | Có nhiều submission |
| Submission | task, chapter, submittedBy, artifactUrl, originalFileUrl, note, status, reviewNote | Được Mangaka review |
| ReviewComment | page, editor, commentText, commentType, status, position | Thuộc page |
| BoardDecision | series, boardMember, decisionType, reason, decisionDate | Thuộc series |
| PublishSchedule | series, publishDate, frequency, status | Thuộc series |
| Notification | user, type, referenceId, message, isRead, createdAt | Thuộc user |
| ChapterLikeLog | chapter, guestAccessLog/session, likedAt | Dùng cho votes |
| GuestAccessLog | sessionToken, userAgent, createdAt, lastAccessedAt | Gắn like |
| SeriesRanking | series, position, score, voteCount, period, calculatedAt | Dùng ranking |
| SeriesFile | series, uploadedBy, fileName, originalFileName, fileUrl, contentType, fileSize, fileType | Hồ sơ series |

### 5.2 Trạng thái chính

| Đối tượng | Trạng thái |
| --- | --- |
| User | `ACTIVE`, `INACTIVE`, `SUSPENDED`, `DELETED` |
| RegistrationRequest | `PENDING`, `APPROVED`, `REJECTED` |
| Series | `DRAFT`, `PENDING_EDITOR`, `TANTOU_REVIEW`, `REVIEWING`, `REVISION_REQUESTED`, `Published`, `EDITOR_ASSIGNMENT_REQUIRED`, `CANCELLED` |
| Chapter | `SUBMITTED_TO_EDITOR`, `REVISION_REQUESTED`, `APPROVED`, `PUBLISHED` và trạng thái draft/khởi tạo theo luồng tạo |
| Page | `DRAWING_FINALIZED`, `FINALIZED`, `PUBLISHED` và trạng thái page khác theo luồng tạo |
| Task | `ASSIGNED`, `IN_PROGRESS`, `SUBMITTED`, `APPROVED`, `REVISION_REQUESTED` |
| Drawing | `DRAFT`, `FINALIZED` |
| Comment | `OPEN`, `RESOLVED` |
| Schedule | mặc định `PLANNED`, có thể cấu hình theo request |
| BoardDecision | `APPROVE`, `REJECT`, `CANCEL` |

## 6. API yêu cầu

### 6.1 Public/Auth API

| Method | Endpoint | Mục đích |
| --- | --- | --- |
| POST | `/auth/register` | Đăng ký tài khoản |
| POST | `/auth/login` | Đăng nhập email/password |
| POST | `/auth/google-login` | Đăng nhập Google |
| GET | `/manga-series` | Danh sách series public |
| GET | `/manga-series/{id}` | Chi tiết series public |
| POST | `/guest-access` | Ghi nhận guest session |
| POST | `/api/guest/access` | Alias ghi nhận guest session |
| POST | `/chapters/{chapterId}/likes` | Like chapter |
| POST | `/registration-request` | Gửi yêu cầu đăng ký/được xét duyệt |

### 6.2 Mangaka API

| Method | Endpoint | Mục đích |
| --- | --- | --- |
| POST | `/mangaka/series` | Tạo series JSON/multipart |
| GET | `/mangaka/series` | Xem series của tôi |
| GET | `/mangaka/my-series` | Alias xem series của tôi |
| POST | `/mangaka/series/{seriesId}/submit` | Submit series kèm file |
| GET | `/mangaka/series/{seriesId}/files` | Xem file series |
| GET | `/mangaka/series/{seriesId}/assistants` | Xem assistant tham gia series |
| POST | `/mangaka/chapters` | Tạo chapter |
| GET | `/mangaka/series/{seriesId}/chapters` | Xem chapter theo series |
| GET | `/mangaka/chapters/{chapterId}` | Xem chi tiết chapter |
| POST | `/mangaka/pages` | Tạo page bằng URL |
| POST | `/mangaka/chapters/{chapterId}/pages` | Upload page images |
| GET | `/mangaka/chapters/{chapterId}/pages` | Xem pages của chapter |
| PATCH | `/mangaka/chapters/{chapterId}/submit-to-editor` | Gửi chapter cho editor |
| GET | `/mangaka/chapters/{chapterId}/revision-notes` | Xem revision notes |
| GET | `/mangaka/assistants` | Xem assistant khả dụng |
| POST | `/mangaka/assistants` | Tạo assistant |
| PATCH | `/mangaka/assistants/{assistantId}/status` | Đổi trạng thái assistant |
| DELETE | `/mangaka/assistants/{assistantId}` | Xóa mềm assistant |
| POST | `/mangaka/tasks` | Giao task |
| GET | `/mangaka/chapters/{chapterId}/tasks` | Xem task của chapter |
| GET | `/mangaka/chapters/{chapterId}/submissions` | Xem submission |
| PATCH | `/mangaka/submissions/{submissionId}/review` | Review submission |
| GET | `/mangaka/rankings` | Xem ranking |
| GET | `/mangaka/notifications` | Xem notification |
| PATCH | `/mangaka/notifications/{notificationId}/read` | Đánh dấu đã đọc |

### 6.3 Assistant API

| Method | Endpoint | Mục đích |
| --- | --- | --- |
| GET | `/assistant/tasks` | Task của assistant |
| GET | `/assistant/tasks/{taskId}` | Chi tiết task |
| PATCH | `/assistant/tasks/{taskId}/accept` | Nhận task |
| GET | `/assistant/tasks/{taskId}/drawing` | Lấy drawing task |
| PUT | `/assistant/tasks/{taskId}/drawing` | Lưu drawing |
| POST | `/assistant/tasks/{taskId}/drawing/finalize` | Finalize drawing |
| GET | `/assistant/tasks/{taskId}/drawing/revisions` | Xem revisions |
| POST | `/assistant/tasks/{taskId}/drawing/revisions/{revisionId}/restore` | Restore revision |
| GET | `/assistant/tasks/{taskId}/submissions` | Xem submissions |
| POST | `/assistant/tasks/{taskId}/submissions` | Nộp task |
| GET | `/assistant/notifications` | Xem notification |
| PATCH | `/assistant/notifications/{notificationId}/read` | Đánh dấu đã đọc |

### 6.4 Tantou Editor API

| Method | Endpoint | Mục đích |
| --- | --- | --- |
| GET | `/tantou-editor/series` | Series được phân công |
| GET | `/tantou-editor/series/pending-editorial-review` | Series chờ board review |
| GET | `/tantou-editor/chapters/pending-review` | Chapter chờ review |
| GET | `/tantou-editor/chapters/{chapterId}` | Chi tiết chapter |
| POST | `/tantou-editor/chapters/{chapterId}/revision-notes` | Tạo revision note |
| POST | `/tantou-editor/chapters/{chapterId}/request-revision` | Yêu cầu sửa chapter |
| POST | `/tantou-editor/chapters/{chapterId}/approve` | Approve chapter |
| POST | `/tantou-editor/chapters/{chapterId}/publish` | Publish chapter |
| GET | `/tantou-editor/series/{seriesId}/manuscript` | Xem manuscript |
| GET | `/tantou-editor/series/{seriesId}/dossier` | Xem dossier |
| PATCH | `/tantou-editor/series/{seriesId}/submit-to-board` | Gửi board review |
| PATCH | `/tantou-editor/series/{seriesId}/request-revision` | Yêu cầu sửa series |
| GET | `/tantou-editor/series/{seriesId}/progress` | Tiến độ series |
| GET | `/tantou-editor/studio/progress` | Tiến độ studio |
| GET | `/tantou-editor/pages/{pageId}/comments` | Xem comment page |
| POST | `/tantou-editor/pages/{pageId}/comments` | Tạo comment |
| PUT | `/tantou-editor/comments/{commentId}` | Sửa comment |
| PATCH | `/tantou-editor/comments/{commentId}/resolve` | Resolve comment |
| DELETE | `/tantou-editor/comments/{commentId}` | Xóa comment |
| GET | `/tantou-editor/schedules` | Xem schedule |
| POST | `/tantou-editor/schedules` | Tạo schedule |
| PUT | `/tantou-editor/schedules/{scheduleId}` | Sửa schedule |
| DELETE | `/tantou-editor/schedules/{scheduleId}` | Xóa schedule |
| POST | `/tantou-editor/series/{seriesId}/accept` | Accept series |
| POST | `/tantou-editor/series/{seriesId}/reject` | Reject series |
| GET | `/tantou-editor/notifications` | Xem notification |
| PATCH | `/tantou-editor/notifications/{notificationId}/read` | Đánh dấu đã đọc |

### 6.5 Editorial Board API

| Method | Endpoint | Mục đích |
| --- | --- | --- |
| GET | `/editorial-board/registration-requests` | Xem registration requests |
| PUT | `/editorial-board/registration-requests/{id}/approve` | Approve request |
| PUT | `/editorial-board/registration-requests/{id}/reject` | Reject request |
| GET | `/editorial-board/series/reviewing` | Series đang review |
| GET | `/editorial-board/series/editor-assignment-required` | Series cần phân công editor |
| GET | `/editorial-board/series/{id}/review` | Chi tiết review series |
| GET | `/editorial-board/series/{id}/decisions` | Decisions của series |
| POST | `/editorial-board/series/{id}/decisions` | Vote decision |
| PATCH | `/editorial-board/series/{id}/assign-editor` | Phân công editor |
| PUT | `/editorial-board/series/{id}/cancel` | Cancel series |
| GET | `/editorial-board/users` | Xem users |
| POST | `/editorial-board/users` | Tạo user |
| PUT | `/editorial-board/users/{id}` | Cập nhật user |
| DELETE | `/editorial-board/users/{id}` | Xóa mềm user |
| GET | `/editorial-board/publish-schedules` | Xem publish schedules |
| POST | `/editorial-board/publish-schedules` | Tạo schedule |
| PUT | `/editorial-board/publish-schedules/{scheduleId}` | Sửa schedule |
| DELETE | `/editorial-board/publish-schedules/{scheduleId}` | Xóa schedule |
| GET | `/editorial-board/reader-votes` | Xem reader votes theo thời gian |
| GET | `/editorial-board/reader-votes/summary` | Summary votes |
| POST | `/editorial-board/reader-feedback-imports` | Import feedback |
| GET | `/editorial-board/reader-feedback-imports` | Lịch sử import |
| GET | `/editorial-board/rankings` | Xem rankings |
| GET | `/editorial-board/rankings/periods` | Xem periods |
| GET | `/editorial-board/rankings/total-votes` | Tổng votes |
| GET | `/editorial-board/notifications` | Xem notification |
| PATCH | `/editorial-board/notifications/{id}/read` | Đánh dấu đã đọc |

## 7. Yêu cầu giao diện

### 7.1 Public UI

- Trang chủ phải hiển thị các manga series nổi bật/danh sách series.
- Trang ranking phải hiển thị thứ hạng, điểm/vote và thông tin series.
- Trang login phải hỗ trợ đăng nhập email/password và Google login.

### 7.2 Role-based UI

- Sau đăng nhập, frontend điều hướng người dùng tới workspace tương ứng với role.
- Các route `/mangaka`, `/assistant`, `/tantou`, `/board` phải được bảo vệ bằng role.
- Nếu user không có token hoặc role không hợp lệ, hệ thống điều hướng về login hoặc chặn truy cập.

### 7.3 Canvas UI

- Canvas phải cho phép thao tác drawing/markup.
- UI phải hỗ trợ lưu nháp, finalize, xem/restore revision.
- Với task assistant, canvas phải hiển thị hoặc bám theo vùng task được giao.

## 8. Yêu cầu phi chức năng

### 8.1 Bảo mật

| Mã | Yêu cầu |
| --- | --- |
| NFR-SEC-01 | API private phải yêu cầu JWT hợp lệ. |
| NFR-SEC-02 | Backend phải kiểm tra role bằng Spring Security và `@PreAuthorize`. |
| NFR-SEC-03 | Password phải được hash bằng BCrypt, không lưu plaintext. |
| NFR-SEC-04 | Token Google phải được xác minh bằng Google client id cấu hình. |
| NFR-SEC-05 | User không được truy cập tài nguyên không thuộc quyền sở hữu/phân công. |
| NFR-SEC-06 | File upload phải được kiểm tra extension, content type, size và path an toàn. |
| NFR-SEC-07 | Tài khoản `DELETED` không được đăng nhập và được xem như không tồn tại. |

### 8.2 Hiệu năng

| Mã | Yêu cầu |
| --- | --- |
| NFR-PER-01 | Các danh sách dashboard/ranking/notification phải phản hồi trong thời gian phù hợp cho dữ liệu đồ án/local. |
| NFR-PER-02 | Upload file phải tôn trọng giới hạn 100 MB mỗi file zip và 200 MB mỗi request series submission. |
| NFR-PER-03 | Các truy vấn ranking/vote nên lọc theo period/time range để tránh tải dữ liệu không cần thiết. |

### 8.3 Độ tin cậy

| Mã | Yêu cầu |
| --- | --- |
| NFR-REL-01 | Các thao tác thay đổi trạng thái nghiệp vụ phải chạy trong transaction. |
| NFR-REL-02 | Lỗi nghiệp vụ phải trả HTTP status phù hợp như 400, 401, 403, 404, 409, 413. |
| NFR-REL-03 | Khi lưu drawing hoặc upload file thất bại, hệ thống không được tạo dữ liệu mồ côi gây sai quy trình. |

### 8.4 Khả dụng và bảo trì

| Mã | Yêu cầu |
| --- | --- |
| NFR-MNT-01 | Backend expose Swagger/OpenAPI tại `/swagger-ui/**` và `/v3/api-docs/**`. |
| NFR-MNT-02 | Code frontend chia theo feature folder cho Mangaka, Assistant, Tantou Editor và Editorial Board. |
| NFR-MNT-03 | Constants role phải dùng thống nhất giữa route guard và logic đăng nhập. |
| NFR-MNT-04 | Các script database migration/seed phải lưu trong `backend/database`. |

### 8.5 Tính tương thích

| Mã | Yêu cầu |
| --- | --- |
| NFR-CMP-01 | Frontend hỗ trợ các browser hiện đại theo cấu hình React app. |
| NFR-CMP-02 | Backend chạy trên Java 17. |
| NFR-CMP-03 | Database chính là Microsoft SQL Server. |

## 9. Yêu cầu kiểm thử và tiêu chí nghiệm thu

### 9.1 Kiểm thử chức năng

| Mã | Tiêu chí nghiệm thu |
| --- | --- |
| AC-01 | Guest mở trang chủ/ranking và xem được series/chapter published mà không cần đăng nhập. |
| AC-02 | Guest like chapter thành công một lần với cùng session token và không tạo like trùng. |
| AC-03 | User đăng nhập thành công nhận JWT và được điều hướng tới workspace đúng role. |
| AC-04 | User không có role phù hợp không truy cập được route/API private của role khác. |
| AC-05 | Mangaka tạo series, upload cover hợp lệ, tạo chapter/page và xem lại dữ liệu vừa tạo. |
| AC-06 | Mangaka upload file không hợp lệ hoặc vượt kích thước bị từ chối với lỗi rõ ràng. |
| AC-07 | Mangaka tạo assistant, giao task hợp lệ và task xuất hiện ở workspace Assistant. |
| AC-08 | Assistant accept task, lưu drawing, finalize, restore revision và submit task thành công. |
| AC-09 | Mangaka approve submission làm task/submission chuyển trạng thái phù hợp. |
| AC-10 | Mangaka request revision làm assistant có thể tiếp tục sửa và nộp lại. |
| AC-11 | Mangaka submit chapter to editor, Tantou Editor thấy chapter trong danh sách pending review. |
| AC-12 | Tantou Editor tạo comment/revision note, request revision, approve hoặc publish chapter thành công. |
| AC-13 | Tantou Editor submit series to board, Editorial Board thấy series trong danh sách reviewing. |
| AC-14 | Editorial Board vote và phân công editor theo quyền, không cho user ngoài assignment vote trái phép. |
| AC-15 | Editorial Board quản lý user, registration request, schedule, reader vote và ranking thành công. |
| AC-16 | Notification được tạo cho các sự kiện chính và có thể đánh dấu đã đọc. |

### 9.2 Kiểm thử bảo mật

- Kiểm thử API private khi không có token.
- Kiểm thử API private với token sai role.
- Kiểm thử user thao tác tài nguyên không thuộc quyền sở hữu.
- Kiểm thử tài khoản inactive/deleted đăng nhập.
- Kiểm thử upload file giả mạo extension/content type.

### 9.3 Kiểm thử dữ liệu

- Kiểm thử unique email/username.
- Kiểm thử chapter number dương và due date tương lai.
- Kiểm thử tọa độ vùng task không âm, width/height dương.
- Kiểm thử period import feedback không rỗng và thời gian hợp lệ.

## 10. Ma trận traceability rút gọn

| Module | Frontend pages/components | Backend controller/service | Nhóm yêu cầu |
| --- | --- | --- | --- |
| Public | `HomePage`, `RankingPage`, `SeriesModal`, `SeriesCard` | `MangaSeriesController`, `GuestAccessController`, `LikeLogController` | FR-PUB |
| Auth | `LoginPage`, `LoginForm`, `AuthContext` | `AuthController`, `AuthService`, `JwtFilter` | FR-AUTH |
| Mangaka | `DashboardMangakaPage`, `MyMangaPage`, `CreateSeriesPage`, `CreateChapterPage`, `ChapterPagesPage`, `DrawingPage`, `ManageAssistantsPage` | `MangakaController`, `MangakaService`, `MangakaDrawingController`, `DrawingService` | FR-MGK, FR-DRW |
| Assistant | `DashboardAssistantPage`, `MyTasksPage`, `TaskDetailPage`, `MySubmissionsPage` | `AssistantController`, `AssistantService` | FR-AST |
| Tantou Editor | `TantouDashboardPage`, `EditorReviewPage`, `ChapterReviewPage`, `ScheduleManagementPage` | `TantouEditorController`, `TantouEditorService` | FR-TED |
| Editorial Board | `RankingDecisionPage`, `ReviewSeriesPage`, `ManageUsersPage`, `PublishSchedulePage`, `ReaderVotesPage` | `EditorialBoardController`, `EditorialBoardService` | FR-BRD |
| Notification | `NotificationBoard`, role notification pages | Role-specific notification endpoints | FR-NOT |

## 11. Giả định và giới hạn

- Hệ thống hiện được mô tả theo môi trường local/dev, chưa bao gồm triển khai production cloud.
- Thanh toán có entity `Payment`, nhưng chưa thấy API nghiệp vụ thanh toán hoàn chỉnh trong controller, nên không đưa vào phạm vi chức năng chính.
- AI processing có entity `AIProcessing`, nhưng chưa thấy workflow AI hoàn chỉnh trong controller, nên được xem là mở rộng tương lai.
- Phần ranking có dữ liệu và API xem/import vote, nhưng thuật toán tính điểm chi tiết phụ thuộc implementation service/database seed.
- CORS wildcard phù hợp dev/local, nhưng production cần giới hạn origin.
- JWT secret mặc định dev phải thay bằng biến môi trường khi triển khai thật.

## 12. Hướng phát triển tiếp theo

- Hoàn thiện đặc tả thuật toán ranking và rule chuyển trạng thái series/chapter bằng state diagram.
- Bổ sung ERD chính thức từ entity JPA.
- Thêm API contract chi tiết cho request/response từng endpoint.
- Bổ sung test case chi tiết theo từng AC.
- Chuẩn hóa trạng thái series `Published` thành cùng style với các trạng thái uppercase nếu muốn dễ maintain.
