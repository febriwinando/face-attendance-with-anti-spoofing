package go.pemkott.appsandroidmobiletebingtinggi.singkronjadwalsift;

import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.BULAN;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.SIMPLE_FORMAT_JAM;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.SIMPLE_FORMAT_TANGGAL;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.TAHUN;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.makeDateString;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;
import go.pemkott.appsandroidmobiletebingtinggi.model.JadwalSift;
import go.pemkott.appsandroidmobiletebingtinggi.model.WaktuSift;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CalendarJadwalShiftActivity extends AppCompatActivity {
    private RecyclerView rvJadwalSifting;
    private final ArrayList<JadwalSift> listJadwalSift = new ArrayList<>();
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
    public static AppCompatActivity jadwalSiftActivity ;
    Dialog monthYearPickerDialog;
    TextView titleCalendarSift;
    LinearLayout llPilihBulan;
    ImageView ivUnduhJadwalSift, ivSyncJadwalSift;
    HttpService holderAPI;
    String bulan = BULAN.format(new Date());
    String tahun = TAHUN.format(new Date());

    SessionManager session;
    String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar_jadwal_shift);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        session = new SessionManager(this);
        userId = session.getPegawaiId();


        jadwalSiftActivity = this;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://absensi.tebingtinggikota.go.id/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        holderAPI = retrofit.create(HttpService.class);


        ImageView ivBack = findViewById(R.id.ivBack);
        titleCalendarSift = findViewById(R.id.titleCalendarSift);
        rvJadwalSifting = findViewById(R.id.rvJadwalSifting);
        llPilihBulan = findViewById(R.id.llPilihBulan);
        rvJadwalSifting.setHasFixedSize(true);
        shimmerJadwalSift = findViewById(R.id.shimmerJadwalSift);
        ivUnduhJadwalSift = findViewById(R.id.ivUnduhJadwalSift);
        ivSyncJadwalSift = findViewById(R.id.ivSyncJadwalSift);

        databaseHelper = new DatabaseHelper(this);
        String date = "Jadwal " + makeDateString(Integer.parseInt(tahun), Integer.parseInt(bulan));
        titleCalendarSift.setText(date);

        llPilihBulan.setOnClickListener(v -> showMonthYearPicker());

        ivUnduhJadwalSift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unduhJadwalSift(0);
            }
        });


        ivSyncJadwalSift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unduhJadwalSift(1);
            }
        });



        jam_masuk = null;
        jam_pulang = null;
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        getData(bulan, tahun);
    }

    private void showMonthYearPicker() {
        monthYearPickerDialog = new Dialog(this, R.style.DialogStyle);
        monthYearPickerDialog.setContentView(R.layout.dialog_month_year_picker);
        monthYearPickerDialog.setCancelable(true);

        NumberPicker pickerMonth = monthYearPickerDialog.findViewById(R.id.picker_month);
        NumberPicker pickerYear = monthYearPickerDialog.findViewById(R.id.picker_year);
        TextView btnCancel = monthYearPickerDialog.findViewById(R.id.btn_cancel);
        TextView btnConfirm = monthYearPickerDialog.findViewById(R.id.btn_confirm);

        String[] monthNames = new String[]{"JAN", "FEB", "MAR", "APR", "MEI", "JUN", "JUL", "AGU", "SEP", "OKT", "NOV", "DES"};
        pickerMonth.setMinValue(1);
        pickerMonth.setMaxValue(12);
        pickerMonth.setDisplayedValues(monthNames);
        pickerMonth.setValue(Integer.parseInt(bulan));

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        pickerYear.setMinValue(currentYear - 1);
        pickerYear.setMaxValue(currentYear + 5);
        pickerYear.setValue(Integer.parseInt(tahun));

        btnCancel.setOnClickListener(v -> monthYearPickerDialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            int selectedMonth = pickerMonth.getValue();
            int selectedYear = pickerYear.getValue();

            if (selectedMonth >= 10) {
                this.bulan = String.valueOf(selectedMonth);
            } else {
                this.bulan = "0" + selectedMonth;
            }
            this.tahun = String.valueOf(selectedYear);

            String dateText = "Jadwal " + makeDateString(selectedYear, selectedMonth);
            titleCalendarSift.setText(dateText);

            getData(this.bulan, this.tahun);
            monthYearPickerDialog.dismiss();
        });

        monthYearPickerDialog.show();
    }

    String dariTanggal;

    String sEmployeID, sUsername, sAkses, sActive, eOPD, eKelompok, eJabatan, latOffice, lngOffice;

    public void getData(String bulan, String tahun){


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

        Cursor resJadwalSift = databaseHelper.getJadwalSiftsCalendar(sEmployeID, bulan, tahun);

        if (resJadwalSift.getCount() > 0){
            ivSyncJadwalSift.setVisibility(View.VISIBLE);
            ivUnduhJadwalSift.setVisibility(View.GONE);
        }else{
            ivSyncJadwalSift.setVisibility(View.GONE);
            ivUnduhJadwalSift.setVisibility(View.VISIBLE);
        }

        while (resJadwalSift.moveToNext()){
            idJadwal.add(resJadwalSift.getString(0));
            employeJadwalSift.add(resJadwalSift.getString(1));
            idSiftJadwal.add(resJadwalSift.getString(2));
            tanggalJadwal.add(resJadwalSift.getString(3));
        }


        Cursor resJamSift = databaseHelper.getJamSift(eOPD);

        while (resJamSift.moveToNext()){
            idSift.add(resJamSift.getString(0));
            opdSift.add(resJamSift.getString(1));
            tipeSift.add(resJamSift.getString(2));
            inisialSift.add(resJamSift.getString(3));
            masukSift.add(resJamSift.getString(4));
            pulangSift.add(resJamSift.getString(5));

        }

        listJadwalSift.addAll(getJadwalSift());
        showRecyclerGrid();
        handlerProgressDialog();
    }

    public void handlerProgressDialog() {

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            shimmerJadwalSift.stopShimmer();
            shimmerJadwalSift.hideShimmer();


        }, 500);
    }

    ArrayList<JadwalSift> getJadwalSift() {
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

    ArrayList<WaktuSift> getJamSift() {
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

    DialogView dialogView = new DialogView(this);
    private void showRecyclerGrid(){
        rvJadwalSifting.setLayoutManager(new GridLayoutManager(this, 4));
        GridCalendarJadwalShiftAdapter gridJadwal = new GridCalendarJadwalShiftAdapter(CalendarJadwalShiftActivity.this, listJadwalSift, getJamSift(), bulan, tahun);
        rvJadwalSifting.setAdapter(gridJadwal);

        gridJadwal.setOnItemClickCallback(s -> {


            String inisialsift = null, tipesift = null, masuksift = null, pulangsift = null, idsift = null;

            for (int i=0 ;i<listJadwalSift.size();i++){
                if (tanggalJadwal.get(i).equals(s)){
                    for (int j = 0 ; j<idSift.size();j++){
                        if (idSiftJadwal.get(i).equals(idSift.get(j))){
                            idsift = idSift.get(j);
                            inisialsift = inisialSift.get(j);
                            tipesift = tipeSift.get(j);
                            masuksift = masukSift.get(j);
                            pulangsift = pulangSift.get(j);

                        }
                    }
                }
            }
            String tanggal = SIMPLE_FORMAT_TANGGAL.format(new Date());
            Date hariini = null;
            try {
                hariini = SIMPLE_FORMAT_TANGGAL.parse(tanggal);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(hariini);
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            Date newDate = calendar.getTime();
            String infoJadwalhariini = SIMPLE_FORMAT_TANGGAL.format(newDate);

            String jamSekarangString = SIMPLE_FORMAT_JAM.format(new Date());
            Date jamSekarang = null, batasJamAbsenMalam = null, jadwalAbsensetelah = null;
            try {
                batasJamAbsenMalam = SIMPLE_FORMAT_JAM.parse("12:00");
                jamSekarang = SIMPLE_FORMAT_JAM.parse(jamSekarangString);
                jadwalAbsensetelah = SIMPLE_FORMAT_TANGGAL.parse(s);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            viewinfo(CalendarJadwalShiftActivity.this, s, inisialsift, tipesift, masuksift, pulangsift, idsift);
        });


    }


    public void viewinfo(Context context, String tanggal, String inisial, String sift, String masuk, String pulang, String idsift){

        Dialog dialoginfo = new Dialog(context, R.style.DialogStyle);
        dialoginfo.setContentView(R.layout.view_info_calendar_jadwal_sift);
        dialoginfo.setCancelable(false);

        TextView jamSiftMasuk = dialoginfo.findViewById(R.id.jamSiftMasuk);
        TextView jamSiftPulang = dialoginfo.findViewById(R.id.jamSiftPulang);
        ImageView ivTutupViewInfoSift = dialoginfo.findViewById(R.id.ivTutupViewInfoSift);
        View btnTutupInfo = dialoginfo.findViewById(R.id.btnTutupInfo);

        jamSiftMasuk.setText(masuk);
        if (sift != null && sift.equals("malam")){
            jamSiftPulang.setText(String.format("%s\n%s", pulang, tanggal));
        }else{
            jamSiftPulang.setText(pulang);
        }

        ivTutupViewInfoSift.setOnClickListener(view -> dialoginfo.dismiss());
        btnTutupInfo.setOnClickListener(view -> dialoginfo.dismiss());
        dialoginfo.show();

    }

    public void unduhJadwalSift(int status){
        Dialog dialogproses = new Dialog(CalendarJadwalShiftActivity.this, R.style.DialogStyle);
        dialogproses.setContentView(R.layout.view_proses);
        dialogproses.setCancelable(false);

        if (status == 1){
            databaseHelper.deleteJadwalSift(sEmployeID, bulan, tahun);
        }

        Call<ArrayList<JadwalSift>> jadwalSiftPegawai = holderAPI.getJadwalSifts("https://absensi.tebingtinggikota.go.id/api/jadwalsift?ide="+sEmployeID+"&bulan="+bulan+"&tahun="+tahun);
        jadwalSiftPegawai.enqueue(new Callback<ArrayList<JadwalSift>>() {

            @Override
            public void onResponse(@NonNull Call<ArrayList<JadwalSift>> call, @NonNull Response<ArrayList<JadwalSift>> response) {
                if (!response.isSuccessful()){
                    dialogView.viewNotifKosong(CalendarJadwalShiftActivity.this, "Gagal mengunduh Jadwal Sift.","Silahkan coba kembali.");
                    dialogproses.dismiss();
                }

                ArrayList<JadwalSift> jadwalSifts = response.body();
                if (jadwalSifts.size() == 0){
                    dialogView.viewNotifKosong(CalendarJadwalShiftActivity.this, "Jadwal belum tersedia, harap hubungi admin OPD anda.", "");
                    dialogproses.dismiss();

                }else{
                    int jlhJadwalSift = 0;
                    for(JadwalSift jadwalSift : jadwalSifts){
                        databaseHelper.insertJadwalSift(jadwalSift.getId(), jadwalSift.getEmployee_id(), jadwalSift.getShift_id(), jadwalSift.getTanggal());
                        jlhJadwalSift += 1;
                    }

                    if (jlhJadwalSift ==  jadwalSifts.size()){
                        databaseHelper.insertLog(sEmployeID, eOPD, SIMPLE_FORMAT_TANGGAL.format(new Date()), "Sinkronisasi Jadwal Shift (Kalender)", "Sync");
                        getData(bulan, tahun);
                    }

                    dialogproses.dismiss();
                }


            }

            @Override
            public void onFailure(@NonNull Call<ArrayList<JadwalSift>> call, @NonNull Throwable t) {
                dialogproses.dismiss();

                dialogView.viewNotifKosong(CalendarJadwalShiftActivity.this, "Gagal mengakses server.", "Silahkan coba kembali.");

            }

        });


        dialogproses.show();

    }
}