package go.pemkott.appsandroidmobiletebingtinggi.izin.keperluanpribadi;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.model.Kegiatan;

public class KeperluanPribadiActivity extends AppCompatActivity {

    private ArrayList<Kegiatan> list = new ArrayList<>();
    private ArrayList<String> kegiatanChecked = new ArrayList<>();
    private ArrayList<String> kegiatansList = new ArrayList<>();
    private String kegiatansLainnya;

    EditText etkegiatanKpLainnya;
    RecyclerView rvKegiatanKp;
    KpAdapter kpAdapter;
    DatabaseHelper databaseHelper;
    DialogView dialogView = new DialogView(KeperluanPribadiActivity.this);

    public static AppCompatActivity kp ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_color));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.background_color));
        setContentView(R.layout.activity_keperluan_pribadi);

        kp = this;
        databaseHelper = new DatabaseHelper(this);
        kegiatanDatabase();

        rvKegiatanKp = findViewById(R.id.rvKegiatanKp);
        etkegiatanKpLainnya = findViewById(R.id.etKegiatanKpLainnya);
        
        setupRecyclerData();
        showRecyclerList();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void setupRecyclerData() {
        list.clear();
        for (String s : kegiatansList) {
            Kegiatan k = new Kegiatan();
            k.setKegiatan(s);
            list.add(k);
        }
    }

    public void kegiatanDatabase(){
        kegiatansList.clear();
        Cursor res = databaseHelper.getKegiatanIzin();
        while (res.moveToNext()){
            if (res.getString(1).equals("kp")){
                kegiatansList.add(res.getString(2));
            }
        }
    }

    public void nextKegiatanKp(View view){
        if (etkegiatanKpLainnya != null && !etkegiatanKpLainnya.getText().toString().isEmpty()){
            kegiatansLainnya = etkegiatanKpLainnya.getText().toString();
        } else {
            kegiatansLainnya = "kosong";
        }

        if (kegiatanChecked.isEmpty() && "kosong".equals(kegiatansLainnya)){
            dialogView.viewNotifKosong(this, "Anda Harus Mengisi Kegiatan Yang Dilaksanakan.", "");
        } else {
            Intent intentTL = new Intent(this, databaseHelper.getCameraActivityClass());
            intentTL.putExtra("aktivitas", "izinkp");
            intentTL.putExtra("title", "Isi Informasi Keperluan Pribadi");
            intentTL.putStringArrayListExtra("kegiatan_checked_kp", kegiatanChecked);
            intentTL.putExtra("kegiatans_kp_lainnya", kegiatansLainnya);
            startActivity(intentTL);
        }
    }

    private void showRecyclerList(){
        rvKegiatanKp.setLayoutManager(new LinearLayoutManager(this));
        kpAdapter = new KpAdapter(list);
        rvKegiatanKp.setAdapter(kpAdapter);

        kpAdapter.setOnItemClickCallback(data -> {
            if (data.isChecked()){
                if (!kegiatanChecked.contains(data.getKegiatan())) {
                    kegiatanChecked.add(data.getKegiatan());
                }
            } else {
                kegiatanChecked.remove(data.getKegiatan());
            }
        });
    }

    public void backKp(View view){
        finish();
    }
}
