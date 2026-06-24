# manga-manager

<h1>Dùng cho localhost DATABASE</h1>

Chuyển sang sử dụng SSMS<br>
// CREATE DATABASE mangadb<br>
Tên : mangadb

PS: Check phần backend/database rồi copy dán vào local ssms và chạy dể có data Mangaka test
<h4>Hướng dẫn</h4>
B1: Tạo database mangadb<br>
B2: <strong><em>Chạy project để tạo tables cho database</em></strong><br>
B3: chạy query trong backend/database<br>
+ Chạy theo thứ tự này: <br>
1. seed_mangaka_test_data.sql<br>
2. normalize_guest_sessions.sql<br>
3. seed_random_chapter_likes.sql<br>
<h5><strong>Lưu ý: số lượng like sẽ khá nhau ở mỗi máy vì dùng %*00 + *0 </strong></h5>

<h3>Tài khoản:</h3>

Mangaka(Hiện có đến 40 tài khoản mangaka khác nhau, dưới đây là một số tài khoản để test):

oda@manga.test<br>
gege@manga.test<br>
gotouge@manga.test<br>
aoyama@manga.test

Editorial_Board:

editorial1@manga.test<br>
editorial2@manga.test<br>
editorial3@manga.test


<h3>Mật khẩu chung: Mangaka@123</h3>
