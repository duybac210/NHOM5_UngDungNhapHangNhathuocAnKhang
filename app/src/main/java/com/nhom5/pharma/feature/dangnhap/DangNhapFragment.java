package com.nhom5.pharma.feature.dangnhap;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nhom5.pharma.MainActivity;
import com.nhom5.pharma.R;

public class DangNhapFragment extends Fragment {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ImageView btnBack, ivEyeIcon;
    private TextView tvForgotPassword, tvSignUp;
    private boolean isPasswordVisible = false;

    private FirebaseAuth mAuth;

    public DangNhapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dang_nhap, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        btnBack = view.findViewById(R.id.btnBack);
        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        ivEyeIcon = view.findViewById(R.id.ivEyeIcon);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        tvSignUp = view.findViewById(R.id.tvSignUp);

        setupListeners();

        // Ensure initial state is hidden
        etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        ivEyeIcon.setImageResource(R.drawable.ic_eye_off);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMain();
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        TextWatcher loginTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                btnLogin.setEnabled(!username.isEmpty() && !password.isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        etUsername.addTextChangedListener(loginTextWatcher);
        etPassword.addTextChangedListener(loginTextWatcher);

        ivEyeIcon.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                // Show password
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                ivEyeIcon.setImageResource(R.drawable.ic_eye);
            } else {
                // Hide password
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ivEyeIcon.setImageResource(R.drawable.ic_eye_off);
            }
            etPassword.setSelection(etPassword.length());
        });

        btnLogin.setOnClickListener(v -> {
            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!email.contains("@")) {
                email = email + "@ankhang.com";
            }

            performLogin(email, password);
        });
    }

    private void performLogin(String email, String password) {
        btnLogin.setEnabled(false);
        btnLogin.setText("Đang xử lý...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        }
                        navigateToMain();
                    } else {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Tiếp tục");
                        String message = task.getException() != null ? task.getException().getMessage()
                                : "Đăng nhập thất bại";
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi: " + message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void navigateToMain() {
        if (getActivity() == null) {
            return;
        }
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}