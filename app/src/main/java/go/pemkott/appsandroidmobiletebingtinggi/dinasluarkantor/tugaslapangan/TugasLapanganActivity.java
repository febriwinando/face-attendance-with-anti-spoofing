package go.pemkott.appsandroidmobiletebingtinggi.dinasluarkantor.tugaslapangan;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
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

public class TugasLapanganActivity extends AppCompatActivity {

    private ArrayList<Kegiatan> list = new ArrayList<>();
    private ArrayList<String> kegiatanChecked = new ArrayList<String>();
    private ArrayList<String> kegiatansList = new ArrayList<String>();
    private String kegiatansLainnya = "kosong";

    EditText etkegiatanLainnya;
    RecyclerView rvKegiatanPd;
    KegiatanAdapter kegiatanAdapter;
    DatabaseHelper databaseHelper;
    DialogView dialogView = new DialogView(TugasLapanganActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_tugas_lapangan);

        databaseHelper = new DatabaseHelper(this);
        kegiatanDatabase();

        rvKegiatanPd = findViewById(R.id.rvKegiatanPd);
        etkegiatanLainnya = findViewById(R.id.etKegiatanLainnya);
        
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
            if (res.getString(1).equals("tl")){
                kegiatansList.add(res.getString(2));
            }
        }
    }

    public void nextKegiatanTL(View view){
        if (etkegiatanLainnya != null && !etkegiatanLainnya.getText().toString().isEmpty()){
            kegiatansLainnya = etkegiatanLainnya.getText().toString();
        } else {
            kegiatansLainnya = "kosong";
        }

        boolean isKegiatanCheckedKosong = kegiatanChecked.isEmpty();
        boolean isLainnyaKosong = kegiatansLainnya.equals("kosong");

        if (isKegiatanCheckedKosong && isLainnyaKosong){
            dialogView.viewNotifKosong(this, "Anda Harus Mengisi Kegiatan Yang Dilaksanakan.", "");
        } else {
            Intent intentTL = new Intent(this, databaseHelper.getCameraActivityClass());
            intentTL.putExtra("aktivitas", "tugaslapangan");
            intentTL.putExtra("title", "Isi Data Tugas Lapangan");
            intentTL.putStringArrayListExtra("kegiatan_checked", kegiatanChecked);
            intentTL.putExtra("kegiatans_lainnya", kegiatansLainnya);
            startActivity(intentTL);
        }
    }

    private void showRecyclerList(){
        rvKegiatanPd.setLayoutManager(new LinearLayoutManager(this));
        kegiatanAdapter = new KegiatanAdapter(list);
        rvKegiatanPd.setAdapter(kegiatanAdapter);

        kegiatanAdapter.setOnItemClickCallback(new KegiatanAdapter.OnItemClickCallback() {
            @Override
            public void onItemClicked(Kegiatan data) {
                if (data.isChecked()){
                    if (!kegiatanChecked.contains(data.getKegiatan())) {
                        kegiatanChecked.add(data.getKegiatan());
                    }
                } else {
                    kegiatanChecked.remove(data.getKegiatan());
                }
            }
        });
    }

    public void backTl(View view){
        finish();
    }
}
