package go.pemkott.appsandroidmobiletebingtinggi.helpdesk.log;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.model.LogAktivitas;

public class LogAktivitasActivity extends AppCompatActivity {

    private RecyclerView rvLogAktivitas;
    private LinearLayout llEmptyLog;
    private TextView tvEmptyLogTitle, tvEmptyLogDesc;
    private LogAktivitasAdapter adapter;
    private DatabaseHelper databaseHelper;

    private TextInputEditText etSearchKegiatan;
    private MaterialButton btnFilterTanggalLog;
    private ImageButton btnClearFilterLog;
    private String selectedDate = "";
    private String searchQuery = "";

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
        setupFilters();
    }

    private void initViews() {
        rvLogAktivitas = findViewById(R.id.rvLogAktivitas);
        llEmptyLog = findViewById(R.id.llEmptyLog);
        tvEmptyLogTitle = findViewById(R.id.tvEmptyLogTitle);
        tvEmptyLogDesc = findViewById(R.id.tvEmptyLogDesc);
        etSearchKegiatan = findViewById(R.id.etSearchKegiatan);
        btnFilterTanggalLog = findViewById(R.id.btnFilterTanggalLog);
        btnClearFilterLog = findViewById(R.id.btnClearFilterLog);
        
        findViewById(R.id.rlBackLog).setOnClickListener(v -> finish());

        rvLogAktivitas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LogAktivitasAdapter();
        rvLogAktivitas.setAdapter(adapter);

        databaseHelper = new DatabaseHelper(this);
    }

    private void setupFilters() {
        etSearchKegiatan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                applyLocalFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnFilterTanggalLog.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                btnFilterTanggalLog.setText(selectedDate);
                btnClearFilterLog.setVisibility(View.VISIBLE);
                applyLocalFilter();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        btnClearFilterLog.setOnClickListener(v -> {
            selectedDate = "";
            btnFilterTanggalLog.setText("Pilih Tanggal");
            btnClearFilterLog.setVisibility(View.GONE);
            applyLocalFilter();
        });
    }

    private void applyLocalFilter() {
        if (adapter != null) {
            adapter.filter(searchQuery, selectedDate);
            updateEmptyState();
        }
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            rvLogAktivitas.setVisibility(View.GONE);
            llEmptyLog.setVisibility(View.VISIBLE);

            if (searchQuery.isEmpty() && selectedDate.isEmpty()) {
                tvEmptyLogTitle.setText("Belum Ada Log");
                tvEmptyLogDesc.setText("Riwayat aktivitas Anda akan muncul di sini.");
            } else {
                tvEmptyLogTitle.setText("Log Tidak Ditemukan");
                tvEmptyLogDesc.setText("Tidak ada riwayat aktivitas yang sesuai dengan kriteria Anda.");
            }
        } else {
            rvLogAktivitas.setVisibility(View.VISIBLE);
            llEmptyLog.setVisibility(View.GONE);
        }
    }

    private void loadLogs() {
        List<LogAktivitas> logs = new ArrayList<>();
        Cursor cursor = databaseHelper.getLogs();

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.LOG_ID);
            int empIdIndex = cursor.getColumnIndex(DatabaseHelper.LOG_EMPLOYEE_ID);
            int opdIdIndex = cursor.getColumnIndex(DatabaseHelper.LOG_OPD_ID);
            int tanggalIndex = cursor.getColumnIndex(DatabaseHelper.LOG_TANGGAL);
            int kegiatanIndex = cursor.getColumnIndex(DatabaseHelper.LOG_KEGIATAN);
            int jenisIndex = cursor.getColumnIndex(DatabaseHelper.LOG_JENIS);
            int devIdIndex = cursor.getColumnIndex(DatabaseHelper.LOG_DEVICE_ID);
            int tsIndex = cursor.getColumnIndex(DatabaseHelper.LOG_TIMESTAMP);
            int empNameIndex = cursor.getColumnIndex("NAMA");
            int opdNameIndex = cursor.getColumnIndex("NAMA_OPD");

            do {
                int id = cursor.getInt(idIndex);
                String empId = cursor.getString(empIdIndex);
                String opdId = cursor.getString(opdIdIndex);
                String tanggal = cursor.getString(tanggalIndex);
                String kegiatan = cursor.getString(kegiatanIndex);
                String jenis = cursor.getString(jenisIndex);
                String devId = cursor.getString(devIdIndex);
                String ts = cursor.getString(tsIndex);
                
                String empName = "-";
                if (empNameIndex != -1) {
                    empName = cursor.getString(empNameIndex) != null ? cursor.getString(empNameIndex) : "-";
                }
                
                String opdName = "-";
                if (opdNameIndex != -1) {
                    opdName = cursor.getString(opdNameIndex) != null ? cursor.getString(opdNameIndex) : "-";
                }

                logs.add(new LogAktivitas(id, empId, opdId, empName, opdName, tanggal, kegiatan, jenis, devId, ts));
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (logs.isEmpty()) {
            rvLogAktivitas.setVisibility(View.GONE);
            llEmptyLog.setVisibility(View.VISIBLE);
        } else {
            adapter.setLogs(logs);
            applyLocalFilter();
        }
    }
}
