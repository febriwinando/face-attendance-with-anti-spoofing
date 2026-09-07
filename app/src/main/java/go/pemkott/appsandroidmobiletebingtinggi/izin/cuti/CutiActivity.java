package go.pemkott.appsandroidmobiletebingtinggi.izin.cuti;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.model.Kegiatan;

public class CutiActivity extends AppCompatActivity {

    DatabaseHelper databaseHelper;
    private ArrayList<String> kegiatanCheckedCuti = new ArrayList<>();
    private List<Kegiatan> listCuti = new ArrayList<>();
    private List<String> kegiatansListCuti = new ArrayList<>();
    private String kegiatansCutiLainnya;

    CutiAdapter cutiAdapter;
    EditText etkegiatanCutiLainnya;
    RecyclerView rvKegiatanCuti;
    DialogView dialogView = new DialogView(CutiActivity.this);
    RelativeLayout rlBackCuti;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_cuti);

        databaseHelper = new DatabaseHelper(this);
        kegiatanDatabase();

        rvKegiatanCuti = findViewById(R.id.rvKegiatanCuti);
        etkegiatanCutiLainnya = findViewById(R.id.etKegiatanCutiLainnya);
        etkegiatanCutiLainnya.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        rlBackCuti = findViewById(R.id.rlBackCuti);
        setupRecyclerData();
        showRecyclerList();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
        rlBackCuti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void setupRecyclerData() {
        listCuti.clear();
        for (String s : kegiatansListCuti) {
            Kegiatan k = new Kegiatan();
            k.setKegiatan(s);
            listCuti.add(k);
        }
    }

    public void kegiatanDatabase(){
        kegiatansListCuti.clear();
        Cursor res = databaseHelper.getKegiatanIzin();
        while (res.moveToNext()){
            if (res.getString(1).equals("cuti")){
                kegiatansListCuti.add(res.getString(2));
            }
        }
    }

    private void showRecyclerList(){
        rvKegiatanCuti.setLayoutManager(new LinearLayoutManager(this));
        cutiAdapter = new CutiAdapter(listCuti);
        rvKegiatanCuti.setAdapter(cutiAdapter);

        cutiAdapter.setOnItemClickCallback(data -> {
            if (data.isChecked()){
                if (!kegiatanCheckedCuti.contains(data.getKegiatan())) {
                    kegiatanCheckedCuti.add(data.getKegiatan());
                }
            } else {
                kegiatanCheckedCuti.remove(data.getKegiatan());
            }
        });
    }

    public void nextKegiatanCuti(View view){
        if (etkegiatanCutiLainnya != null && !etkegiatanCutiLainnya.getText().toString().isEmpty()){
            kegiatansCutiLainnya = etkegiatanCutiLainnya.getText().toString();
        } else {
            kegiatansCutiLainnya = "kosong";
        }

        if (kegiatanCheckedCuti.isEmpty() && "kosong".equals(kegiatansCutiLainnya)){
            dialogView.viewNotifKosong(this, "Anda Harus Mengisi Kegiatan Yang Dilaksanakan.", "");
        } else {
            Intent intentTL = new Intent(this, databaseHelper.getCameraActivityClass());
            intentTL.putExtra("aktivitas", "izincuti");
            intentTL.putExtra("title", "Isi Data Cuti");
            intentTL.putStringArrayListExtra("kegiatan_checked_cuti", kegiatanCheckedCuti);
            intentTL.putExtra("kegiatans_cuti_lainnya", kegiatansCutiLainnya);
            startActivity(intentTL);
        }
    }
}
