package go.pemkott.appsandroidmobiletebingtinggi.izinshift.izinshiftpribadi;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.izin.keperluanpribadi.KpAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.model.Kegiatan;

import go.pemkott.appsandroidmobiletebingtinggi.utils.ClsGlobal;

public class KeperluanPribadiShiftActivity extends AppCompatActivity {

    private ArrayList<Kegiatan> list = new ArrayList<>();
    public static ArrayList<String> kegiatanChecked = new ArrayList<>();
    public static ArrayList<String> kegiatansList = new ArrayList<>();
    public static String kegiatansLainnya;

    EditText etkegiatanKpLainnya;
    RecyclerView rvKegiatanKp;
    KpAdapter kpAdapter;
    DatabaseHelper databaseHelper;
    TextView tvlistCheckedKp;

    public static AppCompatActivity kp ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_keperluan_pribadi_shift);
        kp = this;
        databaseHelper = new DatabaseHelper(this);
        kegiatanDatabase();

        rvKegiatanKp = findViewById(R.id.rvKegiatanKp);
        etkegiatanKpLainnya = findViewById(R.id.etKegiatanKpLainnya);
        tvlistCheckedKp = findViewById(R.id.tvlistCheckedKp);

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

    public void backKp(View view){
        finish();
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
        if (!etkegiatanKpLainnya.getText().toString().isEmpty()){
            kegiatansLainnya = etkegiatanKpLainnya.getText().toString();
        }else{
            kegiatansLainnya = "kosong";
        }

        if (kegiatanChecked.isEmpty() && "kosong".equals(kegiatansLainnya)){
            showMessage("Peringatan!", "Anda Harus Mengisi Kegiatan Yang Dilaksanakan.");
        }else {
            Intent intentTL = new Intent(this, databaseHelper.getCameraActivityClass());
            intentTL.putExtra("title", "Isi Data Keperluan Pribadi");
            intentTL.putExtra("aktivitas", "shiftizinkp");
            
            // Forward everything from previous activities if any
            if (getIntent().getExtras() != null) {
                intentTL.putExtras(getIntent().getExtras());
            }
            
            intentTL.putStringArrayListExtra("kegiatan_checked_kp", kegiatanChecked);
            intentTL.putExtra("kegiatans_kp_lainnya", kegiatansLainnya);
            
            startActivity(intentTL);
        }
    }

    public void showMessage(String title, String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ThemeOverlay_App_MaterialAlertDialog);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
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
            updateCheckedText();
        });
    }

    private void updateCheckedText() {
        if (kegiatanChecked.isEmpty()) {
            tvlistCheckedKp.setVisibility(View.GONE);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < kegiatanChecked.size(); i++) {
                sb.append(kegiatanChecked.get(i));
                if (i < kegiatanChecked.size() - 1) sb.append(", ");
            }
            tvlistCheckedKp.setText(ClsGlobal.capitalizeEveryWord(sb.toString()));
            tvlistCheckedKp.setVisibility(View.VISIBLE);
        }
    }
}
