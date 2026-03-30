# 6. Quy trình Git và hợp tác

## Remote chuẩn

Remote mặc định của nhóm:

`https://github.com/duybac210/NHOM5_49k14.1_Ung-dung-nhap-hang-cho-nha-thuoc-An-Khang.git`

(Xem trên GitHub: [repository](https://github.com/duybac210/NHOM5_49k14.1_Ung-dung-nhap-hang-cho-nha-thuoc-An-Khang).)

## Luồng làm việc gợi ý

1. **Cập nhật nhánh chính trên máy**
   ```bash
   git checkout main
   git pull origin main
   ```

2. **Tạo nhánh theo chức năng hoặc người**
   ```bash
   git checkout -b feat/san-pham-list
   ```
   Tên nhánh gợi ý: `feat/…`, `fix/…`, `chore/…`.

3. **Làm thay đổi, commit**
   ```bash
   git add .
   git status
   git commit -m "feat(sanpham): bind recycler and load from Firestore"
   ```

4. **Đẩy lên GitHub**
   ```bash
   git push -u origin feat/san-pham-list
   ```

5. **Pull Request (PR)**  
   Trên GitHub: so sánh nhánh của bạn với `main`, mô tả ngắn thay đổi, gắn reviewer là trưởng nhóm hoặc bạn khác. Sau khi duyệt → **Merge**.

## Trước khi PR

- Build project trong Android Studio (**Build → Make Project**).
- Chạy app trên emulator / thiết bị, kiểm tra tab mình đụng tới.
- Xem `git diff` tránh commit nhầm file cá nhân (`.idea` workspace, `local.properties`) — project đã `.gitignore` một phần; vẫn nên kiểm tra.

## Khi bị conflict

```bash
git checkout main
git pull origin main
git checkout feat/san-pham-list
git merge main
```

Giải conflict trong Android Studio (Merge tool), build lại, commit merge.

## Tài kện nhạy cảm

- `google-services.json`: nhóm cần quyết định có commit hay gửi riêng qua kênh bảo mật. Nếu không commit, phải hướng dẫn rõ trong README/docs (đã có ở `02-cai-dat-va-chay-app.md`).

## Giao tiếp trong nhóm

- Thống nhất **tên collection Firestore** và **task** trên board / nhóm chat trước khi code song song.
- Thay đổi chung (`MainActivity`, schema dữ liệu): **bàn trước một phiên** để tránh làm đôi lần.
