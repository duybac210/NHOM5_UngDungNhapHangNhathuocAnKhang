# 4. Firebase và Firestore

## Project

- **Project ID (từ cấu hình app):** `pharmaimport-474c3`  
- **Android package:** `com.nhom5.pharma`

Truy cập dữ liệu và rules: [Firebase Console – Firestore](https://console.firebase.google.com/project/pharmaimport-474c3/firestore).

## Thư viện trong app

Định nghĩa trong `app/build.gradle`:

- `firebase-bom` (phiên bản cố định trong project)
- `firebase-analytics`
- `firebase-firestore`
- `firebase-ui-firestore` (adapter realtime cho `RecyclerView`)

Plugin root: `com.google.gms.google-services`.

## Collection đang dùng: `import_orders`

Module **Nhập hàng** đọc/ghi collection này.

### Query mặc định

- `collection("import_orders").orderBy("createdAt", DESCENDING)`  
  Hiển thị đơn mới trước.

### Tìm kiếm (theo code hiện tại)

- Khi ô tìm kiếm **có chữ:**  
  `orderBy("tenNhaCungCap")` + `startAt` / `endAt` + hậu tố `\uf8ff` (prefix search kiểu Firestore).
- Khi **xoá hết** ô tìm kiếm: quay lại query theo `createdAt` DESC.

**Lưu ý:** Đổi kiểu query có thể yêu cầu **composite index** trong Firestore. Nếu log báo link tạo index, mở link đó trong Console để tạo.

### Ghi dữ liệu mẫu (`createSampleData`)

Document mới gồm các field (kiểu gần đúng với model Java):

| Field | Kiểu / ý nghĩa |
|------|-----------------|
| `tenNhaCungCap` | String |
| `tongTien` | Number (double) |
| `trangThai` | Number (int): `1` = đã nhập, khác = chờ xử lý (theo `NhapHangAdapter`) |
| `createdAt` | `FieldValue.serverTimestamp()` |
| `chiTiet` | Mảng map: mỗi phần tử có thể có `tenSanPham`, `soLuong`, `donGia` |

Firestore tự sinh **document ID** (chuỗi ngẫu nhiên). Adapter hiển thị 6 ký tự đầu của ID làm “mã đơn” rút gọn.

## Model Java: `NhapHang`

Class `com.nhom5.pharma.feature.nhaphang.NhapHang`:

- `@IgnoreExtraProperties` — document có thêm field không khai báo vẫn không lỗi deserialize.
- `@ServerTimestamp` trên `createdAt` — map với server timestamp.
- Có constructor rỗng (bắt buộc cho Firestore mapping).
- Getter/setter cho các field Firebase cần đọc; một số field khai báo trong class có thể chưa được dùng trên UI.

Khi các tab khác cần collection riêng (`products`, `suppliers`, …), nên tạo class model tương tự và đặt trong đúng package `feature.*`.

## Bảo mật (quan trọng)

- **Firestore Security Rules** phải được trưởng nhóm cấu hình: ai được đọc/ghi collection nào, có cần đăng nhập Firebase Auth không.
- Repo công khai + `google-services.json` có trong Git: coi như **API key client lộ**; an toàn thực tế dựa vào **Rules** và hạn chế trên Google Cloud, không dựa vào giấu file.

## Gợi ý collection cho các module còn lại

(Nhóm tự đặt tên thống nhất, ví dụ:)

- Sản phẩm: `products`
- Lô hàng: `batches`
- Nhà cung cấp: `suppliers`

Cần khóa ngoại / tham chiếu document ID giữa đơn nhập ↔ chi tiết ↔ sản phẩm thì thiết kế trước trên giấy hoặc schema ngắn để tránh refactor lớn sau này.
