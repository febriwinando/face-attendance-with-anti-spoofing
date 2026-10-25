package go.pemkott.appsandroidmobiletebingtinggi.dialogview;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;

import go.pemkott.appsandroidmobiletebingtinggi.R;

public class DialogView {

    Context context;

    public DialogView(Context context) {
        this.context = context;
    }

    public void viewNotifKosong(Context context, String w1, String w2){
        Dialog dataKosong = new Dialog(context, R.style.DialogStyle);
        dataKosong.setContentView(R.layout.view_warning_kosong);
        TextView tvWarning1 = dataKosong.findViewById(R.id.tvWarning1);
        ImageView tvTutupDialog = dataKosong.findViewById(R.id.tvTutupDialog);

        String msg = (w1 != null ? w1 : "") + (w2 != null ? " " + w2 : "");
        if (tvWarning1 != null) {
            tvWarning1.setText(msg.trim());
        }
        dataKosong.setCancelable(true);
        if (tvTutupDialog != null) {
            tvTutupDialog.setOnClickListener(v -> dataKosong.dismiss());
        }

        dataKosong.show();
    }

    public void viewErrorKosong(Context context){
        Dialog dataKosong = new Dialog(context, R.style.DialogStyle);
        dataKosong.setContentView(R.layout.view_empty);
        dataKosong.setCancelable(true);
        dataKosong.show();
    }

    public void viewSukses(Activity activity, String info){
        Context ctx = (activity != null) ? activity : context;
        Dialog dialogSukes = new Dialog(ctx, R.style.DialogStyle);
        dialogSukes.setContentView(R.layout.view_sukses);
        dialogSukes.setCancelable(true);
        ImageView tvTutupDialog = dialogSukes.findViewById(R.id.ivTutupDialogBerhasil);
        TextView tvSukseKeterangan = dialogSukes.findViewById(R.id.tvSukseKeterangan);

        if (tvSukseKeterangan != null) {
            tvSukseKeterangan.setText(info);
        }

        if (tvTutupDialog != null) {
            tvTutupDialog.setOnClickListener(v -> {
                dialogSukes.dismiss();
                if (activity != null) {
                    activity.finish();
                }
            });
        }

        dialogSukes.show();
    }

    public void viewSuksesNonFinish(Context context, String info) {
        Dialog dialogSukes = new Dialog(context, R.style.DialogStyle);
        dialogSukes.setContentView(R.layout.view_sukses);
        dialogSukes.setCancelable(true);
        ImageView tvTutupDialog = dialogSukes.findViewById(R.id.ivTutupDialogBerhasil);
        TextView tvSukseKeterangan = dialogSukes.findViewById(R.id.tvSukseKeterangan);

        if (tvSukseKeterangan != null) {
            tvSukseKeterangan.setText(info);
        }

        if (tvTutupDialog != null) {
            tvTutupDialog.setOnClickListener(v -> dialogSukes.dismiss());
        }

        dialogSukes.show();
    }

    public void viewFoto(Bitmap data){
        Dialog dialogFoto = new Dialog(context, R.style.DialogStyle);
        dialogFoto.setContentView(R.layout.item_view_photo);
        dialogFoto.setCancelable(true);

        ShapeableImageView imageView = dialogFoto.findViewById(R.id.ivFoto);
        ImageView tvTutupDialog = dialogFoto.findViewById(R.id.tvCloseViewFoto);

        if (imageView != null) {
            imageView.setImageBitmap(data);
        }

        if (tvTutupDialog != null) {
            tvTutupDialog.setOnClickListener(v -> dialogFoto.dismiss());
        }

        dialogFoto.show();
    }

    public void pesanError(Context context){
        Dialog errorDialogs = new Dialog(context, R.style.DialogStyle);
        errorDialogs.setContentView(R.layout.view_error);
        ImageView tvTutupDialog = errorDialogs.findViewById(R.id.tvTutupDialog);

        if (tvTutupDialog != null) {
            tvTutupDialog.setOnClickListener(v -> errorDialogs.dismiss());
        }

        errorDialogs.show();
    }

    public void pesanErrorDetail(Context context, String title, String detailMessage){
        Dialog errorDialogs = new Dialog(context, R.style.DialogStyle);
        errorDialogs.setContentView(R.layout.view_warning_kosong);
        TextView tvWarning1 = errorDialogs.findViewById(R.id.tvWarning1);
        ImageView tvTutupDialog = errorDialogs.findViewById(R.id.tvTutupDialog);

        if (tvWarning1 != null) {
            tvWarning1.setText(detailMessage);
        }
        errorDialogs.setCancelable(true);
        if (tvTutupDialog != null) {
            tvTutupDialog.setOnClickListener(v -> errorDialogs.dismiss());
        }

        errorDialogs.show();
    }
}
