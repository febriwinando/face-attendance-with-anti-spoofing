package go.pemkott.appsandroidmobiletebingtinggi.izin.sakit;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

public class SakitActivity extends AppCompatActivity {
    private ArrayList<Kegiatan> listSakit = new ArrayList<>();
    private ArrayList<String> kegiatanCheckedSakit = new ArrayList<String>();
    private ArrayList<String> kegiatansListSakit = new ArrayList<String>();
    private String kegiatansSakitLainnya;

    EditText etkegiatanSakitLainnya;
    RecyclerView rvKegiatanSakit;
    DatabaseHelper databaseHelper;
    SakitAdapter sakitAdapter;
    DialogView dialogView = new DialogView(SakitActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_sakit);

        databaseHelper = new DatabaseHelper(this);
        kegiatanDatabase();

        rvKegiatanSakit = findViewById(R.id.rvKegiatanSakit);
        etkegiatanSakitLainnya = findViewById(R.id.etKegiatanSakitLainmya);
        
        setupRecyclerData();
        
        RelativeLayout backSakitIzinActivity = findViewById(R.id.rlBackSakit);
        TextView tvSelanjutnya = findViewById(R.id.tvSelanjutnya);

        tvSelanjutnya.setOnClickListener(v -> nextKegiatanSakitIzin());
        backSakitIzinActivity.setOnClickListener(v -> finish());

        showRecyclerList();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void setupRecyclerData() {
        listSakit.clear();
        for (String s : kegiatansListSakit) {
            Kegiatan k = new Kegiatan();
            k.setKegiatan(s);
            listSakit.add(k);
        }
    }

    public void kegiatanDatabase(){
        kegiatansListSakit.clear();
        Cursor res = databaseHelper.getKegiatanIzin();
        while (res.moveToNext()){
            if (res.getString(1).equals("sk")){
                kegiatansListSakit.add(res.getString(2));
            }
        }
    }

    public void nextKegiatanSakitIzin(){
        if (etkegiatanSakitLainnya != null && !etkegiatanSakitLainnya.getText().toString().isEmpty()){
            kegiatansSakitLainnya = etkegiatanSakitLainnya.getText().toString();
        } else {
            kegiatansSakitLainnya = "kosong";
        }

        if (kegiatanCheckedSakit.isEmpty() && kegiatansSakitLainnya.equals("kosong")){
            dialogView.viewNotifKosong(this, "Anda Harus Mengisi Kegiatan Yang Dilaksanakan.", "");
        } else {
            Intent intentTL = new Intent(this, databaseHelper.getCameraActivityClass());
            intentTL.putExtra("lampiran", 23);
            intentTL.putExtra("aktivitas", "izinsakit");
            intentTL.putExtra("title", "Isi Data Kondisi Kesehatan");
            intentTL.putStringArrayListExtra("kegiatan_checked_sakit", kegiatanCheckedSakit);
            intentTL.putExtra("kegiatans_sakit_lainnya", kegiatansSakitLainnya);
            startActivity(intentTL);
        }
    }

    private void showRecyclerList(){
        rvKegiatanSakit.setLayoutManager(new LinearLayoutManager(this));
        sakitAdapter = new SakitAdapter(listSakit);
        rvKegiatanSakit.setAdapter(sakitAdapter);

        sakitAdapter.setOnItemClickCallback(data -> {
            if (data.isChecked()){
                if (!kegiatanCheckedSakit.contains(data.getKegiatan())) {
                    kegiatanCheckedSakit.add(data.getKegiatan());
                }
            } else {
                kegiatanCheckedSakit.remove(data.getKegiatan());
            }
        });
    }
}
