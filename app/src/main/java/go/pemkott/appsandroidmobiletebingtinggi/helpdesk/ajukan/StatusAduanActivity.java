package go.pemkott.appsandroidmobiletebingtinggi.helpdesk.ajukan;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;
import go.pemkott.appsandroidmobiletebingtinggi.model.AduanResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatusAduanActivity extends AppCompatActivity {

    private RecyclerView rvAduan;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private LinearLayout llEmpty;
    private TextView tvEmptyTitle, tvEmptyDesc;
    private AduanAdapter adapter;
    
    private TextInputEditText etSearchNomor;
    private MaterialButton btnFilterTanggal;
    private ImageButton btnClearFilter;
    private String selectedDate = "";
    private String searchQuery = "";
    
    private HttpService httpService;
    private SessionManager session;
    private String sEmployee_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_status_aduan);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupData();
        fetchAduans();
    }

    private void initViews() {
        rvAduan = findViewById(R.id.rvAduan);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        llEmpty = findViewById(R.id.llEmpty);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyDesc = findViewById(R.id.tvEmptyDesc);
        etSearchNomor = findViewById(R.id.etSearchNomor);
        btnFilterTanggal = findViewById(R.id.btnFilterTanggal);
        btnClearFilter = findViewById(R.id.btnClearFilter);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        rvAduan.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AduanAdapter(aduan -> {
            Intent intent = new Intent(this, AduanDetailActivity.class);
            intent.putExtra("aduan_id", aduan.getId());
            intent.putExtra("employee_id", sEmployee_id);
            startActivity(intent);
        });
        rvAduan.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::fetchAduans);

        setupFilters();
    }

    private void setupFilters() {
        etSearchNomor.addTextChangedListener(new TextWatcher() {
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

        btnFilterTanggal.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                btnFilterTanggal.setText(selectedDate);
                btnClearFilter.setVisibility(View.VISIBLE);
                applyLocalFilter();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        btnClearFilter.setOnClickListener(v -> {
            selectedDate = "";
            btnFilterTanggal.setText("Pilih Tanggal");
            btnClearFilter.setVisibility(View.GONE);
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
            rvAduan.setVisibility(View.GONE);
            llEmpty.setVisibility(View.VISIBLE);
            
            if (searchQuery.isEmpty() && selectedDate.isEmpty()) {
                tvEmptyTitle.setText("Belum Ada Laporan");
                tvEmptyDesc.setText("Anda Belum Pernah Mengirimkan Laporan Ke Layanan Helpdesk");
            } else {
                tvEmptyTitle.setText("Laporan Tidak Ditemukan");
                tvEmptyDesc.setText("Tidak ada data yang sesuai dengan pencarian atau filter Anda");
            }
        } else {
            rvAduan.setVisibility(View.VISIBLE);
            llEmpty.setVisibility(View.GONE);
        }
    }

    private void setupData() {
        session = new SessionManager(this);
        httpService = RetroClient.getInstance().getApi2();
        
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        try (Cursor tUser = databaseHelper.getAllData22(session.getPegawaiId())) {
            if (tUser != null && tUser.moveToNext()) {
                sEmployee_id = tUser.getString(1);
            }
        }
    }

    private void fetchAduans() {
        if (sEmployee_id == null || sEmployee_id.isEmpty()) return;

        swipeRefresh.setRefreshing(true);
        llEmpty.setVisibility(View.GONE);

        String token = "Bearer " + session.getToken();
        int empId;
        try {
            empId = Integer.parseInt(sEmployee_id);
        } catch (NumberFormatException e) {
            swipeRefresh.setRefreshing(false);
            return;
        }

        httpService.getAduans(token, empId, null, 1, 50).enqueue(new Callback<AduanResponse>() {
            @Override
            public void onResponse(@NonNull Call<AduanResponse> call, @NonNull Response<AduanResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    AduanResponse aduanResponse = response.body();
                    if (aduanResponse.getData() != null && !aduanResponse.getData().isEmpty()) {
                        adapter.setAduans(aduanResponse.getData());
                        applyLocalFilter();
                    } else {
                        rvAduan.setVisibility(View.GONE);
                        llEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(StatusAduanActivity.this, "Gagal Memuat Daftar Laporan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AduanResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                Log.e("API_ERROR", "Error: " + t.getMessage());
                Toast.makeText(StatusAduanActivity.this, "Terjadi Kesalahan Pada Koneksi Jaringan", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
