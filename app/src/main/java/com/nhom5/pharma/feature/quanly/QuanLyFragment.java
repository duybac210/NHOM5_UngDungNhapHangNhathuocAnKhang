package com.nhom5.pharma.feature.quanly;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nhom5.pharma.R;
import com.nhom5.pharma.feature.dangnhap.DangNhapActivity;

public class QuanLyFragment extends Fragment {

    private RelativeLayout btnBasicInfo, btnChangePassword, btnBackup, btnLogout;
    private LinearLayout expandableBasicInfo;
    private ImageView ivArrowBasicInfo;
    private TextView tvUserNameHeader, tvFullNameDetail, tvPhoneDetail, tvEmailDetail, tvAddressDetail, btnEditProfile;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isBasicInfoExpanded = false;
    private String currentDocId = "";
    private String currentMaChiNhanh = "";

    public QuanLyFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quan_ly, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvUserNameHeader = view.findViewById(R.id.tvUserNameHeader);
        tvFullNameDetail = view.findViewById(R.id.tvFullNameDetail);
        tvPhoneDetail = view.findViewById(R.id.tvPhoneDetail);
        tvEmailDetail = view.findViewById(R.id.tvEmailDetail);
        tvAddressDetail = view.findViewById(R.id.tvAddressDetail);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        btnBasicInfo = view.findViewById(R.id.btnBasicInfo);
        expandableBasicInfo = view.findViewById(R.id.expandableBasicInfo);
        ivArrowBasicInfo = view.findViewById(R.id.ivArrowBasicInfo);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnBackup = view.findViewById(R.id.btnBackup);
        btnLogout = view.findViewById(R.id.btnLogout);

        loadUserInfo();
        setupListeners();
    }

    private void loadUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            String loggedInEmail = user.getEmail();
            tvEmailDetail.setText(loggedInEmail);

            db.collection("TaiKhoan")
                .whereEqualTo("email", loggedInEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        currentDocId = doc.getId();
                        
                        String tenNguoiDung = doc.getString("tenNguoiDung");
                        currentMaChiNhanh = doc.getString("maChiNhanh");

                        tvUserNameHeader.setText(tenNguoiDung != null ? tenNguoiDung : "N/A");
                        tvFullNameDetail.setText(tenNguoiDung != null ? tenNguoiDung : "---");

                        if (currentMaChiNhanh != null) {
                            loadBranchInfo(currentMaChiNhanh);
                        }
                    }
                });
        }
    }

    private void loadBranchInfo(String maChiNhanh) {
        db.collection("ChiNhanh").document(maChiNhanh).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String sdt = doc.getString("sdt");
                    String diaChi = doc.getString("diaChi");
                    tvPhoneDetail.setText(sdt != null ? sdt : "---");
                    tvAddressDetail.setText(diaChi != null ? diaChi : "---");
                }
            });
    }

    private void setupListeners() {
        btnBasicInfo.setOnClickListener(v -> {
            isBasicInfoExpanded = !isBasicInfoExpanded;
            expandableBasicInfo.setVisibility(isBasicInfoExpanded ? View.VISIBLE : View.GONE);
            ivArrowBasicInfo.setRotation(isBasicInfoExpanded ? 90 : 0);
        });

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnBackup.setOnClickListener(v -> showBackupInfoDialog());

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), DangNhapActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
        });
    }

    private void showBackupInfoDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Sao lưu & Phục hồi")
                .setIcon(R.drawable.ic_backup)
                .setMessage("Dữ liệu của bạn luôn được hệ thống tự động đồng bộ hóa an toàn trên đám mây của Pharma An Khang.")
                .setPositiveButton("Đã hiểu", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showEditProfileDialog() {
        if (currentDocId.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng đợi tải dữ liệu...", Toast.LENGTH_SHORT).show();
            return;
        }

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_profile);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText etName = dialog.findViewById(R.id.etEditFullName);
        EditText etPhone = dialog.findViewById(R.id.etEditPhone);
        EditText etAddr = dialog.findViewById(R.id.etEditAddress);
        Button btnSave = dialog.findViewById(R.id.btnSaveProfile);

        String oldName = tvFullNameDetail.getText().toString();
        String oldPhone = tvPhoneDetail.getText().toString();
        String oldAddr = tvAddressDetail.getText().toString();
        
        etName.setText(oldName.equals("---") ? "" : oldName);
        etPhone.setText(oldPhone.equals("---") ? "" : oldPhone);
        etAddr.setText(oldAddr.equals("---") ? "" : oldAddr);

        btnSave.setEnabled(false);
        TextWatcher editWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String n = etName.getText().toString().trim();
                String p = etPhone.getText().toString().trim();
                String a = etAddr.getText().toString().trim();
                btnSave.setEnabled(!n.isEmpty() && (!n.equals(oldName) || !p.equals(oldPhone) || !a.equals(oldAddr)));
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        etName.addTextChangedListener(editWatcher);
        etPhone.addTextChangedListener(editWatcher);
        etAddr.addTextChangedListener(editWatcher);

        btnSave.setOnClickListener(v -> {
            String n = etName.getText().toString().trim();
            String p = etPhone.getText().toString().trim();
            String a = etAddr.getText().toString().trim();

            WriteBatch batch = db.batch();
            DocumentReference userRef = db.collection("TaiKhoan").document(currentDocId);
            batch.update(userRef, "tenNguoiDung", n);

            if (currentMaChiNhanh != null && !currentMaChiNhanh.isEmpty()) {
                DocumentReference branchRef = db.collection("ChiNhanh").document(currentMaChiNhanh);
                batch.update(branchRef, "sdt", p, "diaChi", a);
            }

            batch.commit().addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                tvUserNameHeader.setText(n);
                tvFullNameDetail.setText(n);
                tvPhoneDetail.setText(p);
                tvAddressDetail.setText(a);
                dialog.dismiss();
            }).addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });
        dialog.show();
    }

    private void showChangePasswordDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_change_password);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText etCur = dialog.findViewById(R.id.etCurrentPassword);
        EditText etNew = dialog.findViewById(R.id.etNewPassword);
        EditText etCon = dialog.findViewById(R.id.etConfirmPassword);
        View layoutCur = dialog.findViewById(R.id.layoutCurrentPassword);
        ImageView ivEye1 = dialog.findViewById(R.id.ivEyeIcon1);
        ImageView ivEye2 = dialog.findViewById(R.id.ivEyeIcon2);
        Button btnSave = dialog.findViewById(R.id.btnSavePassword);

        View.OnClickListener toggle = v -> {
            EditText target = (v.getId() == R.id.ivEyeIcon1) ? etNew : etCon;
            ImageView eye = (ImageView) v;
            if (target.getTransformationMethod() instanceof PasswordTransformationMethod) {
                target.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                eye.setImageResource(R.drawable.ic_eye);
            } else {
                target.setTransformationMethod(PasswordTransformationMethod.getInstance());
                eye.setImageResource(R.drawable.ic_eye_off);
            }
            target.setSelection(target.length());
        };
        ivEye1.setOnClickListener(toggle);
        ivEye2.setOnClickListener(toggle);

        TextWatcher tw = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String n = etNew.getText().toString().trim();
                String c = etCon.getText().toString().trim();
                String cur = etCur.getText().toString().trim();
                boolean reAuth = layoutCur.getVisibility() == View.VISIBLE;
                boolean valid = n.length() >= 6 && n.equals(c);
                btnSave.setEnabled(reAuth ? (valid && !cur.isEmpty()) : valid);
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        etNew.addTextChangedListener(tw);
        etCon.addTextChangedListener(tw);
        etCur.addTextChangedListener(tw);

        btnSave.setOnClickListener(v -> {
            String newP = etNew.getText().toString().trim();
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) return;

            if (layoutCur.getVisibility() == View.VISIBLE) {
                String curP = etCur.getText().toString().trim();
                user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), curP))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Cập nhật Auth trước
                            user.updatePassword(newP).addOnCompleteListener(t -> {
                                if (t.isSuccessful()) {
                                    // CẬP NHẬT TIẾP VÀO FIRESTORE
                                    if (!currentDocId.isEmpty()) {
                                        db.collection("TaiKhoan").document(currentDocId).update("matKhau", newP);
                                    }
                                    dialog.dismiss(); 
                                    Toast.makeText(getContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show(); 
                                }
                            });
                        } else Toast.makeText(getContext(), "Mật khẩu cũ sai", Toast.LENGTH_SHORT).show();
                    });
            } else {
                user.updatePassword(newP).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // CẬP NHẬT TIẾP VÀO FIRESTORE
                        if (!currentDocId.isEmpty()) {
                            db.collection("TaiKhoan").document(currentDocId).update("matKhau", newP);
                        }
                        dialog.dismiss(); 
                        Toast.makeText(getContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show(); 
                    }
                    else if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                        layoutCur.setVisibility(View.VISIBLE);
                        dialog.findViewById(R.id.tvCurrentPasswordLabel).setVisibility(View.VISIBLE);
                        btnSave.setEnabled(false);
                    }
                });
            }
        });
        dialog.show();
    }
}