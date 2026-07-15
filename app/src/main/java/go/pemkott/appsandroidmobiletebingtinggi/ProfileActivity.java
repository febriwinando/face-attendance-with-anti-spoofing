package go.pemkott.appsandroidmobiletebingtinggi;

import static go.pemkott.appsandroidmobiletebingtinggi.NewDashboard.DashboardVersiOne.dashboardVersiOne;
import static go.pemkott.appsandroidmobiletebingtinggi.utils.FileUtil.getDriveFilePath;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import go.pemkott.appsandroidmobiletebingtinggi.api.ApiAddProduk;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.ResponsePOJO;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetrofitBuilder;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.dinasluarkantor.tugaslapangan.TugasLapanganFinalActivity;
import go.pemkott.appsandroidmobiletebingtinggi.konstanta.AmbilFoto;
import go.pemkott.appsandroidmobiletebingtinggi.login.LoginActivity;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;
import go.pemkott.appsandroidmobiletebingtinggi.model.Updatep;
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
    TableRow trGantiPassword, trPanduanAplikasi;
    LinearLayout llKeluarProfil;
    CircleImageView civProfilPegawai;
    ImageView ivBack;
    SessionManager session;
    String userId;
    ApiAddProduk api;

    ImageView ivGantiProfil;
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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://absensi.tebingtinggikota.go.id/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        holderAPI = retrofit.create(HttpService.class);

        databaseHelper = new DatabaseHelper(this);

        dataEmployee();

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
                    .placeholder(R.drawable.profilpics)
                    .error(R.drawable.profilpics)
                    .into(civProfilPegawai);
        }

        // Nama aman
        if (sNama != null && sNama.length() > 14) {
            tvNamaPegawai.setText(
                    sNama.substring(0,14)+" ..."
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

        llKeluarProfil.setOnClickListener(v -> {
            peringatanKeluar();
        });

        trGantiPassword.setOnClickListener(v -> {
            gantiPassView();
        });

        ivBack.setOnClickListener(v -> {
            onBackPressed();
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

    }

    File fileUpdateProfile;
    AmbilFoto ambilFoto = new AmbilFoto(ProfileActivity.this);
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED){
            if (requestCode == 33 && resultCode == Activity.RESULT_OK && data != null){
                requestPermission();

                Uri selectedImageUri = data.getData();
                String FilePath2  = getDriveFilePath(selectedImageUri, ProfileActivity.this);

                File originalLampiran = new File(FilePath2);
                try {
                    fileUpdateProfile = ambilFoto.compressToFile(this, originalLampiran);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Bitmap preview = BitmapFactory.decodeFile(fileUpdateProfile.getAbsolutePath());
                civProfilPegawai.setImageBitmap(preview);

                kirimfotoprofile();
            }
        }


    }

    private MultipartBody.Part prepareFilePart(String partName, byte[] imageBytes) {
        RequestBody requestBody =
                RequestBody.create(
                        imageBytes,
                        MediaType.parse("image/jpeg")
                );

        return MultipartBody.Part.createFormData(
                partName,
                "profil.jpg",
                requestBody
        );
    }

    private RequestBody textPart(String value) {
        return RequestBody.create(
                okhttp3.MediaType.parse("text/plain"),
                value
        );
    }
    public void kirimfotoprofile(){
        Dialog dialogproses = new Dialog(ProfileActivity.this, R.style.DialogStyle);
        dialogproses.setContentView(R.layout.view_proses);
        dialogproses.setCancelable(false);

        byte[] imageBytes = ambilFoto.compressToMax80KB(fileUpdateProfile);
        MultipartBody.Part fotoPart =
                prepareFilePart("fototaging", imageBytes);

        Call<ResponsePOJO> call =
                RetroClient.getInstance().getApi().updateProfile(
                        fotoPart,
                        textPart(sEmployee_id)
                );

        call.enqueue(new Callback<ResponsePOJO>() {
            @Override
            public void onResponse(@NonNull Call<ResponsePOJO> call, @NonNull Response<ResponsePOJO> response) {

                dialogproses.dismiss();

                Log.d("PROFILE", "HTTP CODE : " + response.code());

                if (!response.isSuccessful()) {

                    try {

                        Log.e("UpdateFotoProfil",

                                "ERROR BODY : " + response.errorBody().string());

                    } catch (Exception e) {

                        e.printStackTrace();

                    }

                    dialogView.viewNotifKosong(

                            ProfileActivity.this,

                            "Gagal mengganti foto profil",

                            "HTTP : " + response.code()

                    );

                    return;

                }

                ResponsePOJO data = response.body();

                Log.d("UpdateFotoProfil", "BODY : " + new Gson().toJson(data));

                if (data != null && data.isStatus()) {

                    dialogView.viewSukses(

                            ProfileActivity.this,

                            data.getRemarks()

                    );

                } else {

                    dialogView.viewNotifKosong(

                            ProfileActivity.this,

                            data != null ? data.getRemarks() : "Response kosong",

                            ""

                    );

                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponsePOJO> call, @NonNull Throwable t) {
                dialogproses.dismiss();
                dialogView.pesanError(ProfileActivity.this);

            }
        });

        dialogproses.show();
    }

    private void requestPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            int REQUEST_CODE_ASK_PERMISSIONS = 123;
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
        }
    }
    public void dataEmployee(){

        Cursor res = databaseHelper.getAllData22(userId);

        if(res != null && res.moveToFirst()){

            do{

                sEmployee_id = res.getString(1) != null ?
                        res.getString(1) : "";

                sAkses = res.getString(3) != null ?
                        res.getString(3) : "";

                sActive = res.getString(4) != null ?
                        res.getString(4) : "";

                sToken = res.getString(5) != null ?
                        res.getString(5) : "";

            }
            while(res.moveToNext());

            res.close();
        }


        Cursor resa = databaseHelper.getDataEmployee(sEmployee_id);

        if(resa != null && resa.moveToFirst()){

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

    DialogView dialogView = new DialogView(ProfileActivity.this);
    public void gantiPassView(){
        Dialog dialogpassword = new Dialog(ProfileActivity.this, R.style.DialogStyle);
        dialogpassword.setContentView(R.layout.view_ganti_password);
        dialogpassword.setCancelable(false);


        EditText etPasswordSebelum = dialogpassword.findViewById(R.id.etPasswordSebelum);
        EditText etNewPassword = dialogpassword.findViewById(R.id.etNewPassword);
        EditText etEntryNewPassword = dialogpassword.findViewById(R.id.etEntryNewPassword);
        TextView btnGantiPass = dialogpassword.findViewById(R.id.btnGantiPass);
        TextView statuspass = dialogpassword.findViewById(R.id.statuspass);
        ImageView ivCloseGantiPass = dialogpassword.findViewById(R.id.ivCloseGantiPass);
        ProgressBar pbGantiPass = dialogpassword.findViewById(R.id.pbGantiPass);
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
                if (etPasswordSebelum.getText().toString().trim().isEmpty() )
                {
                    etPasswordSebelum.setError("Harap masukkan password lama.");

                } else if (etNewPassword.getText().toString().trim().isEmpty()) {
                    etNewPassword.setError("Harap masukkan password baru.");

                } else if (etEntryNewPassword.getText().toString().trim().isEmpty()) {
                    etEntryNewPassword.setError("Harap masukkan ulang password baru.");

                } else{
                    btnGantiPass.setVisibility(View.GONE);
                    pbGantiPass.setVisibility(View.VISIBLE);
                    Call<List<Updatep>> update = holderAPI.getUrlUpdatep("https://absensi.tebingtinggikota.go.id/api/updatep?"
                            +"employee_id="+sEmployee_id
                            +"&old="+etPasswordSebelum.getText()
                            +"&new="+etNewPassword.getText()
                    );

                    update.enqueue(new Callback<List<Updatep>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<Updatep>> call, @NonNull retrofit2.Response<List<Updatep>> response) {

                            List<Updatep> updateps = response.body();
                            if (!response.isSuccessful()){
                                btnGantiPass.setVisibility(View.VISIBLE);
                                pbGantiPass.setVisibility(View.GONE);
                                statuspass.setVisibility(View.VISIBLE);
                                statuspass.setText("Gagal mengganti password, mohon coba kembali!");
                                return;
                            }

                            for (Updatep updatep : updateps){
                                if (updatep.isStatus() == true){
                                    btnGantiPass.setVisibility(View.VISIBLE);
                                    pbGantiPass.setVisibility(View.GONE);
                                    dialogpassword.dismiss();
//                                    dialogView.viewSukses(ProfileActivity.this,"");
                                }else{
                                    btnGantiPass.setVisibility(View.VISIBLE);
                                    pbGantiPass.setVisibility(View.GONE);
                                    statuspass.setVisibility(View.VISIBLE);
                                    statuspass.setText("Password lama salah, periksa kembali!");
                                }
                            }




                        }

                        @Override
                        public void onFailure(Call<List<Updatep>> call, Throwable t) {
                            btnGantiPass.setVisibility(View.VISIBLE);
                            pbGantiPass.setVisibility(View.GONE);
                            statuspass.setVisibility(View.VISIBLE);
                            statuspass.setText("Gagal terhubung, mohon coba kembali!");

                        }
                    });

                }
            }
        });



        dialogpassword.show();
    }

    public void peringatanKeluar(){
        Dialog dataKosong = new Dialog(ProfileActivity.this, R.style.DialogStyle);
        dataKosong.setContentView(R.layout.view_warning_keluar);
        ImageView tvTutupDialog = dataKosong.findViewById(R.id.tvTutupDialog);
        TextView tvYakin = dataKosong.findViewById(R.id.tvYakin);

        tvTutupDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataKosong.dismiss();
            }
        });
        tvYakin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnKeluar();
                dataKosong.dismiss();
            }
        });

        dataKosong.show();
    }

    public void btnKeluar(){
        Dialog dialogproses = new Dialog(ProfileActivity.this, R.style.DialogStyle);
        dialogproses.setContentView(R.layout.view_proses);
        dialogproses.setCancelable(false);

        String token = session.getToken();

        api.logout(token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                session.clearSession();
                DatabaseHelper db = new DatabaseHelper(ProfileActivity.this);

                databaseHelper.deleteDataUseAll();
                databaseHelper.deleteDataEmployeeAll();
                databaseHelper.deleteDataKoordinatOPDAll();
                databaseHelper.deleteKegiatanIzin();
                databaseHelper.deleteTimeTableAll();
                databaseHelper.deleteDataKoordinatEmployeeAll();
                databaseHelper.deleteJamSift();
                databaseHelper.deleteJadwalSift2();
                session.clearSession();

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if(dashboardVersiOne != null){

                        dashboardVersiOne.finish();

                    }
                    Intent loginActivity = new Intent(ProfileActivity.this, LoginActivity.class);
                    loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginActivity);
                    finish();
                }, 3000);

            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dialogproses.show();
    }


}