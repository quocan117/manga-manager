# manga-manager

<h1>Dùng cho localhost DATABASE</h1>

Chuyển sang sử dụng SSMS<br>
// CREATE DATABASE mangadb<br>
Tên : mangadb

PS: Check phần backend/database rồi copy dán vào local ssms và chạy dể có data Mangaka test
<h4>Hướng dẫn</h4>
B1: Tạo database mangadb<br>
B2: <strong><em>Chạy project để tạo tables cho database</em></strong><br>
B3(Optional): chạy query trong backend/database (Chỉ dùng khi có cập nhật kiểu dữ liệu hoặc database bị cũ.)<br>
+ Chạy theo thứ tự này: <br>
1.seed_editorial_board_test_accounts.sql<br>
2. seed_mangaka_test_data.sql<br>
3. normalize_guest_sessions.sql<br>
4. seed_random_chapter_likes.sql<br>
5. ensure_large_image_columns<br>
6. alter_text_columns_to_nvarchar_max<br>
7. add_task_submission_original_file_urls<br>
8. add_chapter_editor_review_flow<br>
9. add_task_file_rounds_and_markup_pages<br>
10. drop_chapter_board_reviews<br>
11. add_series_publication_coordinator<br>
12. add_chapter_manuscript_files_and_revision_descriptions<br>
13. add_series_editor_rejection_reason<br>
14. replace_feedback_period_with_range<br>
<h5><strong>Lưu ý: số lượng like sẽ khác nhau ở mỗi máy vì dùng %*00 + *0 </strong></h5>

<h3>Tài khoản:</h3>

Mangaka(Hiện có đến 40 tài khoản mangaka khác nhau, dưới đây là một số tài khoản để test):<br>
oda@manga.test<br>
gege@manga.test<br>
gotouge@manga.test

Assistant:<br>
assistant1@manga.test

Editor:<br>
editor1@manga.test

Editorial_Board:<br>
editorial1@manga.test<br>
editorial2@manga.test<br>
editorial3@manga.test<br>
editorial4@manga.test<br>
editorial5@manga.test<br>
editorial6@manga.test<br>
editorial7@manga.test


<h3>Mật khẩu chung: Mangaka@123</h3>
