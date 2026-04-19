package com.nhom5.pharma.util;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import com.nhom5.pharma.R;

public class SuccessDialogHelper {
    public interface OnDialogDismissListener {
        void onDismiss();
    }

    public static void showSuccessDialog(Activity activity, String message, OnDialogDismissListener listener) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        tvMessage.setText(message);

        ImageView ivClose = dialog.findViewById(R.id.ivClose);
        ivClose.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onDismiss();
            }
        });

        dialog.show();
    }
}