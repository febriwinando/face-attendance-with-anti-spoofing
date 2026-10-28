package go.pemkott.appsandroidmobiletebingtinggi.utils;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import go.pemkott.appsandroidmobiletebingtinggi.R;

public class CustomUiFeedback {

    public enum Type {
        SYNC, SUCCESS, ERROR
    }

    /**
     * Tampilkan Banner Notifikasi Melayang di Bagian Atas Layar (Sleek Top Banner)
     */
    public static void showTopBanner(Activity activity, String message, Type type) {
        if (activity == null || activity.isFinishing()) return;

        ViewGroup rootView = activity.findViewById(android.R.id.content);
        if (rootView == null) return;

        MaterialCardView card = new MaterialCardView(activity);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.TOP;
        params.setMargins(32, 90, 32, 0);
        card.setLayoutParams(params);
        card.setCardElevation(8f);
        card.setRadius(24f);
        card.setStrokeWidth(1);

        View content = LayoutInflater.from(activity).inflate(R.layout.item_top_banner_feedback, null);
        FrameLayout flBadge = content.findViewById(R.id.flBannerBadge);
        ImageView ivIcon = content.findViewById(R.id.ivBannerIcon);
        ProgressBar pbSync = content.findViewById(R.id.pbBannerSync);
        TextView tvMsg = content.findViewById(R.id.tvBannerMessage);
        TextView tvSub = content.findViewById(R.id.tvBannerSubtitle);
        ImageView ivClose = content.findViewById(R.id.ivCloseBanner);

        tvMsg.setText(message);

        if (type == Type.SYNC) {
            card.setCardBackgroundColor(Color.parseColor("#F0F7FF"));
            card.setStrokeColor(Color.parseColor("#BFDBFE"));
            flBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#DBEAFE")));
            tvMsg.setTextColor(Color.parseColor("#1E3A8A"));
            tvSub.setText("Sistem Absensi Pemko Tebing Tinggi");

            pbSync.setVisibility(View.VISIBLE);
            ivIcon.setVisibility(View.GONE);

        } else if (type == Type.SUCCESS) {
            card.setCardBackgroundColor(Color.parseColor("#F0FDF4"));
            card.setStrokeColor(Color.parseColor("#BBF7D0"));
            flBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#DCFCE7")));
            tvMsg.setTextColor(Color.parseColor("#14532D"));
            tvSub.setText("Proses Berhasil Dilakukan");

            pbSync.setVisibility(View.GONE);
            ivIcon.setVisibility(View.VISIBLE);
            ivIcon.setImageResource(R.drawable.ic_sukses);
            ivIcon.setColorFilter(Color.parseColor("#15803D"));

        } else {
            card.setCardBackgroundColor(Color.parseColor("#FEF2F2"));
            card.setStrokeColor(Color.parseColor("#FECACA"));
            flBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
            tvMsg.setTextColor(Color.parseColor("#7F1D1D"));
            tvSub.setText("Pemberitahuan Sistem");

            pbSync.setVisibility(View.GONE);
            ivIcon.setVisibility(View.VISIBLE);
            ivIcon.setImageResource(R.drawable.ic_emot_error);
            ivIcon.setColorFilter(Color.parseColor("#B91C1C"));
        }

        card.addView(content);
        rootView.addView(card);

        // Slide down animation
        card.setTranslationY(-250f);
        card.animate()
                .translationY(0f)
                .setDuration(350)
                .start();

        Runnable dismissRunnable = () -> {
            if (card.getParent() != null) {
                card.animate()
                        .translationY(-300f)
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> rootView.removeView(card))
                        .start();
            }
        };

        ivClose.setOnClickListener(v -> dismissRunnable.run());

        // Auto dismiss after 2.8 seconds
        card.postDelayed(dismissRunnable, 2800);
    }
}
