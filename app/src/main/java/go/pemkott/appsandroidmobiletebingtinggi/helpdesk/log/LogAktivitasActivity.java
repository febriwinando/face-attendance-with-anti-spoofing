package go.pemkott.appsandroidmobiletebingtinggi.helpdesk.log;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.model.LogAktivitas;

public class LogAktivitasActivity extends AppCompatActivity {

    private RecyclerView rvLogAktivitas;
    private LinearLayout llEmptyLog;
    private LogAktivitasAdapter adapter;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_aktivitas);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        loadLogs();
    }

    private void initViews() {
        rvLogAktivitas = findViewById(R.id.rvLogAktivitas);
        llEmptyLog = findViewById(R.id.llEmptyLog);
        
        findViewById(R.id.rlBackLog).setOnClickListener(v -> finish());

        rvLogAktivitas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LogAktivitasAdapter();
        rvLogAktivitas.setAdapter(adapter);

        databaseHelper = new DatabaseHelper(this);
    }

    private void loadLogs() {
        List<LogAktivitas> logs = new ArrayList<>();
        Cursor cursor = databaseHelper.getLogs();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String empId = cursor.getString(1);
                String opdId = cursor.getString(2);
                String tanggal = cursor.getString(3);
                String kegiatan = cursor.getString(4);
                String jenis = cursor.getString(5);
                String devId = cursor.getString(6);
                String ts = cursor.getString(7);
                String empName = cursor.getString(8) != null ? cursor.getString(8) : "-";
                String opdName = cursor.getString(9) != null ? cursor.getString(9) : "-";

                logs.add(new LogAktivitas(id, empId, opdId, empName, opdName, tanggal, kegiatan, jenis, devId, ts));
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (logs.isEmpty()) {
            rvLogAktivitas.setVisibility(View.GONE);
            llEmptyLog.setVisibility(View.VISIBLE);
        } else {
            rvLogAktivitas.setVisibility(View.VISIBLE);
            llEmptyLog.setVisibility(View.GONE);
            adapter.setLogs(logs);
        }
    }
}
