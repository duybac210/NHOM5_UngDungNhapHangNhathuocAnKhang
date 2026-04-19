# 5. Làm việc theo chức năng

Mỗi tab / màn tương ứng một **module** trong `feature.*`. Trưởng nhóm có thể **giao mỗi người một package** để giảm đè file giữa các thành viên.

## Module: Nhập hàng (`feature.nhaphang`)

**Trạng thái:** Đã có Firestore realtime + `RecyclerView` + tìm kiếm + chi tiết + xóa.

| Thành phần | File |
|------------|------|
| Màn chính | `NhapHangFragment.java`, `fragment_nhap_hang.xml` |
| Dòng list | `NhapHangAdapter.java`, `item_nhap_hang.xml` |
| Model | `NhapHang.java` |

**Việc có thể làm thêm:** Form tạo/sửa phiếu nhập thật, cập nhật trạng thái duyệt, đồng bộ write với Auth (chỉ user được phép ghi).

## Module: Sản phẩm (`feature.sanpham`)

**Trạng thái:** Fragment + layout cơ bản, chưa logic.

| Thành phần | File |
|------------|------|
| Fragment | `SanPhamFragment.java`, `fragment_san_pham.xml` |

**Gợi ý:** Thiết kế collection Firestore (hoặc subcollection), model `SanPham`, `RecyclerView` hoặc form; tái dùng pattern từ `nhaphang` nếu phù hợp.

## Module: Lô hàng (`feature.lohang`)

**Trạng thái:** Đã có list realtime + lọc + tìm kiếm + chi tiết.

| Thành phần | File |
|------------|------|
| Fragment | `LoHangFragment.java`, `fragment_lo_hang.xml` |
| Chi tiết | `ChiTietLoHangActivity.java` |

**Gợi ý:** Liên kết lô với sản phẩm / đơn nhập (ID tham chiếu), hạn sử dụng, số lô. Điểm ghi Firestore hiện nằm ở `NhapHangRepository.upsertLoHang(...)`.

## Module: Nhà cung cấp (`feature.nhacungcap`)

**Trạng thái:** Khung rỗng.

| Thành phần | File |
|------------|------|
| Fragment | `NhaCungCapFragment.java`, `fragment_nha_cung_cap.xml` |

**Gợi ý:** CRUD nhà cung cấp; field `tenNhaCungCap` trên `import_orders` có thể thay bằng ID NCC sau này khi chuẩn hóa dữ liệu.

## Module: Quản lý (`feature.quanly`)

**Trạng thái:** Khung rỗng.

| Thành phần | File |
|------------|------|
| Fragment | `QuanLyFragment.java`, `fragment_quan_ly.xml` |

**Gợi ý:** Báo cáo nhanh, cài đặt, quản lý tài khoản, hoặc tổng hợp thống kê — theo yêu cầu môn / sản phẩm.

## Module: Đăng nhập (`feature.dangnhap`)

**Trạng thái:** Có `DangNhapFragment` + `fragment_dang_nhap.xml` nhưng **chưa** được `MainActivity` hoặc manifest dùng làm màn đầu.

**Gợi ý:** Tích hợp Firebase Authentication (Email/Google); sau khi đăng nhập thành công mới `startActivity` tới `MainActivity` hoặc hiện fragment chính.

## Checklist cho developer khi nhận một chức năng

1. Kéo nhánh mới từ `main` (hoặc `develop` nếu nhóm dùng).
2. Chạy app, mở đúng tab / màn được giao.
3. Đọc Fragment + layout hiện có; chụp hoặc liệt kê **view id** cần bind.
4. Nếu cần Firestore: thống nhất **tên collection** và field với trưởng nhóm; cập nhật Rules.
5. Commit nhỏ, message rõ (`feat(sanpham): load product list`).
6. Tạo Pull Request; nhờ người khác review, đặc biệt khi sửa `MainActivity`, `build.gradle`, hoặc `AndroidManifest.xml`.

## Tránh xung đột

- **Ưu tiên** sửa trong package `feature.<module>` và layout tên `fragment_<module>.xml`.
- Hạn chế nhiều người cùng sửa `MainActivity` — gom một người hoặc tách patch nhỏ.
- Drawable / `colors.xml` / `strings.xml` dùng chung: thêm key mới thay vì sửa giá trị key người khác đang dùng (trừ khi thống nhất).
