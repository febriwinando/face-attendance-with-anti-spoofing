package go.pemkott.appsandroidmobiletebingtinggi;

import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.SIMPLE_FORMAT_TANGGAL;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.ApiAddProduk;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.ResponsePOJO;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetrofitBuilder;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.model.DataEmployee;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.konstanta.AmbilFoto;
import go.pemkott.appsandroidmobiletebingtinggi.login.LoginActivity;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;
import go.pemkott.appsandroidmobiletebingtinggi.policy.PanduanAplikasiActivity;
import go.pemkott.appsandroidmobiletebingtinggi.policy.PrivacyPolicyActivity;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileActivity extends AppCompatActivity {
    RelativeLayout backHome;
    HttpService holderAPI;
    DatabaseHelper databaseHelper;
    String sNama, sNIP, sEmail, sNoHpEmployee, sKelompok, sJabatan, sKantor, sKeluar, sOPD;
    TextView tvNamaPegawai, tvNip, tvEmailEmploy, tvNoHpEmploye, tvKelompokEmploy, tvJabatanEmploy, tvDinasEmploy;
    String fotoProfileUpdate;

    String sEmployee_id, sAkses, sActive, sToken, fotoProfile;
    LinearLayout trGantiPassword, trPanduanAplikasi;
    MaterialButton llKeluarProfil;
    CircleImageView civProfilPegawai;
    ImageView ivBack;
    SessionManager session;
    String userId;
    ApiAddProduk api;
    MaterialButton llBukaDaftarPegawai;
    ImageView ivGantiProfil;
    long profileCacheBuster = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        session = new SessionManager(this);
        userId = session.getPegawaiId();

        api = RetrofitBuilder.getClient().create(ApiAddProduk.class);

        civProfilPegawai = findViewById(R.id.civProfilPegawai);
        tvNamaPegawai = findViewById(R.id.tvNamaPegawai);
        tvNip = findViewById(R.id.tvNip);

        tvEmailEmploy = findViewById(R.id.tvEmailEmploy);
        tvNoHpEmploye = findViewById(R.id.tvNoHpEmploye);
        tvKelompokEmploy = findViewById(R.id.tvKelompokEmploy);
        tvJabatanEmploy = findViewById(R.id.tvJabatanEmploy);
        tvDinasEmploy = findViewById(R.id.tvDinasEmploy);
        ivGantiProfil = findViewById(R.id.ivGantiProfil);

        llKeluarProfil = findViewById(R.id.llKeluarProfil);
        trGantiPassword = findViewById(R.id.trGantiPassword);
        ivBack = findViewById(R.id.ivBack);
        llBukaDaftarPegawai = findViewById(R.id.llBukaDaftarPegawai);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://absensi.tebingtinggikota.go.id/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        holderAPI = retrofit.create(HttpService.class);

        databaseHelper = new DatabaseHelper(this);

        dataEmployee();
        displayEmployeeData();

        llKeluarProfil.setOnClickListener(v -> {
            peringatanKeluar();
        });

        trGantiPassword.setOnClickListener(v -> {
            gantiPassView();
        });

        trPanduanAplikasi = findViewById(R.id.trPanduanAplikasi);
        trPanduanAplikasi.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, PanduanAplikasiActivity.class);
            startActivity(intent);
        });

        LinearLayout trPrivasi = findViewById(R.id.trPrivasi);
        trPrivasi.setOnClickListener(v -> showPrivacyPolicy());

        ivBack.setOnClickListener(v -> {
            finish();
        });

        ivGantiProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Pilih Gambar"), 33);
            }
        });

        if ("199402092020121005".equalsIgnoreCase(sNIP)){
            llBukaDaftarPegawai.setVisibility(View.VISIBLE);
        }

        llBukaDaftarPegawai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent rahasia = new Intent(ProfileActivity.this, EmployeeSyncActivity.class);
                startActivity(rahasia);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 33 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Dialog dialogproses = new Dialog(ProfileActivity.this, R.style.DialogStyle);
            dialogproses.setContentView(R.layout.view_proses);
            dialogproses.setCancelable(false);
            dialogproses.show();

            MultipartBody.Part imagePart = AmbilFoto.prepareFilePart(this, "fototaging", data.getData());
            RequestBody employeeIdPart = textPart(sEmployee_id);

            api.updateProfile(imagePart, employeeIdPart).enqueue(new Callback<ResponsePOJO>() {
                @Override
                public void onResponse(@NonNull Call<ResponsePOJO> call, @NonNull Response<ResponsePOJO> response) {
                    Log.d("UPDATE_PROFILE", "Response Code: " + response.code());
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ResponsePOJO body = response.body();
                        Log.d("UPDATE_PROFILE", "Status: " + body.isStatus());
                        Log.d("UPDATE_PROFILE", "Remarks/Message: " + body.getRemarks());

                        if (body.isStatus()) {
                            Toast.makeText(ProfileActivity.this, "Berhasil Update Profil", Toast.LENGTH_SHORT).show();
                            syncDataEmployee(dialogproses);
                        } else {
                            dialogproses.dismiss();
                            String pesan = body.getRemarks() != null ? body.getRemarks() : "Gagal Update Profil (Status False)";
                            Toast.makeText(ProfileActivity.this, pesan, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        dialogproses.dismiss();
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                            Log.e("UPDATE_PROFILE", "Error Body: " + errorBody);
                        } catch (Exception e) {
                            Log.e("UPDATE_PROFILE", "Error reading error body", e);
                        }
                        Toast.makeText(ProfileActivity.this, "Gagal Update Profil (Server Error)", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponsePOJO> call, @NonNull Throwable t) {
                    dialogproses.dismiss();
                    Log.e("UPDATE_PROFILE", "Failure: " + t.getMessage(), t);
                    Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void syncDataEmployee(Dialog dialogproses) {
        api.dataEmployee(sEmployee_id).enqueue(new Callback<DataEmployee>() {
            @Override
            public void onResponse(@NonNull Call<DataEmployee> call, @NonNull Response<DataEmployee> response) {
                dialogproses.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    DataEmployee de = response.body();
                    databaseHelper.deleteDataEmployeeAll();
                    databaseHelper.insertDataEmployee(
                            de.getId(), de.getAtasan_id1(), de.getAtasan_id2(), de.getPosition_id(), de.getOpd_id(),
                            de.getNip(), de.getNama(), de.getEmail(), de.getNo_hp(), de.getKelompok(), de.getS_jabatan(),
                            de.getEselon(), de.getJabatan(), de.getOpd(), de.getAlamat(), de.getLet(), de.getLng(),
                            de.getFoto(), de.getAwal_waktu(), String.valueOf(de.getShift())
                    );

                    profileCacheBuster = System.currentTimeMillis();
                    dataEmployee();
                    displayEmployeeData();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DataEmployee> call, @NonNull Throwable t) {
                dialogproses.dismiss();
                dataEmployee();
                displayEmployeeData();
            }
        });
    }

    private void displayEmployeeData() {
        // Foto profil aman
        if (fotoProfile == null ||
                fotoProfile.trim().isEmpty() ||
                "null".equals(fotoProfile)) {

            Glide.with(this)
                    .load(R.drawable.profilpics)
                    .into(civProfilPegawai);

        } else {

            Glide.with(this)
                    .load("https://absensi.tebingtinggikota.go.id/storage/foto-pegawai/" + fotoProfile)
                    .signature(new ObjectKey(profileCacheBuster))
                    .placeholder(R.drawable.profilpics)
                    .error(R.drawable.profilpics)
                    .into(civProfilPegawai);
        }

        // Nama aman
        if (sNama != null && sNama.length() > 13) {
            tvNamaPegawai.setText(
                    sNama.substring(0, 13) + "..."
            );
        } else {
            tvNamaPegawai.setText(
                    sNama != null ? sNama : ""
            );
        }

        tvNip.setText(sNIP != null ? sNIP : "");
        tvEmailEmploy.setText(sEmail != null ? sEmail : "");
        tvNoHpEmploye.setText(sNoHpEmployee != null ? sNoHpEmployee : "");
        tvKelompokEmploy.setText(sKelompok != null ? sKelompok : "");
        tvJabatanEmploy.setText(sJabatan != null ? sJabatan : "");
        tvDinasEmploy.setText(sKantor != null ? sKantor : "");
    }

    private RequestBody textPart(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    public void dataEmployee(){
        Cursor resa = databaseHelper.getAllData22(userId);
        if (resa.getCount() == 0) {
            return;
        }

        while (resa.moveToNext()) {
            sEmployee_id = resa.getString(1);
            sAkses = resa.getString(2);
            sActive = resa.getString(4);
            sToken = resa.getString(5);
        }

        resa.close();

        resa = databaseHelper.getDataEmployee(sEmployee_id);
        if(resa.getCount() != 0)
        {
            resa.moveToFirst();
            do{

                sNIP = resa.getString(5) != null ?
                        resa.getString(5) : "";

                sNama = resa.getString(6) != null ?
                        resa.getString(6) : "";

                sEmail = resa.getString(7) != null ?
                        resa.getString(7) : "";

                sNoHpEmployee = resa.getString(8) != null ?
                        resa.getString(8) : "";

                sOPD = resa.getString(4) != null ?
                        resa.getString(4) : "";

                String statusPegawai =
                        resa.getString(9);

                if(!"pns".equals(statusPegawai)){
                    sKelompok = "Non-PNS";
                }else{
                    sKelompok = "Pegawai Negeri Sipil";
                }

                sJabatan = resa.getString(12) != null ?
                        resa.getString(12) : "";

                sKantor = resa.getString(13) != null ?
                        resa.getString(13) : "";

                fotoProfile = resa.getString(17);

                fotoProfileUpdate = fotoProfile;

            }
            while(resa.moveToNext());

            resa.close();

        }else{

            sNIP="";
            sNama="";
            sEmail="";
            sNoHpEmployee="";
            sKelompok="";
            sJabatan="";
            sKantor="";
            fotoProfile="";
        }

    }


    public void gantiPassView(){
        Dialog dialogpassword = new Dialog(ProfileActivity.this, R.style.DialogStyle);
        dialogpassword.setContentView(R.layout.view_ganti_password);
        dialogpassword.setCancelable(false);


        EditText etPasswordSebelum = dialogpassword.findViewById(R.id.etPasswordSebelum);
        EditText etNewPassword = dialogpassword.findViewById(R.id.etNewPassword);
        EditText etEntryNewPassword = dialogpassword.findViewById(R.id.etEntryNewPassword);
        TextView btnGantiPass = dialogpassword.findViewById(R.id.btnGantiPass);
        TextView statuspass = dialogpassword.findViewById(R.id.statuspass);
        View ivCloseGantiPass = dialogpassword.findViewById(R.id.ivCloseGantiPass);
        etEntryNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().trim().isEmpty()){
                    if (etNewPassword.getText().toString().trim().equals(etEntryNewPassword.getText().toString().trim())){
                        statuspass.setVisibility(View.GONE);
                    }else{
                        statuspass.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ivCloseGantiPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogpassword.dismiss();
            }
        });
        btnGantiPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etPasswordSebelum.getText().toString().isEmpty() || etNewPassword.getText().toString().isEmpty() || etEntryNewPassword.getText().toString().isEmpty()){
                    Toast.makeText(ProfileActivity.this, "Anda harus mengisi data password terlebih dahulu.", Toast.LENGTH_SHORT).show();
                }else{
                    if (etNewPassword.getText().toString().equals(etEntryNewPassword.getText().toString())){
                        // Fitur ganti password belum tersedia di API saat ini
                        Toast.makeText(ProfileActivity.this, "Fitur ganti password segera hadir.", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(ProfileActivity.this, "Password baru tidak sama.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        dialogpassword.show();
    }

    private void peringatanKeluar(){
        Dialog dialogperingatan = new Dialog(ProfileActivity.this, R.style.DialogStyle);
        dialogperingatan.setContentView(R.layout.view_warning_keluar);
        dialogperingatan.setCancelable(true);

        View tvTutupDialog = dialogperingatan.findViewById(R.id.tvTutupDialog);
        TextView tvYakin = dialogperingatan.findViewById(R.id.tvYakin);

        tvTutupDialog.setOnClickListener(v -> dialogperingatan.dismiss());
        tvYakin.setOnClickListener(v -> {
            dialogperingatan.dismiss();
            btnKeluar();
        });

        dialogperingatan.show();
    }

    public void btnKeluar(){
        Dialog dialogproses = new Dialog(ProfileActivity.this, R.style.DialogStyle);
        dialogproses.setContentView(R.layout.view_proses);
        dialogproses.setCancelable(false);
        dialogproses.show();

        api.logout(sToken).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                databaseHelper.deleteAllDataUser();
                databaseHelper.deleteDataTableInfoClearUser(sEmployee_id);
                session.saveToken(null);
                session.savePegawaiId(null);
                session.saveEmployeeId(null);
                dialogproses.dismiss();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent loginActivity = new Intent(ProfileActivity.this, LoginActivity.class);
                        loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(loginActivity);
                        finish();
                    }
                }, 1000);

            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                dialogproses.dismiss();
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // Tetap logout lokal jika gagal koneksi
                databaseHelper.deleteAllDataUser();
                session.saveToken(null);
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void showPrivacyPolicy() {
        Intent intent = new Intent(ProfileActivity.this, PrivacyPolicyActivity.class);
        startActivity(intent);
    }
}
