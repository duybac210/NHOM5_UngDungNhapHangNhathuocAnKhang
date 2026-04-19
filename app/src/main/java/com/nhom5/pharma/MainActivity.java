package com.nhom5.pharma;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.nhom5.pharma.feature.dangnhap.DangNhapActivity;
import com.nhom5.pharma.feature.lohang.LoHangFragment;
import com.nhom5.pharma.feature.nhacungcap.NhaCungCapFragment;
import com.nhom5.pharma.feature.nhaphang.NhapHangFragment;
import com.nhom5.pharma.feature.quanly.QuanLyFragment;
import com.nhom5.pharma.feature.sanpham.SanPhamFragment;

public class MainActivity extends AppCompatActivity {

    private static final int TAB_ORDERS = 0;
    private static final int TAB_PRODUCTS = 1;
    private static final int TAB_BATCHES = 2;
    private static final int TAB_SUPPLIERS = 3;
    private static final int TAB_MANAGE = 4;

    private static final int ACTIVE_COLOR = Color.parseColor("#5cc849");
    private static final int INACTIVE_COLOR = Color.parseColor("#000000");

    private ImageView[] navIcons;
    private TextView[] navLabels;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
<<<<<<< HEAD
        
        // Tạm thời bỏ qua đăng nhập để làm Nhà cung cấp
        /*
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
=======
        mAuth = FirebaseAuth.getInstance();
        // TODO: Bỏ comment lại khi phát triển xong
        /*if (mAuth.getCurrentUser() == null) {
>>>>>>> f6626e1bd9cc4d313d85fe4f8056470d2969e674
            Intent intent = new Intent(this, DangNhapActivity.class);
            startActivity(intent);
            finish();
            return;
        }*/

        setContentView(R.layout.activity_main);

        navIcons = new ImageView[]{
                findViewById(R.id.iv_nav_orders),
                findViewById(R.id.iv_nav_products),
                findViewById(R.id.iv_nav_batches),
                findViewById(R.id.iv_nav_suppliers),
                findViewById(R.id.iv_nav_manage)
        };
        navLabels = new TextView[]{
                findViewById(R.id.tv_nav_orders),
                findViewById(R.id.tv_nav_products),
                findViewById(R.id.tv_nav_batches),
                findViewById(R.id.tv_nav_suppliers),
                findViewById(R.id.tv_nav_manage)
        };

        LinearLayout tabOrders = findViewById(R.id.nav_tab_orders);
        LinearLayout tabProducts = findViewById(R.id.nav_tab_products);
        LinearLayout tabBatches = findViewById(R.id.nav_tab_batches);
        LinearLayout tabSuppliers = findViewById(R.id.nav_tab_suppliers);
        LinearLayout tabManage = findViewById(R.id.nav_tab_manage);

        tabOrders.setOnClickListener(v -> selectTab(TAB_ORDERS));
        tabProducts.setOnClickListener(v -> selectTab(TAB_PRODUCTS));
        tabBatches.setOnClickListener(v -> selectTab(TAB_BATCHES));
        tabSuppliers.setOnClickListener(v -> selectTab(TAB_SUPPLIERS));
        tabManage.setOnClickListener(v -> selectTab(TAB_MANAGE));

<<<<<<< HEAD
        // CHUYỂN VỀ TAB NHÀ CUNG CẤP LÀM MẶC ĐỊNH
        selectTab(TAB_SUPPLIERS);
=======
        if (getIntent().getBooleanExtra("SELECT_MODE", false)) {
            selectTab(TAB_PRODUCTS);
        } else {
            selectTab(TAB_SUPPLIERS);  // TODO: Thay đổi thành TAB_ORDERS khi phát triển xong
        }
>>>>>>> f6626e1bd9cc4d313d85fe4f8056470d2969e674
    }

    private void selectTab(int index) {
        Fragment selected;
        switch (index) {
            case TAB_ORDERS:
                selected = new NhapHangFragment();
                break;
            case TAB_PRODUCTS:
                selected = new SanPhamFragment();
                break;
            case TAB_BATCHES:
                selected = new LoHangFragment();
                break;
            case TAB_SUPPLIERS:
                selected = new NhaCungCapFragment();
                break;
            case TAB_MANAGE:
                selected = new QuanLyFragment();
                break;
            default:
                selected = new NhaCungCapFragment();
                index = TAB_SUPPLIERS;
                break;
        }

        loadFragment(selected);

        for (int i = 0; i < navIcons.length; i++) {
            if (navIcons[i] == null || navLabels[i] == null) continue;
            boolean isSelected = (i == index);
            int color = isSelected ? ACTIVE_COLOR : INACTIVE_COLOR;
            ImageViewCompat.setImageTintList(navIcons[i], ColorStateList.valueOf(color));
            navLabels[i].setTextColor(color);
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }
}
