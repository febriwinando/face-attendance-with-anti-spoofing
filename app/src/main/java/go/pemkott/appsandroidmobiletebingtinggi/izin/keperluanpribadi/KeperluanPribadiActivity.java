package go.pemkott.appsandroidmobiletebingtinggi.izin.keperluanpribadi;

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
        go.pemkott.appsandroidmobiletebingtinggi.utils.WindowUtil.applyDashboardStyle(this);

        setContentView(R.layout.activity_keperluan_pribadi);

        kp = this;
        databaseHelper = new DatabaseHelper(this);
        kegiatanDatabase();

        rvKegiatanKp = findViewById(R.id.rvKegiatanKp);
        etkegiatanKpLainnya = findViewById(R.id.etKegiatanKpLainnya);
        androidx.core.widget.NestedScrollView nsvKeperluanPribadi = findViewById(R.id.nsvKeperluanPribadi);

        View titleHeader = findViewById(R.id.titleHeader);
        View kpRootLayout = findViewById(R.id.kpRootLayout);
        if (kpRootLayout != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(kpRootLayout, (v, insets) -> {
                androidx.core.graphics.Insets imeInsets = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime());
                androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                int bottomPadding = Math.max(imeInsets.bottom, systemBars.bottom);
                v.setPadding(v.getPaddingLeft(), 0, v.getPaddingRight(), bottomPadding);
                if (titleHeader != null) {
                    titleHeader.setPadding(titleHeader.getPaddingLeft(), systemBars.top, titleHeader.getPaddingRight(), titleHeader.getPaddingBottom());
                }
                return insets;
            });
        }

        if (etkegiatanKpLainnya != null) {
            etkegiatanKpLainnya.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            etkegiatanKpLainnya.setSingleLine(false);

            etkegiatanKpLainnya.setOnTouchListener((v, event) -> {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    v.performClick();
                }
                return false;
            });

            if (nsvKeperluanPribadi != null) {
                etkegiatanKpLainnya.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) {
                        nsvKeperluanPribadi.postDelayed(() -> {
                            int targetY = etkegiatanKpLainnya.getTop() - 100;
                            nsvKeperluanPribadi.smoothScrollTo(0, Math.max(0, targetY));
                        }, 250);
                    }
                });

                etkegiatanKpLainnya.setOnClickListener(v -> {
                    etkegiatanKpLainnya.requestFocus();
                    nsvKeperluanPribadi.postDelayed(() -> {
                        int targetY = etkegiatanKpLainnya.getTop() - 100;
                        nsvKeperluanPribadi.smoothScrollTo(0, Math.max(0, targetY));
                    }, 250);
                });
            }
        }

        findViewById(R.id.rlBackKP).setOnClickListener(v -> finish());
        
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
