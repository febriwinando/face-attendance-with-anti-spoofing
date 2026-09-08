package go.pemkott.appsandroidmobiletebingtinggi.izinshift;

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

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
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

import go.pemkott.appsandroidmobiletebingtinggi.NewDashboard.DashboardVersiOne;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.izinshift.izinshiftcuti.CutiShiftActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izinshift.izinshiftpribadi.KeperluanPribadiShiftActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izinshift.izinshiftsakit.SakitShiftActivity;
import go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;
import go.pemkott.appsandroidmobiletebingtinggi.model.JadwalSift;
import go.pemkott.appsandroidmobiletebingtinggi.model.WaktuSift;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class JadwalIzinShiftActivity extends AppCompatActivity {
    private RecyclerView rvJadwalSifting;
    private ArrayList<JadwalSift> listJadwalSift = new ArrayList<>();

    DatabaseHelper databaseHelper;
    ShimmerFrameLayout shimmerJadwalSift;

    static ArrayList<String> idJadwal = new ArrayList<String>();
    static ArrayList<String> employeJadwalSift = new ArrayList<String>();
    static ArrayList<String> idSiftJadwal = new ArrayList<String>();
    static ArrayList<String> tanggalJadwal = new ArrayList<String>();

    static ArrayList<String> idSift = new ArrayList<>();
    static ArrayList<String> opdSift = new ArrayList<String>();
    static ArrayList<String> tipeSift = new ArrayList<String>();
    static ArrayList<String> inisialSift = new ArrayList<String>();
    static ArrayList<String> masukSift = new ArrayList<String>();
    static ArrayList<String> pulangSift = new ArrayList<String>();
    String jam_masuk, jam_pulang;
    public static AppCompatActivity jadwalIzinSiftActivity ;
    HttpService holderAPI;
    ImageView ivSyncJadwalSift;
    String bulan = BULAN.format(new Date());
    String tahun = TAHUN.format(new Date());
    SessionManager session;

    String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_jadwal_izin_shift);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        session = new SessionManager(this);
        userId = session.getPegawaiId();

        jadwalIzinSiftActivity = this;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://absensi.tebingtinggikota.go.id/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        holderAPI = retrofit.create(HttpService.class);

        jam_masuk = DashboardVersiOne.jam_masuk;
        jam_pulang = DashboardVersiOne.jam_pulang;
        ImageView ivBack = findViewById(R.id.ivBack);
        ivSyncJadwalSift = findViewById(R.id.ivSyncJadwalSift);
        ivBack.setOnClickListener(view -> finish());
        TextView titleSift = findViewById(R.id.titleSift);
        rvJadwalSifting = findViewById(R.id.rvJadwalSifting);
        shimmerJadwalSift = findViewById(R.id.shimmerJadwalSift);
        databaseHelper = new DatabaseHelper(this);
        String title = "Jadwal "+bulan(BULAN.format(new Date()))+" "+TAHUN.format(new Date());
        titleSift.setText(title);
        ivSyncJadwalSift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unduhDataSiftOPD();
            }
        });
        getData(TimeFormat.ambilbulanjadwal(bulan), bulan, String.valueOf(Integer.parseInt(tahun)-1), tahun);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        findViewById(R.id.rlBackIzinSift).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    public void unduhDataSiftOPD(){
        databaseHelper.deleteJamShift();
        Call<List<WaktuSift>> jadwalSiftPegawai = holderAPI.getTestSift("https://absensi.tebingtinggikota.go.id/api/testsift?eOPD="+eOPD);
        jadwalSiftPegawai.enqueue(new Callback<List<WaktuSift>>() {
            @Override
            public void onResponse(@NonNull Call<List<WaktuSift>> call, @NonNull Response<List<WaktuSift>> response) {

                if (!response.isSuccessful()){
                    dialogView.viewNotifKosong(JadwalIzinShiftActivity.this, "Gagal mengunduh data, periksa koneksi internet anda dan coba kembali.", "");
                    return;
                }

                List<WaktuSift> waktuSifts = response.body();
                int jumlahdata = 0;
                for(WaktuSift waktuSift : waktuSifts){
                    databaseHelper.insertJamShift(String.valueOf(waktuSift.getId()), String.valueOf(waktuSift.getOpd_id()), String.valueOf(waktuSift.getTipe()), String.valueOf(waktuSift.getInisial()), String.valueOf(waktuSift.getMasuk()), String.valueOf(waktuSift.getPulang()));
                    jumlahdata += 1;
                }

                if (jumlahdata == waktuSifts.size()){
                    databaseHelper.insertLog(sEmployeID, eOPD, SIMPLE_FORMAT_TANGGAL.format(new Date()), "Sinkronisasi Master Jam Shift OPD (Izin)", "Sync");
                    unduhJadwalSift(1);
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<WaktuSift>> call, @NonNull Throwable t) {
                dialogView.viewNotifKosong(JadwalIzinShiftActivity.this, "Gagal mengunduh data, periksa koneksi internet anda dan coba kembali.", "");
            }
        });

    }
    String sEmployeID, sUsername, sAkses, sActive, eOPD, eKelompok, eJabatan, latOffice, lngOffice;

    public void getData(String bulansebelum, String bulan, String tahunsebelum, String tahun){


        idJadwal.clear();
        employeJadwalSift.clear();
        tanggalJadwal.clear();
        idSiftJadwal.clear();

        idSift.clear();
        opdSift.clear();
        tipeSift.clear();
        inisialSift.clear();
        masukSift.clear();
        pulangSift.clear();
        listJadwalSift.clear();

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

        Cursor resJadwalSift = databaseHelper.getJadwalShifts2(sEmployeID, bulansebelum, bulan, tahunsebelum, tahun);

        if (resJadwalSift.getCount() > 0){
            ivSyncJadwalSift.setVisibility(View.VISIBLE);
        }else{
            ivSyncJadwalSift.setVisibility(View.GONE);
        }

        while (resJadwalSift.moveToNext()){
            idJadwal.add(resJadwalSift.getString(0));
            employeJadwalSift.add(resJadwalSift.getString(1));
            idSiftJadwal.add(resJadwalSift.getString(2));
            tanggalJadwal.add(resJadwalSift.getString(3));
        }


        Cursor resJamSift = databaseHelper.getJamShift(eOPD);

        while (resJamSift.moveToNext()){
            idSift.add(resJamSift.getString(0));
            opdSift.add(resJamSift.getString(1));
            tipeSift.add(resJamSift.getString(2));
            inisialSift.add(resJamSift.getString(3));
            masukSift.add(resJamSift.getString(4));
            pulangSift.add(resJamSift.getString(5));

        }

        listJadwalSift.addAll(getJadwalShift());
        showRecyclerGrid();
        handlerProgressDialog();;
    }
    

    public void handlerProgressDialog() {

        shimmerJadwalSift.stopShimmer();
        shimmerJadwalSift.hideShimmer();
    }

    ArrayList<JadwalSift> getJadwalShift() {
        ArrayList<JadwalSift> listJadwal = new ArrayList<>();
        listJadwal.clear();

        for (int position = 0; position < idJadwal.size(); position++) {

            JadwalSift jadwalSift = new JadwalSift();
            jadwalSift.setId(idJadwal.get(position));
            jadwalSift.setEmployee_id(employeJadwalSift.get(position));
            jadwalSift.setShift_id(idSiftJadwal.get(position));
            jadwalSift.setTanggal(tanggalJadwal.get(position));
            listJadwal.add(jadwalSift);

        }
        return listJadwal;
    }

    ArrayList<WaktuSift> getJamShift() {
        ArrayList<WaktuSift> waktuSifts = new ArrayList<>();
        waktuSifts.clear();

        for (int position = 0; position < idSift.size(); position++) {

            WaktuSift waktuSift = new WaktuSift();
            waktuSift.setId(idSift.get(position));
            waktuSift.setOpd_id(opdSift.get(position));
            waktuSift.setTipe(tipeSift.get(position));
            waktuSift.setInisial(inisialSift.get(position));
            waktuSift.setMasuk(masukSift.get(position));
            waktuSift.setPulang(pulangSift.get(position));

            waktuSifts.add(waktuSift);
        }
        return waktuSifts;
    }

    DialogView dialogView = new DialogView(JadwalIzinShiftActivity.this);
    public static String inisialsift = null, tipesift = null, masuksift = null, pulangsift = null, idsift = null, tanggalSift = null;

    private void showRecyclerGrid(){
        rvJadwalSifting.setLayoutManager(new GridLayoutManager(this, 4));
        GridJadwalIzinShiftAdapter gridJadwal = new GridJadwalIzinShiftAdapter(JadwalIzinShiftActivity.this, listJadwalSift, getJamShift());
        rvJadwalSifting.setAdapter(gridJadwal);

        gridJadwal.setOnItemClickCallback(new GridJadwalIzinShiftAdapter.OnItemClickCallback() {
            @Override
            public void onItemClicked(String s) {
                tanggalSift = s;


                for (int i = 0; i < listJadwalSift.size(); i++) {
                    if (tanggalJadwal.get(i).equals(s)) {
                        for (int j = 0; j < idSift.size(); j++) {
                            if (idSiftJadwal.get(i).equals(idSift.get(j))) {
                                idsift = idSift.get(j);
                                inisialsift = inisialSift.get(j);
                                tipesift = tipeSift.get(j);
                                masuksift = masukSift.get(j);
                                pulangsift = pulangSift.get(j);

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
                        dialogView.viewNotifKosong(JadwalIzinShiftActivity.this, "Anda belum dapat melakukan absensi masuk untuk jadwal pada "+TimeFormat.formatBahasaIndonesia(s),"");
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
                                dialogView.viewNotifKosong(JadwalIzinShiftActivity.this, "Kami informasikan bahwa waktu absensi untuk shift malam pada "+TimeFormat.formatBahasaIndonesia(s)+" tersebut telah terlewati","");
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

    public static int jenisabsensi = 0;

    public void viewinfo(){

        Dialog dialoginfo = new Dialog(JadwalIzinShiftActivity.jadwalIzinSiftActivity, R.style.DialogStyle);
        dialoginfo.setContentView(R.layout.view_info_jadwal_izin_sift);
        dialoginfo.setCancelable(true);

        TextView txtKpSift = dialoginfo.findViewById(R.id.txtKpSift);
        TextView txtCutiSift = dialoginfo.findViewById(R.id.txtCutiSift);
        TextView txtSakitSift = dialoginfo.findViewById(R.id.txtSakitSift);
        TextView jamIzinSiftMasuk = dialoginfo.findViewById(R.id.jamIzinSiftMasuk);
        TextView jamIzinSiftPulang = dialoginfo.findViewById(R.id.jamIzinSiftPulang);
        ImageView ivTutupViewInfoSift = dialoginfo.findViewById(R.id.ivTutupViewInfoSift);

        jamIzinSiftMasuk.setText(masuksift);
        if (tipeSift.equals("malam")){
            jamIzinSiftPulang.setText(pulangsift+"\n"+tanggalSift);
        }else{
            jamIzinSiftPulang.setText(pulangsift);
        }

        txtKpSift.setOnClickListener(view -> {
            jenisabsensi = 9;
            Intent absensift = new Intent(JadwalIzinShiftActivity.this, KeperluanPribadiShiftActivity.class);
            startActivity(absensift);
        });

        txtCutiSift.setOnClickListener(view -> {
            jenisabsensi = 10;
            Intent absensift = new Intent(JadwalIzinShiftActivity.this, CutiShiftActivity.class);
            startActivity(absensift);
        });

        txtSakitSift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jenisabsensi = 11;
                Intent absensift = new Intent(JadwalIzinShiftActivity.this, SakitShiftActivity.class);
                startActivity(absensift);
            }
        });

        ivTutupViewInfoSift.setOnClickListener(view -> dialoginfo.dismiss());

        dialoginfo.show();

    }

    public void unduhJadwalSift(int status){
        Dialog dialogproses = new Dialog(JadwalIzinShiftActivity.this, R.style.DialogStyle);
        dialogproses.setContentView(R.layout.view_proses);
        dialogproses.setCancelable(false);

        if (status == 1){
            databaseHelper.deleteJadwalShift(sEmployeID, bulan, tahun);
        }

        Call<ArrayList<JadwalSift>> jadwalSiftPegawai = holderAPI.getJadwalSifts("https://absensi.tebingtinggikota.go.id/api/jadwalsift?ide="+sEmployeID+"&bulan="+bulan+"&tahun="+tahun);
        jadwalSiftPegawai.enqueue(new Callback<ArrayList<JadwalSift>>() {
            @Override
            public void onResponse(@NonNull Call<ArrayList<JadwalSift>> call, @NonNull Response<ArrayList<JadwalSift>> response) {
                dialogproses.dismiss();
                if (!response.isSuccessful()){
                    dialogView.viewNotifKosong(JadwalIzinShiftActivity.this, "Gagal mengunduh Jadwal Sift.","Silahkan coba kembali.");

                }

                ArrayList<JadwalSift> jadwalSifts = response.body();
                if (jadwalSifts.size() == 0){
                    dialogView.viewNotifKosong(JadwalIzinShiftActivity.this, "Jadwal belum tersedia, harap hubungi admin OPD anda.", "");
                }else{
                    int jlhJadwalSift = 0;
                    for(JadwalSift jadwalSift : jadwalSifts){
                        databaseHelper.insertJadwalShift(jadwalSift.getId(), jadwalSift.getEmployee_id(), jadwalSift.getShift_id(), jadwalSift.getTanggal());
                        jlhJadwalSift += 1;
                    }

                    if (jlhJadwalSift ==  jadwalSifts.size()){
                        databaseHelper.insertLog(sEmployeID, eOPD, SIMPLE_FORMAT_TANGGAL.format(new Date()), "Sinkronisasi Jadwal Shift Pegawai (Izin)", "Sync");
                        getData(TimeFormat.ambilbulanjadwal(bulan), bulan, String.valueOf(Integer.parseInt(tahun)-1), tahun);
                    }
                }


            }

            @Override
            public void onFailure(@NonNull Call<ArrayList<JadwalSift>> call, @NonNull Throwable t) {
                dialogproses.dismiss();
                dialogView.viewNotifKosong(JadwalIzinShiftActivity.this, "Gagal mengakses server.", "Silahkan coba kembali.");

            }
        });

        dialogproses.show();


    }
}