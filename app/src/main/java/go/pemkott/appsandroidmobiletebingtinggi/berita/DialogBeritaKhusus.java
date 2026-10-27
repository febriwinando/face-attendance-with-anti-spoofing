package go.pemkott.appsandroidmobiletebingtinggi.berita;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.berita.BeritaItem;

public class DialogBeritaKhusus {

    public static void showModal(Context context, BeritaItem headlineItem) {
        if (context == null || headlineItem == null) return;

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_berita_khusus, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
            int insetPx = (int) (6 * context.getResources().getDisplayMetrics().density);
            InsetDrawable inset = new InsetDrawable(back, insetPx, 0, insetPx, 0);
            dialog.getWindow().setBackgroundDrawable(inset);
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        MaterialCardView cvCloseDialog = view.findViewById(R.id.cvCloseDialog);
        ImageView btnCloseDialog = view.findViewById(R.id.btnCloseDialog);
        ImageView ivHeadlineImage = view.findViewById(R.id.ivHeadlineImage);
        TextView tvHeadlineBadge = view.findViewById(R.id.tvHeadlineBadge);
        TextView tvHeadlineTitle = view.findViewById(R.id.tvHeadlineTitle);
        TextView tvHeadlineSummary = view.findViewById(R.id.tvHeadlineSummary);
        TextView tvHeadlineDate = view.findViewById(R.id.tvHeadlineDate);
        TextView tvHeadlineViews = view.findViewById(R.id.tvHeadlineViews);
        MaterialButton btnReadHeadline = view.findViewById(R.id.btnReadHeadline);

        tvHeadlineTitle.setText(headlineItem.getTitle());

        if (headlineItem.getSummary() != null && !headlineItem.getSummary().trim().isEmpty()) {
            tvHeadlineSummary.setText(headlineItem.getSummary().trim());
        } else {
            View cvSummaryBox = view.findViewById(R.id.cvSummaryBox);
            if (cvSummaryBox != null) cvSummaryBox.setVisibility(View.GONE);
        }

        if (headlineItem.getCategory() != null && !headlineItem.getCategory().trim().isEmpty()) {
            tvHeadlineBadge.setText("📢 " + headlineItem.getCategory().toUpperCase());
        } else {
            tvHeadlineBadge.setText("📢 INFORMASI PENTING");
        }

        if (tvHeadlineDate != null && headlineItem.getDateFormatted() != null) {
            tvHeadlineDate.setText(headlineItem.getDateFormatted());
        }

        if (tvHeadlineViews != null) {
            tvHeadlineViews.setText(headlineItem.getViews() + "x");
        }

        String imgUrl = headlineItem.getThumbnailUrl();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            Glide.with(context)
                    .load(imgUrl)
                    .placeholder(R.drawable.one_person)
                    .error(R.drawable.one_person)
                    .into(ivHeadlineImage);
        }

        View.OnClickListener dismissAction = v -> dialog.dismiss();
        if (cvCloseDialog != null) cvCloseDialog.setOnClickListener(dismissAction);
        if (btnCloseDialog != null) btnCloseDialog.setOnClickListener(dismissAction);

        btnReadHeadline.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(context, DetailBeritaActivity.class);
            intent.putExtra(DetailBeritaActivity.EXTRA_SLUG, headlineItem.getSlug());
            intent.putExtra(DetailBeritaActivity.EXTRA_TITLE, headlineItem.getTitle());
            intent.putExtra(DetailBeritaActivity.EXTRA_THUMBNAIL, headlineItem.getThumbnailUrl());
            context.startActivity(intent);
        });

        // Entrance scale animation
        view.setScaleX(0.85f);
        view.setScaleY(0.85f);
        view.setAlpha(0f);
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(350)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();

        dialog.setCancelable(true);
        dialog.show();
    }
}
