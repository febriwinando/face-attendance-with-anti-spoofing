package go.pemkott.appsandroidmobiletebingtinggi.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.ResponsePOJO;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminLokasiPegawaiActivity extends AppCompatActivity {

    private EditText etEmployeeId, etAlamat, etLat, etLng;
    private DialogView dialogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_lokasi_pegawai);

        dialogView = new DialogView(this);
        ImageView ivBack = findViewById(R.id.ivAdminLokasiBack);
        etEmployeeId = findViewById(R.id.etAdminLokasiEmployeeId);
        etAlamat = findViewById(R.id.etAdminLokasiAlamat);
        etLat = findViewById(R.id.etAdminLokasiLat);
        etLng = findViewById(R.id.etAdminLokasiLng);
        MaterialButton btnSubmit = findViewById(R.id.btnAdminLokasiSubmit);

        ivBack.setOnClickListener(v -> finish());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        btnSubmit.setOnClickListener(v -> submitLokasi());
    }

    private void submitLokasi() {
        String empId = etEmployeeId.getText().toString().trim();
        String alamat = etAlamat.getText().toString().trim();
        String lat = etLat.getText().toString().trim();
        String lng = etLng.getText().toString().trim();

        if (empId.isEmpty() || alamat.isEmpty() || lat.isEmpty() || lng.isEmpty()) {
            dialogView.viewNotifKosong(this, "Peringatan", "Semua field (ID Pegawai, Alamat, Lat, Lng) wajib diisi.");
            return;
        }

        Dialog dialogProses = new Dialog(this, R.style.DialogStyle);
        dialogProses.setContentView(R.layout.view_proses);
        dialogProses.setCancelable(false);
        dialogProses.show();

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.storeLokasiPegawaiAdmin(empId, alamat, lat, lng)
                .enqueue(new Callback<ResponsePOJO>() {
                    @Override
                    public void onResponse(Call<ResponsePOJO> call, Response<ResponsePOJO> response) {
                        dialogProses.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            ResponsePOJO pojo = response.body();
                            if (pojo.isStatus()) {
                                dialogView.viewSukses(AdminLokasiPegawaiActivity.this, pojo.getRemarks() != null ? pojo.getRemarks() : "Lokasi pegawai berhasil disimpan!");
                            } else {
                                dialogView.viewNotifKosong(AdminLokasiPegawaiActivity.this, "Gagal Simpan", pojo.getRemarks() != null ? pojo.getRemarks() : "Gagal menyimpan lokasi.");
                            }
                        } else {
                            dialogView.viewNotifKosong(AdminLokasiPegawaiActivity.this, "Gagal Server", "HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponsePOJO> call, Throwable t) {
                        dialogProses.dismiss();
                        dialogView.viewNotifKosong(AdminLokasiPegawaiActivity.this, "Koneksi Gagal", t.getMessage());
                    }
                });
    }
}
