# 2. Cài đặt và chạy app

## Yêu cầu máy

- **Android Studio** bản ổn định mới (khuyến nghị có SDK 36 và build-tools tương thích với `compileSdk` / `targetSdk` trong `app/build.gradle`).
- JDK 11 (trùng `compileOptions` trong project).
- Thiết bị thật hoặc **Android Emulator** (API ≥ `minSdk` 24).

## Lấy mã nguồn

```bash
git clone https://github.com/duybac210/NHOM5_49k14.1_Ung-dung-nhap-hang-cho-nha-thuoc-An-Khang.git
cd NHOM5_49k14.1_Ung-dung-nhap-hang-cho-nha-thuoc-An-Khang
```

(Nếu bạn đang dùng bản clone khác tên thư mục, chỉ cần mở thư mục gốc chứa `settings.gradle` trong Android Studio.)

## File Firebase bắt buộc: `google-services.json`

App dùng plugin Google Services. File cấu hình phải nằm đúng chỗ:

- Đường dẫn: `app/google-services.json`

Nếu clone repo mà **không** có file này (ví dụ file không được commit vì chính sách nhóm):

1. Vào [Firebase Console](https://console.firebase.google.com/) → project **pharmaimport-474c3** (hoặc project mới nếu nhóm tạo riêng).
2. Cài đặt dự án Android với **package name** `com.nhom5.pharma`.
3. Tải `google-services.json` và đặt vào thư mục `app/`.
4. **Sync Gradle** (File → Sync Project with Gradle Files).

**Lưu ý bảo mật:** File này chứa thông tin kết nối client. Với repo công khai, nên cấu hình **Firestore Security Rules** chặt chẽ và cân nhắc không lưu file nhạy cảm trên Git công khai (hoặc dùng giới hạn API key trong Google Cloud Console). Trưởng nhóm cần phổ biến quy tắc cho cả nhóm.

## Mở project và chạy

1. Android Studio → **Open** → chọn thư mục gốc project.
2. Chờ Gradle sync xong; nếu báo thiếu SDK thì làm theo hướng dẫn SDK Manager.
3. Chọn device/emulator → nút **Run** (▶).

**Màn Nhập hàng** cần **mạng** để đọc/ghi Firestore. Máy ảo phải bật internet.

## Kiểm tra nhanh sau khi chạy

- App mở `MainActivity`, tab mặc định là **Nhập hàng**.
- Nếu Firestore và rules cho phép đọc `import_orders`, danh sách hiển thị (có thể trống nếu chưa có document).
- Nút tạo dữ liệu mẫu (trong màn Nhập hàng) thêm một document vào `import_orders` nếu rules cho phép **ghi**.

## Lỗi thường gặp

| Hiện tượng | Hướng xử lý |
|------------|-------------|
| Gradle sync fail | Kiểm tra JDK, proxy, và phiên bản Android Studio; xem log “Build” |
| `google-services.json` missing | Thêm file vào `app/` như trên |
| Firestore permission denied | Kiểm tra **Security Rules** trên Firebase Console |
| RecyclerView trống dù có data | Kiểm tra field `createdAt`, index composite nếu query phức tạp (xem `04-firebase-va-firestore.md`) |
