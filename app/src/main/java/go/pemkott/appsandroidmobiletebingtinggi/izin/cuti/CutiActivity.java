package go.pemkott.appsandroidmobiletebingtinggi.izin.cuti;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.camerax.CameraxActivity;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.model.Kegiatan;

public class CutiActivity extends AppCompatActivity {

    public static AppCompatActivity cuti ;
    DatabaseHelper databaseHelper;

    public static ArrayList<String> kegiatanCheckedCuti = new ArrayList<String>();
    private static List<Kegiatan> listCuti = new ArrayList<>();
    static List<String> kegiatansListCuti = new ArrayList<String>();

    CutiAdapter cutiAdapter;

    EditText etkegiatanCutiLainnya;
    RecyclerView rvKegiatanCuti;

    StringBuffer buffer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_color));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.background_color));
        setContentView(R.layout.activity_cuti);


        cuti = this;


        rvKegiatanCuti = findViewById(R.id.rvKegiatanCuti);
        etkegiatanCutiLainnya = findViewById(R.id.etKegiatanCutiLainnya);
        etkegiatanCutiLainnya.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        listCuti.clear();
        kegiatansListCuti.clear();
        databaseHelper = new DatabaseHelper(this);
        kegiatanDatabase();

        listCuti.addAll(getListData());
        showRecyclerList();


    }


    public void kegiatanDatabase(){
        Cursor res = databaseHelper.getKegiatanIzin();
        while (res.moveToNext()){
            if (res.getString(1).equals("cuti")){
                kegiatansListCuti.add(res.getString(2));
            }
        }
    }

    static ArrayList<Kegiatan> getListData() {
        ArrayList<Kegiatan> list = new ArrayList<>();
        list.clear();
        for (int position = 0; position < kegiatansListCuti.size(); position++) {
            Kegiatan kegiatans = new Kegiatan();
            kegiatans.setKegiatan(kegiatansListCuti.get(position));
            list.add(kegiatans);
        }
        return list;
    }

    private void showRecyclerList(){
        rvKegiatanCuti.setLayoutManager(new LinearLayoutManager(this));
        cutiAdapter = new CutiAdapter(listCuti);
        rvKegiatanCuti.setAdapter(cutiAdapter);

        cutiAdapter.setOnItemClickCallback(new CutiAdapter.OnItemClickCallback() {
            @Override
            public void onItemClicked(Kegiatan data) {
                showSelectedKegiatan(data);
            }
        });
    }

    private void showSelectedKegiatan(Kegiatan kegiatan) {
        if (kegiatan.isChecked() == true ){
            kegiatanCheckedCuti.add(kegiatan.getKegiatan());

            buffer = new StringBuffer();
            for (int i = 0; i<kegiatanCheckedCuti.size()-1;i++){
                buffer.append(kegiatanCheckedCuti.get(i)+", ");
            }
            buffer.append(kegiatanCheckedCuti.get(kegiatanCheckedCuti.size()-1));


        }else{
            kegiatanCheckedCuti.remove(kegiatan.getKegiatan());

            StringBuffer buffer = new StringBuffer();
            if (kegiatanCheckedCuti.size() == 1){
                buffer.append(kegiatanCheckedCuti.get(kegiatanCheckedCuti.size()-1));
            }else if(kegiatanCheckedCuti.isEmpty()){
            }
            else{
                for (int i = 0; i<kegiatanCheckedCuti.size()-1;i++){
                    buffer.append(kegiatanCheckedCuti.get(i)+", ");
                }
                buffer.append(kegiatanCheckedCuti.get(kegiatanCheckedCuti.size()-1)+", ");
            }
        }

    }

    public  static  String kegiatansCutiLainnya;

    DialogView dialogView = new DialogView(CutiActivity.this);
    public void nextKegiatanCuti(View view){
        if (!etkegiatanCutiLainnya.getText().toString().isEmpty()){
            kegiatansCutiLainnya = etkegiatanCutiLainnya.getText().toString();
        }else{
            kegiatansCutiLainnya = "kosong";
        }

        if (kegiatanCheckedCuti.isEmpty() && kegiatansCutiLainnya.equals("kosong")){
            dialogView.viewNotifKosong(CutiActivity.this, "Anda Harus Mengisi Kegiatan Yang Dilaksanakan.", "");

        }else {

            Intent intentTL = new Intent(CutiActivity.this, CameraxActivity.class);
            intentTL.putExtra("aktivitas", 5);
            startActivity(intentTL);

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}