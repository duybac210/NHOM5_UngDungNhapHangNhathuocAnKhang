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
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom5.pharma.R;
import com.nhom5.pharma.feature.dangnhap.DangNhapActivity;
import com.nhom5.pharma.util.SecurityUtils;

public class QuanLyFragment extends Fragment {

    private RelativeLayout btnBasicInfo, btnChangePassword, btnBackup, btnLogout;
    private LinearLayout expandableBasicInfo;
    private ImageView ivArrowBasicInfo;
    private TextView tvUserNameHeader, tvFullNameDetail, tvPhoneDetail, tvEmailDetail, tvAddressDetail;
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
                        Log.d("QuanLyFragment", "Current User Doc ID: " + currentDocId);
                        
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
                .setTitle("Sao lưu \u0026 Phục hồi")
                .setIcon(R.drawable.ic_backup)
                .setMessage("Dữ liệu của bạn luôn được hệ thống tự động đồng bộ hóa an toàn trên đám mây của Pharma An Khang.")
                .setPositiveButton("Đã hiểu", (dialog, which) -> dialog.dismiss())
                .show();
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

            btnSave.setEnabled(false);
            btnSave.setText("Đang xử lý...");

            if (layoutCur.getVisibility() == View.VISIBLE) {
                String curP = etCur.getText().toString().trim();
                user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), curP))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            updateUserPassword(user, newP, dialog);
                        } else {
                            btnSave.setEnabled(true);
                            btnSave.setText("Lưu thay đổi");
                            Toast.makeText(getContext(), "Mật khẩu cũ không chính xác", Toast.LENGTH_SHORT).show();
                        }
                    });
            } else {
                updateUserPassword(user, newP, dialog);
            }
        });
        dialog.show();
    }

    private void updateUserPassword(FirebaseUser user, String newP, Dialog dialog) {
        user.updatePassword(newP).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!currentDocId.isEmpty()) {
                    String encryptedPassword = SecurityUtils.encrypt(newP);
                    db.collection("TaiKhoan").document(currentDocId)
                        .update("matKhau", encryptedPassword)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("QuanLyFragment", "Firestore password updated with AES: " + encryptedPassword);
                            Toast.makeText(getContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("QuanLyFragment", "Error updating Firestore", e);
                            Toast.makeText(getContext(), "Lỗi cập nhật Firestore", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
                } else {
                    Toast.makeText(getContext(), "Đổi thành công (Auth), nhưng không tìm thấy Doc ID", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            } else {
                if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                    View layoutCur = dialog.findViewById(R.id.layoutCurrentPassword);
                    layoutCur.setVisibility(View.VISIBLE);
                    dialog.findViewById(R.id.tvCurrentPasswordLabel).setVisibility(View.VISIBLE);
                    Button btnSave = dialog.findViewById(R.id.btnSavePassword);
                    btnSave.setEnabled(false);
                    btnSave.setText("Lưu thay đổi");
                    Toast.makeText(getContext(), "Vui lòng xác thực lại mật khẩu cũ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }
}
