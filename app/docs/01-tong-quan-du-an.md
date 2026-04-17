# 1. Tổng quan dự án

## Mục tiêu

Ứng dụng hỗ trợ **quản lý nhập hàng dược phẩm** (theo bối cảnh nhà thuốc / chuỗi): xem danh sách đơn nhập, sản phẩm, lô hàng, nhà cung cấp, và khu vực quản lý. Hiện phần **Nhập hàng** đã kết nối **Cloud Firestore**; các tab còn lại chủ yếu có **giao diện (layout)** và **Fragment khung**, chờ bổ sung logic và dữ liệu.

## Công nghệ

| Thành phần | Chi tiết |
|------------|---------|
| Ngôn ngữ | Java 11 |
| UI | XML layouts, AppCompat / Material |
| Cấu trúc màn hình | `AppCompatActivity` + `Fragment` (thay thế trong `FrameLayout`) |
| Backend dữ liệu (đã dùng) | Firebase: Firestore, Analytics; Firebase UI Firestore cho `RecyclerView` |
| Build | Gradle với Version Catalog (`libs.*`), plugin `com.google.gms.google-services` |
| Package / applicationId | `com.nhom5.pharma` |

## Kho mã nguồn

- GitHub: [duybac210/NHOM5_49k14.1_Ung-dung-nhap-hang-cho-nha-thuoc-An-Khang](https://github.com/duybac210/NHOM5_49k14.1_Ung-dung-nhap-hang-cho-nha-thuoc-An-Khang)

Mỗi thành viên clone repo về máy, mở bằng **Android Studio**, làm việc trên nhánh riêng rồi đẩy và gộp vào nhánh chung (xem file `06-quy-trinh-git-va-hop-tac.md`).

## Các tab trong app (bottom navigation tùy chỉnh)

Thanh điều hướng dưới cùng đổi màu tab khi chọn. Ứng với từng tab:

| Tab (nhãn trên UI) | Fragment | Ý nghĩa nghiệp vụ |
|---------------------|----------|---------------------|
| Nhập hàng / Đơn | `NhapHangFragment` | Danh sách đơn nhập từ Firestore, tìm kiếm, nút tạo dữ liệu mẫu |
| Sản phẩm | `SanPhamFragment` | Khung sẵn, cần CRUD / Firestore tùy thiết kế |
| Lô hàng | `LoHangFragment` | Khung sẵn |
| NCC (nhà cung cấp) | `NhaCungCapFragment` | Khung sẵn |
| Quản lý | `QuanLyFragment` | Khung sẵn |

Trong code `MainActivity`, các tab được đặt tên nội bộ: `TAB_ORDERS`, `TAB_PRODUCTS`, `TAB_BATCHES`, `TAB_SUPPLIERS`, `TAB_MANAGE`.

## Màn hình đăng nhập

Tồn tại `DangNhapFragment` và layout `fragment_dang_nhap`, nhưng **chưa** được gắn vào `MainActivity` (app hiện mở thẳng `MainActivity`). Nếu nhóm cần flow đăng nhập, sẽ phải thêm Activity hoặc đổi launcher và điều hướng sau khi đăng nhập.

## Firebase

- Console dự án (ví dụ xem dữ liệu `import_orders`): [Firestore – pharmaimport-474c3](https://console.firebase.google.com/project/pharmaimport-474c3/firestore/databases/-default-/data/~2Fimport_orders~2F0HPOZtGswkvDrAOsn7x4)  
  (Cần tài khoản Google được mời quyền trên project.)

Chi tiết collection, field và quyền truy cập: xem `04-firebase-va-firestore.md`.
