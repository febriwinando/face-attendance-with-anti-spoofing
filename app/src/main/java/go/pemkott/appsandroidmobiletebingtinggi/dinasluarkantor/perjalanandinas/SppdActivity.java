package go.pemkott.appsandroidmobiletebingtinggi.dinasluarkantor.perjalanandinas;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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

public class SppdActivity extends AppCompatActivity {

    private ArrayList<Kegiatan> listPd = new ArrayList<>();
    private ArrayList<String> kegiatanCheckedPd = new ArrayList<String>();
    private ArrayList<String> kegiatansListPd = new ArrayList<String>();
    private String kegiatansPdLainnya = "kosong";

    EditText etkegiatanPdLainnya;
    RecyclerView rvKegiatanPd;
    DatabaseHelper databaseHelper;
    SppdAdapter sppdAdapter;
    DialogView dialogView = new DialogView(SppdActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sppd);

        databaseHelper = new DatabaseHelper(this);
        kegiatanDatabase();

        rvKegiatanPd = findViewById(R.id.rvKegiatanPd);
        etkegiatanPdLainnya = findViewById(R.id.etKegiatanPdLainnya);
        etkegiatanPdLainnya.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

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
        listPd.clear();
        for (String s : kegiatansListPd) {
            Kegiatan k = new Kegiatan();
            k.setKegiatan(s);
            listPd.add(k);
        }
    }

    public void kegiatanDatabase(){
        kegiatansListPd.clear();
        Cursor res = databaseHelper.getKegiatanIzin();
        while (res.moveToNext()){
            if (res.getString(1).equals("pd")){
                kegiatansListPd.add(res.getString(2));
            }
        }
    }

    public void nextKegiatanPd(View view) {
        if (etkegiatanPdLainnya != null && !etkegiatanPdLainnya.getText().toString().isEmpty()) {
            kegiatansPdLainnya = etkegiatanPdLainnya.getText().toString();
        } else {
            kegiatansPdLainnya = "kosong";
        }

        boolean isKegiatanCheckedKosong = kegiatanCheckedPd.isEmpty();
        boolean isLainnyaKosong = kegiatansPdLainnya.equals("kosong");

        if (isKegiatanCheckedKosong && isLainnyaKosong) {
            dialogView.viewNotifKosong(this, "Anda Harus Mengisi Kegiatan Yang Dilaksanakan.", "");
            return;
        } else {
            Intent intentTL = new Intent(this, databaseHelper.getCameraActivityClass());
            intentTL.putExtra("aktivitas", "perjalanandinas");
            intentTL.putExtra("title", "Isi Data Perjalanan Dinas");
            intentTL.putStringArrayListExtra("kegiatan_checked_pd", kegiatanCheckedPd);
            intentTL.putExtra("kegiatan_pd_lainnya", kegiatansPdLainnya);
            startActivity(intentTL);
        }
    }

    private void showRecyclerList(){
        rvKegiatanPd.setLayoutManager(new LinearLayoutManager(this));
        sppdAdapter = new SppdAdapter(this, listPd);
        rvKegiatanPd.setAdapter(sppdAdapter);

        sppdAdapter.setOnItemClickCallback(new SppdAdapter.OnItemClickCallback() {
            @Override
            public void onItemClicked(Kegiatan data) {
                if (data.isChecked()){
                    if (!kegiatanCheckedPd.contains(data.getKegiatan())) {
                        kegiatanCheckedPd.add(data.getKegiatan());
                    }
                } else {
                    kegiatanCheckedPd.remove(data.getKegiatan());
                }
            }
        });
    }

    public void backPd(View view){
        finish();
    }
}
