package go.pemkott.appsandroidmobiletebingtinggi.admin;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.ResponsePOJO;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminInsertMassalActivity extends AppCompatActivity {

    private EditText etTanggal, etNips;
    private CheckBox cbUpdateAl;
    private TextView tvSummaryInfo;
    private Calendar calendar = Calendar.getInstance();
    private DialogView dialogView;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_insert_massal);

        dialogView = new DialogView(this);
        ImageView ivBack = findViewById(R.id.ivAdminInsertBack);
        etTanggal = findViewById(R.id.etAdminInsertTanggal);
        etNips = findViewById(R.id.etAdminInsertNips);
        cbUpdateAl = findViewById(R.id.cbAdminUpdateAl);
        tvSummaryInfo = findViewById(R.id.tvAdminInsertSummary);
        MaterialButton btnPreview = findViewById(R.id.btnAdminPreviewMassal);
        MaterialButton btnStore = findViewById(R.id.btnAdminStoreMassal);

        ivBack.setOnClickListener(v -> finish());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        etTanggal.setText(dateFormat.format(calendar.getTime()));
        etTanggal.setOnClickListener(v -> showDatePicker());

        btnPreview.setOnClickListener(v -> previewMassal());
        btnStore.setOnClickListener(v -> storeMassal());
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            etTanggal.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void previewMassal() {
        String tanggal = etTanggal.getText().toString().trim();
        String nips = etNips.getText().toString().trim();
        boolean updateAl = cbUpdateAl.isChecked();

        if (tanggal.isEmpty() || nips.isEmpty()) {
            dialogView.viewNotifKosong(this, "Peringatan", "Tanggal dan daftar NIP pegawai wajib diisi.");
            return;
        }

        Dialog dialogProses = new Dialog(this, R.style.DialogStyle);
        dialogProses.setContentView(R.layout.view_proses);
        dialogProses.setCancelable(false);
        dialogProses.show();

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.previewInsertMassal(tanggal, nips, updateAl)
                .enqueue(new Callback<ResponsePOJO>() {
                    @Override
                    public void onResponse(Call<ResponsePOJO> call, Response<ResponsePOJO> response) {
                        dialogProses.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            ResponsePOJO pojo = response.body();
                            tvSummaryInfo.setText("Hasil Preview: " + (pojo.getRemarks() != null ? pojo.getRemarks() : "Preview berhasil diproses."));
                        } else {
                            dialogView.viewNotifKosong(AdminInsertMassalActivity.this, "Gagal Preview", "HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponsePOJO> call, Throwable t) {
                        dialogProses.dismiss();
                        dialogView.viewNotifKosong(AdminInsertMassalActivity.this, "Koneksi Gagal", t.getMessage());
                    }
                });
    }

    private void storeMassal() {
        String tanggal = etTanggal.getText().toString().trim();
        String nips = etNips.getText().toString().trim();
        boolean updateAl = cbUpdateAl.isChecked();

        if (tanggal.isEmpty() || nips.isEmpty()) {
            dialogView.viewNotifKosong(this, "Peringatan", "Tanggal dan daftar NIP pegawai wajib diisi.");
            return;
        }

        Dialog dialogProses = new Dialog(this, R.style.DialogStyle);
        dialogProses.setContentView(R.layout.view_proses);
        dialogProses.setCancelable(false);
        dialogProses.show();

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.storeInsertMassal(tanggal, nips, updateAl)
                .enqueue(new Callback<ResponsePOJO>() {
                    @Override
                    public void onResponse(Call<ResponsePOJO> call, Response<ResponsePOJO> response) {
                        dialogProses.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            ResponsePOJO pojo = response.body();
                            if (pojo.isStatus()) {
                                dialogView.viewSukses(AdminInsertMassalActivity.this, pojo.getRemarks() != null ? pojo.getRemarks() : "Eksekusi absensi massal berhasil!");
                            } else {
                                dialogView.viewNotifKosong(AdminInsertMassalActivity.this, "Gagal Eksekusi", pojo.getRemarks() != null ? pojo.getRemarks() : "Proses eksekusi gagal.");
                            }
                        } else {
                            dialogView.viewNotifKosong(AdminInsertMassalActivity.this, "Gagal Server", "HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponsePOJO> call, Throwable t) {
                        dialogProses.dismiss();
                        dialogView.viewNotifKosong(AdminInsertMassalActivity.this, "Koneksi Gagal", t.getMessage());
                    }
                });
    }
}
