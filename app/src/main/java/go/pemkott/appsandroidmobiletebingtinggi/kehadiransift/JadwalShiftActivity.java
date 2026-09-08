package go.pemkott.appsandroidmobiletebingtinggi.kehadiransift;

import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.BULAN;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.SIMPLE_FORMAT_TANGGAL;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.TAHUN;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.bulan;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;
import go.pemkott.appsandroidmobiletebingtinggi.model.JadwalSift;
import go.pemkott.appsandroidmobiletebingtinggi.model.WaktuSift;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class JadwalShiftActivity extends AppCompatActivity {
    private RecyclerView rvJadwalShift;
    private ArrayList<JadwalSift> listJadwalShift = new ArrayList<>();
    private String title = "Mode List";
    DatabaseHelper databaseHelper;
    ShimmerFrameLayout shimmerJadwalShift;

    static ArrayList<String> idJadwal = new ArrayList<String>();
    static ArrayList<String> employeJadwalShift = new ArrayList<String>();
    static ArrayList<String> idShiftJadwal = new ArrayList<String>();
    static ArrayList<String> tanggalJadwal = new ArrayList<String>();

    static ArrayList<String> idShiftArray = new ArrayList<>();
    static ArrayList<String> opdShift = new ArrayList<String>();
    static ArrayList<String> tipeShiftList = new ArrayList<String>();
    static ArrayList<String> inisialShiftList = new ArrayList<String>();
    static ArrayList<String> masukShiftList = new ArrayList<String>();
    static ArrayList<String> pulangShiftList = new ArrayList<String>();
    public static String jam_masuk, jam_pulang;
    public static AppCompatActivity jadwalShiftActivity ;
    ImageView ivSyncJadwalShift;
    String bulan = BULAN.format(new Date());
    String tahun = TAHUN.format(new Date());
    HttpService holderAPI;

    public static String tanggalShift=  null, inisialShift = null, tipeShift = null, masukShift = null, pulangShift = null, idShift = null;
SessionManager session;
String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_jadwal_shift);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        session = new SessionManager(this);
        userId = session.getPegawaiId();


        jadwalShiftActivity = this;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://absensi.tebingtinggikota.go.id/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        holderAPI = retrofit.create(HttpService.class);
        Bundle intent = getIntent().getExtras();
        jam_masuk = intent.getString("jam_masuk");
        jam_pulang = intent.getString("jam_pulang");
        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView titleShift = findViewById(R.id.titleSift);
        ivSyncJadwalShift = findViewById(R.id.ivSyncJadwalSift);
        rvJadwalShift = findViewById(R.id.rvJadwalSifting);
        shimmerJadwalShift = findViewById(R.id.shimmerJadwalSift);
        databaseHelper = new DatabaseHelper(this);


        String title = "Jadwal "+bulan(BULAN.format(new Date()))+" "+TAHUN.format(new Date());
        titleShift.setText(title);

        ivSyncJadwalShift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unduhDataShiftOPD();
            }
        });

        getData(TimeFormat.ambilbulanjadwal(bulan), bulan, String.valueOf(Integer.parseInt(tahun)-1), tahun);
    }



    public void unduhDataShiftOPD(){
        databaseHelper.deleteJamShift();
        Call<List<WaktuSift>> jadwalShiftPegawai = holderAPI.getTestSift("https://absensi.tebingtinggikota.go.id/api/testsift?eOPD="+eOPD);
        jadwalShiftPegawai.enqueue(new Callback<List<WaktuSift>>() {
            @Override
            public void onResponse(@NonNull Call<List<WaktuSift>> call, @NonNull Response<List<WaktuSift>> response) {

                if (!response.isSuccessful()){
                    dialogView.viewNotifKosong(JadwalShiftActivity.this, "Gagal mengunduh data, periksa koneksi internet anda dan coba kembali.", "");
                    return;
                }

                List<WaktuSift> waktuShifts = response.body();
                int jumlahdata = 0;
                for(WaktuSift waktuShift : waktuShifts){
                    databaseHelper.insertJamShift(String.valueOf(waktuShift.getId()), String.valueOf(waktuShift.getOpd_id()), String.valueOf(waktuShift.getTipe()), String.valueOf(waktuShift.getInisial()), String.valueOf(waktuShift.getMasuk()), String.valueOf(waktuShift.getPulang()));
                    jumlahdata += 1;
                }

                if (jumlahdata == waktuShifts.size()){
                    databaseHelper.insertLog(sEmployeID, eOPD, SIMPLE_FORMAT_TANGGAL.format(new Date()), "Sinkronisasi Master Jam Shift OPD", "Sync");
                    unduhJadwalShift(1);
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<WaktuSift>> call, @NonNull Throwable t) {
                dialogView.viewNotifKosong(JadwalShiftActivity.this, "Gagal mengunduh data, periksa koneksi internet anda dan coba kembali.", "");
            }
        });

    }
    String sEmployeID, sUsername, sAkses, sActive, eOPD, eKelompok, eJabatan, latOffice, lngOffice;

    public void getData(String bulansebelum, String bulan, String tahunsebelum, String tahun){

        idJadwal.clear();
        employeJadwalShift.clear();
        tanggalJadwal.clear();
        idShiftJadwal.clear();

        idShiftArray.clear();
        opdShift.clear();
        tipeShiftList.clear();
        inisialShiftList.clear();
        masukShiftList.clear();
        pulangShiftList.clear();
        listJadwalShift.clear();

        Cursor res = databaseHelper.getAllData22(userId);
        if (res.getCount() == 0) {
            return;
        }

        while (res.moveToNext()) {
            sEmployeID = res.getString(1);
            sUsername = res.getString(2);
            sAkses = res.getString(2);
            sActive = res.getString(4);
        }

        Cursor employe = databaseHelper.getDataEmployee(sEmployeID);
        while (employe.moveToNext()){
            eOPD = employe.getString(4);
            eKelompok = employe.getString(9);
            eJabatan = employe.getString(11);
            latOffice = employe.getString(15);
            lngOffice = employe.getString(16);
        }

        Cursor resJadwalShift = databaseHelper.getJadwalShifts2(sEmployeID, bulansebelum, bulan, tahunsebelum, tahun);
        if (resJadwalShift.getCount() > 0){
            ivSyncJadwalShift.setVisibility(View.VISIBLE);
        }else{
            ivSyncJadwalShift.setVisibility(View.GONE);
        }

        while (resJadwalShift.moveToNext()){
            idJadwal.add(resJadwalShift.getString(0));
            employeJadwalShift.add(resJadwalShift.getString(1));
            idShiftJadwal.add(resJadwalShift.getString(2));
            tanggalJadwal.add(resJadwalShift.getString(3));
        }


        Cursor resJamShift = databaseHelper.getJamShift(eOPD);

        while (resJamShift.moveToNext()){
            idShiftArray.add(resJamShift.getString(0));
            opdShift.add(resJamShift.getString(1));
            tipeShiftList.add(resJamShift.getString(2));
            inisialShiftList.add(resJamShift.getString(3));
            masukShiftList.add(resJamShift.getString(4));
            pulangShiftList.add(resJamShift.getString(5));

        }

        listJadwalShift.addAll(getJadwalShift());
        showRecyclerGrid();
        handlerProgressDialog();;
    }

    public void handlerProgressDialog() {
        shimmerJadwalShift.stopShimmer();
        shimmerJadwalShift.hideShimmer();
    }

    ArrayList<JadwalSift> getJadwalShift() {
        ArrayList<JadwalSift> listJadwal = new ArrayList<>();
        listJadwal.clear();

        for (int position = 0; position < idJadwal.size(); position++) {

            JadwalSift jadwalSift = new JadwalSift();
            jadwalSift.setId(idJadwal.get(position));
            jadwalSift.setEmployee_id(employeJadwalShift.get(position));
            jadwalSift.setShift_id(idShiftJadwal.get(position));
            jadwalSift.setTanggal(tanggalJadwal.get(position));
            listJadwal.add(jadwalSift);
        }
        return listJadwal;
    }

    ArrayList<WaktuSift> getJamShift() {
        ArrayList<WaktuSift> waktuShifts = new ArrayList<>();
        waktuShifts.clear();

        for (int position = 0; position < idShiftArray.size(); position++) {

            WaktuSift waktuShift = new WaktuSift();
            waktuShift.setId(idShiftArray.get(position));
            waktuShift.setOpd_id(opdShift.get(position));
            waktuShift.setTipe(tipeShiftList.get(position));
            waktuShift.setInisial(inisialShiftList.get(position));
            waktuShift.setMasuk(masukShiftList.get(position));
            waktuShift.setPulang(pulangShiftList.get(position));

            waktuShifts.add(waktuShift);
        }
        return waktuShifts;
    }

    DialogView dialogView = new DialogView(this);
    private void showRecyclerGrid(){
        rvJadwalShift.setLayoutManager(new GridLayoutManager(this, 4));
        GridJadwalShiftAdapter gridJadwal = new GridJadwalShiftAdapter(JadwalShiftActivity.this, listJadwalShift, getJamShift());
        rvJadwalShift.setAdapter(gridJadwal);

        gridJadwal.setOnItemClickCallback(new GridJadwalShiftAdapter.OnItemClickCallback() {
            @Override
            public void onItemClicked(String s) {

                tanggalShift = s;
//                Log.d("ABSEN_MASUK_PAGI", s);
                for (int i=0 ;i<listJadwalShift.size();i++){
                    if (tanggalJadwal.get(i).equals(s)){
                        for (int j = 0 ; j< idShiftArray.size();j++){
                            if (idShiftJadwal.get(i).equals(idShiftArray.get(j))){
                                idShift = idShiftArray.get(j);
                                inisialShift = inisialShiftList.get(j);
                                tipeShift = tipeShiftList.get(j);
                                masukShift = masukShiftList.get(j);
                                pulangShift = pulangShiftList.get(j);
                            }
                        }
                    }
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                // tanggal hari ini
                Date today = null;
                try {
                    today = sdf.parse(sdf.format(new Date()));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                // tanggal yang mau dibandingkan
                Date targetDate = null;
                try {
                    targetDate = sdf.parse(s);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                if (targetDate.after(today)) {
                    try {
                        dialogView.viewNotifKosong(JadwalShiftActivity.this, "Anda belum dapat melakukan absensi masuk untuk jadwal pada "+TimeFormat.formatBahasaIndonesia(s),"");
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }

                } else if (targetDate.before(today)) {

                    try {
                        Date tanggalDB = SIMPLE_FORMAT_TANGGAL.parse(s);
                        Date harinini = SIMPLE_FORMAT_TANGGAL.parse(
                                SIMPLE_FORMAT_TANGGAL.format(new Date())
                        );

                        long diffDay = (harinini.getTime() - tanggalDB.getTime())
                                / (1000 * 60 * 60 * 24);

                        if (diffDay == 0) {
                            viewinfo();
                        }else if (diffDay == 1){

                            Calendar calendar = Calendar.getInstance();
                            int currentHour = calendar.get(Calendar.HOUR_OF_DAY); // 0–23

                            if (currentHour >= 22) {
                                dialogView.viewNotifKosong(JadwalShiftActivity.this, "Kami informasikan bahwa waktu absensi untuk shift malam pada "+TimeFormat.formatBahasaIndonesia(s)+" tersebut telah terlewati","");
                            } else {
                                viewinfo();
                            }

                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } else {
                    viewinfo();
                }
            }
        });


    }


    public void viewinfo(){

        Dialog dialoginfo = new Dialog(JadwalShiftActivity.this, R.style.DialogStyle);
        dialoginfo.setContentView(R.layout.view_info_jadwal_sift);
        dialoginfo.setCancelable(true);

        TextView txtAbsen = dialoginfo.findViewById(R.id.txtAbsen);
        TextView jamShiftMasuk = dialoginfo.findViewById(R.id.jamSiftMasuk);
        TextView jamShiftPulang = dialoginfo.findViewById(R.id.jamSiftPulang);
        ImageView ivTutupViewInfoShift = dialoginfo.findViewById(R.id.ivTutupViewInfoSift);

        jamShiftMasuk.setText(masukShift);
        if (tipeShift != null && tipeShift.equals("malam")){
            jamShiftPulang.setText(pulangShift+"\n"+tanggalShift);
        }else{
            jamShiftPulang.setText(pulangShift);
        }

        txtAbsen.setOnClickListener(view -> {
            Intent absenshift = new Intent(JadwalShiftActivity.this, databaseHelper.getCameraActivityClass());
            absenshift.putExtra("aktivitas", "kehadiransift");
            startActivity(absenshift);
        });

        ivTutupViewInfoShift.setOnClickListener(view -> dialoginfo.dismiss());
        dialoginfo.show();

    }

    public void unduhJadwalShift(int status){
        Dialog dialogproses = new Dialog(JadwalShiftActivity.this, R.style.DialogStyle);
        dialogproses.setContentView(R.layout.view_proses);
        dialogproses.setCancelable(false);

        if (status == 1){
            databaseHelper.deleteJadwalShift(sEmployeID, bulan, tahun);
        }

        Call<ArrayList<JadwalSift>> jadwalShiftPegawai = holderAPI.getJadwalSifts("https://absensi.tebingtinggikota.go.id/api/jadwalsift?ide="+sEmployeID+"&bulan="+bulan+"&tahun="+tahun);
        jadwalShiftPegawai.enqueue(new Callback<ArrayList<JadwalSift>>() {
            @Override
            public void onResponse(@NonNull Call<ArrayList<JadwalSift>> call, @NonNull Response<ArrayList<JadwalSift>> response) {
                if (!response.isSuccessful()){
                    dialogView.viewNotifKosong(JadwalShiftActivity.this, "Gagal mengunduh Jadwal Sift.","Silahkan coba kembali.");
                    dialogproses.dismiss();
                    return;
                }

                ArrayList<JadwalSift> jadwalShifts = response.body();
                if (jadwalShifts.size() == 0){
                    dialogproses.dismiss();

                    dialogView.viewNotifKosong(JadwalShiftActivity.this, "Jadwal belum tersedia, harap hubungi admin OPD anda.", "");
                }else{
                    dialogproses.dismiss();

                    int jlhJadwalShift = 0;
                    for(JadwalSift jadwalShift : jadwalShifts){
                        databaseHelper.insertJadwalShift(jadwalShift.getId(), jadwalShift.getEmployee_id(), jadwalShift.getShift_id(), jadwalShift.getTanggal());
                        jlhJadwalShift += 1;
                    }

                    if (jlhJadwalShift ==  jadwalShifts.size()){
                        databaseHelper.insertLog(sEmployeID, eOPD, SIMPLE_FORMAT_TANGGAL.format(new Date()), "Sinkronisasi Jadwal Shift Pegawai", "Sync");
                        getData(TimeFormat.ambilbulanjadwal(bulan), bulan, String.valueOf(Integer.parseInt(tahun)-1), tahun);;
                    }
                }


            }

            @Override
            public void onFailure(@NonNull Call<ArrayList<JadwalSift>> call, @NonNull Throwable t) {
                dialogproses.dismiss();
                dialogView.viewNotifKosong(JadwalShiftActivity.this, "Gagal mengakses server.", "Silahkan coba kembali.");

            }
        });


        dialogproses.show();
    }

}