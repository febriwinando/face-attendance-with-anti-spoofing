package go.pemkott.appsandroidmobiletebingtinggi.dinasluarkantor.perjalanandinas;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import go.pemkott.appsandroidmobiletebingtinggi.DeteksiWajah.DetectorActivity;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.model.Kegiatan;

public class SppdActivity extends AppCompatActivity {

    private static ArrayList<Kegiatan> listPd = new ArrayList<>();

    private ArrayList<String>  kegiatanAddedPd = new ArrayList<String>();
    public static ArrayList<String> kegiatanCheckedPd = new ArrayList<String>();
    static ArrayList<String> kegiatansListPd = new ArrayList<String>();
    public static String kegiatansPdLainnya = "kosong";
    StringBuffer buffer2;

    EditText etkegiatanPdLainnya;
    RecyclerView rvKegiatanPd;
    DatabaseHelper databaseHelper;
    SppdAdapter sppdAdapter;

    public static AppCompatActivity pd ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_color));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.background_color));
        setContentView(R.layout.activity_sppd);


        pd = this;


        listPd.clear();
        kegiatansListPd.clear();

        databaseHelper = new DatabaseHelper(this);
        kegiatanDatabase();

        rvKegiatanPd = findViewById(R.id.rvKegiatanPd);
        etkegiatanPdLainnya = findViewById(R.id.etKegiatanPdLainnya);
        etkegiatanPdLainnya.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);


        listPd.addAll(getListData2());

        showRecyclerList();

    }


    static ArrayList<Kegiatan> getListData2() {
        ArrayList<Kegiatan> list = new ArrayList<>();
        list.clear();
        for (int position = 0; position < kegiatansListPd.size(); position++) {
            Kegiatan kegiatans = new Kegiatan();
            kegiatans.setKegiatan(kegiatansListPd.get(position));
            list.add(kegiatans);

        }
        Log.d("PerjalananDinasList", list.toString());
        return list;
    }

    public void kegiatanDatabase(){
        Cursor res = databaseHelper.getKegiatanIzin();
        while (res.moveToNext()){
            if (res.getString(1).equals("pd")){
                kegiatansListPd.add(res.getString(2));
            }
        }
    }
    DialogView dialogView = new DialogView(SppdActivity.this);
    public void nextKegiatanPd(View view) {
        // Pastikan EditText sudah terhubung
        if (etkegiatanPdLainnya != null && !etkegiatanPdLainnya.getText().toString().isEmpty()) {
            kegiatansPdLainnya = etkegiatanPdLainnya.getText().toString();
        } else {
            kegiatansPdLainnya = "kosong";
        }

        if (kegiatanCheckedPd.isEmpty() && "kosong".equals(kegiatansPdLainnya)) {
            dialogView.viewNotifKosong(SppdActivity.this,
                    "Anda Harus Mengisi Kegiatan Yang Dilaksanakan.", "");
        } else {
            Intent intentPd = new Intent(SppdActivity.this, DetectorActivity.class);
            intentPd.putExtra("title", "Isi Data Perjalanan Dinas");
            startActivity(intentPd);
        }
    }


    private void showRecyclerList(){
        rvKegiatanPd.setLayoutManager(new LinearLayoutManager(this));
        sppdAdapter = new SppdAdapter(SppdActivity.this, listPd);
        rvKegiatanPd.setAdapter(sppdAdapter);

        sppdAdapter.setOnItemClickCallback(new SppdAdapter.OnItemClickCallback() {
            @Override
            public void onItemClicked(Kegiatan data) {
                showSelectedKegiatan(data);
            }
        });
    }

    private void showSelectedKegiatan(Kegiatan kegiatan) {
        if (kegiatan.isChecked()){
            kegiatanCheckedPd.add(kegiatan.getKegiatan());

            buffer2 = new StringBuffer();
            for (int i = 0; i<kegiatanCheckedPd.size()-1;i++){
                buffer2.append(kegiatanCheckedPd.get(i)+", ");
            }
            buffer2.append(kegiatanCheckedPd.get(kegiatanCheckedPd.size()-1));

        }else{
            kegiatanCheckedPd.remove(kegiatan.getKegiatan());

            StringBuffer buffer = new StringBuffer();
            if (kegiatanCheckedPd.size() == 1){
                buffer.append(kegiatanCheckedPd.get(kegiatanCheckedPd.size()-1));
            }else if(kegiatanCheckedPd.isEmpty()){
            }
            else{
                for (int i = 0; i<kegiatanCheckedPd.size()-1;i++){
                    buffer.append(kegiatanCheckedPd.get(i)+", ");
                }
                buffer.append(kegiatanCheckedPd.get(kegiatanCheckedPd.size()-1)+", ");
            }

        }

        Log.d("PerjalananDinasList", kegiatanCheckedPd.toString());

    }

    public void backPd(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}