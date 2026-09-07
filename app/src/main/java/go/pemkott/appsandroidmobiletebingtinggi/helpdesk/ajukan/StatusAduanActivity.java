package go.pemkott.appsandroidmobiletebingtinggi.helpdesk.ajukan;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
    private AduanAdapter adapter;
    
    private HttpService httpService;
    private SessionManager session;
    private String sEmployee_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_status_aduan);

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
    }

    private void setupData() {
        session = new SessionManager(this);
        httpService = RetroClient.getInstance().getApi2();
        
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        Cursor tUser = databaseHelper.getAllData22(session.getPegawaiId());
        if (tUser.moveToNext()) {
            sEmployee_id = tUser.getString(1);
        }
    }

    private void fetchAduans() {
        if (sEmployee_id == null) return;

        swipeRefresh.setRefreshing(true);
        llEmpty.setVisibility(View.GONE);

        String token = "Bearer " + session.getToken();
        int empId = Integer.parseInt(sEmployee_id);

        httpService.getAduans(token, empId, null, 1, 50).enqueue(new Callback<AduanResponse>() {
            @Override
            public void onResponse(@NonNull Call<AduanResponse> call, @NonNull Response<AduanResponse> response) {
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    AduanResponse aduanResponse = response.body();
                    if (aduanResponse.getData() != null && !aduanResponse.getData().isEmpty()) {
                        adapter.setAduans(aduanResponse.getData());
                        rvAduan.setVisibility(View.VISIBLE);
                        llEmpty.setVisibility(View.GONE);
                    } else {
                        rvAduan.setVisibility(View.GONE);
                        llEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(StatusAduanActivity.this, "Gagal memuat daftar laporan.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AduanResponse> call, @NonNull Throwable t) {
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                Log.e("API_ERROR", "Error: " + t.getMessage());
                Toast.makeText(StatusAduanActivity.this, "Terjadi kesalahan pada koneksi jaringan.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
