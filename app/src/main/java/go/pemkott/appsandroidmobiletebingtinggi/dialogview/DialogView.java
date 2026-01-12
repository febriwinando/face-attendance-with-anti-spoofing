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

        tvWarning1.setText(w1+" "+w2);
        dataKosong.setCancelable(true);
        tvTutupDialog.setOnClickListener(v -> dataKosong.dismiss());

        dataKosong.show();
    }


    public void viewErrorKosong(Context context){
        Dialog dataKosong = new Dialog(context, R.style.DialogStyle);
        dataKosong.setContentView(R.layout.view_empty);
        dataKosong.setCancelable(true);
        dataKosong.show();
    }

    public void viewSukses(Activity activity, String info){
        Dialog dialogSukes = new Dialog(context, R.style.DialogStyle);
        dialogSukes.setContentView(R.layout.view_sukses);
        dialogSukes.setCancelable(true);
        ImageView tvTutupDialog = dialogSukes.findViewById(R.id.ivTutupDialogBerhasil);
        TextView tvSukseKeterangan = dialogSukes.findViewById(R.id.tvSukseKeterangan);
        tvSukseKeterangan.setText(info);

        tvTutupDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSukes.dismiss();
                activity.finish();
            }
        });

        dialogSukes.show();
    }

    public void viewFoto(Bitmap data){
        Dialog dialogFoto = new Dialog(context, R.style.DialogStyle);
        dialogFoto.setContentView(R.layout.item_view_photo);
        dialogFoto.setCancelable(true);

        ShapeableImageView imageView = dialogFoto.findViewById(R.id.ivFoto);
        ImageView tvTutupDialog = dialogFoto.findViewById(R.id.tvCloseViewFoto);

        imageView.setImageBitmap(data);

        tvTutupDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFoto.dismiss();
            }
        });

        dialogFoto.show();
    }

    public void pesanError(Context context){

        Dialog errorDialogs = new Dialog(context, R.style.DialogStyle);
        errorDialogs.setContentView(R.layout.view_error);
        ImageView tvTutupDialog = errorDialogs.findViewById(R.id.tvTutupDialog);

        tvTutupDialog.setOnClickListener(v -> errorDialogs.dismiss());

        errorDialogs.show();

    }


}
