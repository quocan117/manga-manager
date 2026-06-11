# UC01 – Tạo hồ sơ series mới

## Thông tin chung

| Mục | Nội dung |
|-----|----------|
| **Mã UC** | UC01 |
| **Tên UC** | Tạo hồ sơ series mới |
| **Phiên bản** | 1.0 |
| **Ngày tạo** | 11/06/2026 |
| **Tác nhân chính** | Mangaka (Họa sĩ truyện tranh) |
| **Mức độ ưu tiên** | Cao |
| **Tần suất sử dụng** | Thường xuyên |

## Mô tả tóm tắt

Mangaka tạo một hồ sơ series mới trên hệ thống Manga Manager bằng cách nhập thông tin cơ bản (tên, thể loại, tóm tắt), khai báo các nhân vật chính và tải lên bản thảo sơ bộ (draft) cho series đó.

## Tiền điều kiện

1. Mangaka đã đăng nhập thành công vào hệ thống.
2. Mangaka có quyền tạo series mới.
3. Hệ thống đang hoạt động bình thường.

## Hậu điều kiện

**Thành công:**
- Hồ sơ series mới được lưu vào cơ sở dữ liệu với trạng thái `DRAFT` (bản nháp).
- Thông tin nhân vật được gắn với series vừa tạo.
- Các file bản thảo sơ bộ được lưu trữ trên hệ thống và liên kết với series.
- Hệ thống hiển thị thông báo tạo series thành công.

**Thất bại:**
- Không có dữ liệu mới được lưu.
- Hệ thống hiển thị thông báo lỗi phù hợp.

## Luồng sự kiện chính

| Bước | Tác nhân | Hệ thống |
|------|---------|----------|
| 1 | Mangaka chọn chức năng **"Tạo series mới"** trên giao diện. | Hiển thị form tạo hồ sơ series. |
| 2 | Mangaka nhập **tên series** (bắt buộc). | Kiểm tra tên không được để trống. |
| 3 | Mangaka chọn/nhập **thể loại** (bắt buộc), ví dụ: Shonen, Seinen, Shoujo, Isekai… | Ghi nhận thể loại đã chọn. |
| 4 | Mangaka nhập **tóm tắt nội dung** (bắt buộc). | Ghi nhận tóm tắt. |
| 5 | Mangaka bấm **"Thêm nhân vật"** và nhập thông tin từng nhân vật: tên, mô tả/vai trò. | Thêm dòng nhân vật vào form; cho phép thêm nhiều nhân vật. |
| 6 | Mangaka chọn **file bản thảo sơ bộ** từ máy tính (hỗ trợ nhiều file: PNG, JPG, PDF…). | Hiển thị danh sách file đã chọn. |
| 7 | Mangaka bấm **"Tạo series"**. | Kiểm tra hợp lệ dữ liệu đầu vào. |
| 8 | | Lưu thông tin series, nhân vật và file bản thảo vào hệ thống. |
| 9 | | Hiển thị thông báo **"Tạo series thành công"** kèm thông tin series vừa tạo. |

## Luồng sự kiện thay thế

### 5a. Mangaka xóa một nhân vật đã thêm

| Bước | Tác nhân | Hệ thống |
|------|---------|----------|
| 5a.1 | Mangaka bấm **"Xóa"** trên dòng nhân vật cần bỏ. | Xóa dòng nhân vật khỏi form. |
| 5a.2 | | Quay lại bước 5 hoặc tiếp tục bước 6. |

### 6a. Mangaka không tải bản thảo (bỏ qua)

| Bước | Tác nhân | Hệ thống |
|------|---------|----------|
| 6a.1 | Mangaka bỏ qua bước tải file và bấm **"Tạo series"**. | Cho phép tạo series không kèm bản thảo; tiếp tục bước 7. |

### 7a. Mangaka hủy thao tác

| Bước | Tác nhân | Hệ thống |
|------|---------|----------|
| 7a.1 | Mangaka bấm **"Hủy"** hoặc quay lại trang trước. | Không lưu dữ liệu; quay về màn hình trước. |

## Luồng sự kiện ngoại lệ

### E1. Thiếu thông tin bắt buộc

| Bước | Tác nhân | Hệ thống |
|------|---------|----------|
| E1.1 | Mangaka bấm **"Tạo series"** khi chưa nhập đủ tên, thể loại hoặc tóm tắt. | Hiển thị thông báo lỗi tại các trường thiếu; không lưu dữ liệu. |
| E1.2 | Mangaka bổ sung thông tin và thử lại. | Quay lại bước 7. |

### E2. Tên series đã tồn tại

| Bước | Tác nhân | Hệ thống |
|------|---------|----------|
| E2.1 | Mangaka nhập tên series trùng với series đã có. | Hiển thị lỗi **"Tên series đã tồn tại"**; không lưu dữ liệu. |
| E2.2 | Mangaka đổi tên khác và thử lại. | Quay lại bước 7. |

### E3. File bản thảo không hợp lệ

| Bước | Tác nhân | Hệ thống |
|------|---------|----------|
| E3.1 | Mangaka tải file sai định dạng hoặc vượt dung lượng cho phép. | Hiển thị lỗi **"File không hợp lệ hoặc quá lớn"**; không lưu dữ liệu. |
| E3.2 | Mangaka chọn file hợp lệ và thử lại. | Quay lại bước 6. |

### E4. Lỗi hệ thống khi lưu

| Bước | Tác nhân | Hệ thống |
|------|---------|----------|
| E4.1 | Hệ thống gặp lỗi khi lưu dữ liệu hoặc upload file. | Hiển thị thông báo **"Không thể tạo series, vui lòng thử lại"**; ghi log lỗi. |

## Yêu cầu đặc biệt

1. Tên series: tối đa 200 ký tự, không trùng lặp trong hệ thống.
2. Tóm tắt: tối đa 2000 ký tự.
3. Mỗi series có thể có nhiều nhân vật; mỗi nhân vật cần có tên.
4. File bản thảo: định dạng cho phép `PNG`, `JPG`, `JPEG`, `PDF`; dung lượng tối đa **50 MB/file**.
5. Giao diện hỗ trợ tiếng Việt.
6. Thời gian phản hồi sau khi bấm **"Tạo series"** ≤ 5 giây (với file ≤ 10 MB).

## Quy tắc nghiệp vụ

| Mã BR | Mô tả |
|-------|-------|
| BR-01 | Series mới luôn được tạo với trạng thái `DRAFT`. |
| BR-02 | Phải có ít nhất một nhân vật trước khi tạo series (khuyến nghị, có thể cấu hình). |
| BR-03 | Mangaka chỉ được chỉnh sửa series do mình tạo. |

## Giao diện liên quan

- **UI-01**: Form tạo hồ sơ series mới (`CreateSeriesForm`)
- **UI-02**: Thông báo kết quả tạo series

## API liên quan

| Phương thức | Endpoint | Mô tả |
|------------|----------|-------|
| POST | `/api/series` | Tạo series mới (multipart: thông tin + file bản thảo) |
| GET | `/api/series` | Lấy danh sách series |
| GET | `/api/series/{id}` | Xem chi tiết series |

## Dữ liệu mẫu (demo)

```json
{
  "name": "Hành Trình Về Phía Bình Minh",
  "genre": "Shonen",
  "summary": "Câu chuyện về một cậu bé mang ước mơ trở thành mangaka, vượt qua khó khăn để theo đuổi đam mê vẽ truyện.",
  "characters": [
    {
      "name": "Minato Kazuki",
      "description": "Nhân vật chính – học sinh cấp 3, đam mê vẽ manga."
    },
    {
      "name": "Sora Aizawa",
      "description": "Bạn thân và người hỗ trợ kịch bản cho Minato."
    }
  ]
}
```

## Liên kết triển khai code

| Thành phần | Đường dẫn |
|-----------|-----------|
| Backend API | `backend/src/main/java/com/example/backend/controller/SeriesController.java` |
| Entity | `backend/src/main/java/com/example/backend/model/` |
| Frontend form | `frontend/src/components/CreateSeriesForm.js` |
