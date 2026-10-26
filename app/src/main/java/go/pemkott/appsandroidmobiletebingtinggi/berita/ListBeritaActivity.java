package go.pemkott.appsandroidmobiletebingtinggi.berita;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.berita.BeritaVerticalAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.model.berita.BeritaItem;
import go.pemkott.appsandroidmobiletebingtinggi.model.berita.BeritaResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListBeritaActivity extends AppCompatActivity {

    private RelativeLayout rlListHeader;
    private MaterialCardView cvListBack;
    private EditText etSearchQuery;
    private RecyclerView rvListBerita;
    private ProgressBar pbLoadingList;
    private LinearLayout llEmptyState;

    private BeritaVerticalAdapter adapter;
    private HttpService httpService;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        boolean isDarkMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        controller.setAppearanceLightStatusBars(!isDarkMode);
        controller.setAppearanceLightNavigationBars(!isDarkMode);

        setContentView(R.layout.activity_list_berita);

        httpService = RetroClient.getInstance().getApi2();

        rlListHeader = findViewById(R.id.rlListHeader);
        cvListBack = findViewById(R.id.cvListBack);
        etSearchQuery = findViewById(R.id.etSearchQuery);
        rvListBerita = findViewById(R.id.rvListBerita);
        pbLoadingList = findViewById(R.id.pbLoadingList);
        llEmptyState = findViewById(R.id.llEmptyState);

        View clMainList = findViewById(R.id.clMainList);
        if (clMainList != null && rlListHeader != null) {
            ViewCompat.setOnApplyWindowInsetsListener(clMainList, (v, insets) -> {
                int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rlListHeader.getLayoutParams();
                params.topMargin = statusBarHeight;
                rlListHeader.setLayoutParams(params);
                return insets;
            });
        }

        cvListBack.setOnClickListener(v -> finish());

        rvListBerita.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BeritaVerticalAdapter(this);
        rvListBerita.setAdapter(adapter);

        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> performSearch(s.toString().trim());
                searchHandler.postDelayed(searchRunnable, 400);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadAllNews();
    }

    private void loadAllNews() {
        if (pbLoadingList != null) pbLoadingList.setVisibility(View.VISIBLE);
        if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);

        String localUrl = "http://172.15.1.13:4321/api/berita?type=lainnya&limit=30";
        String publicUrl = "https://diskominfo.tebingtinggikota.go.id/api/berita?type=lainnya&limit=30";

        fetchListFromUrl(localUrl, () -> fetchListFromUrl(publicUrl, null));
    }

    private void performSearch(String query) {
        if (pbLoadingList != null) pbLoadingList.setVisibility(View.VISIBLE);
        if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);

        String localUrl = "http://172.15.1.13:4321/api/berita?type=lainnya&q=" + query + "&limit=30";
        String publicUrl = "https://diskominfo.tebingtinggikota.go.id/api/berita?type=lainnya&q=" + query + "&limit=30";

        fetchListFromUrl(localUrl, () -> fetchListFromUrl(publicUrl, null));
    }

    private void fetchListFromUrl(String url, Runnable onFailureFallback) {
        httpService.getBeritaDiskominfo(url).enqueue(new Callback<BeritaResponse>() {
            @Override
            public void onResponse(@NonNull Call<BeritaResponse> call, @NonNull Response<BeritaResponse> response) {
                if (pbLoadingList != null) pbLoadingList.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<BeritaItem> items = response.body().getItemList();
                    if (items == null && response.body().getData() != null) {
                        items = response.body().getData().getBeritaLainnya();
                        if (items == null || items.isEmpty()) {
                            items = response.body().getData().getBeritaTerbaru();
                        }
                    }

                    if (items != null && !items.isEmpty()) {
                        adapter.setList(items);
                        if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);
                    } else {
                        adapter.setList(null);
                        if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
                    }
                } else if (onFailureFallback != null) {
                    onFailureFallback.run();
                } else {
                    if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BeritaResponse> call, @NonNull Throwable t) {
                if (onFailureFallback != null) {
                    onFailureFallback.run();
                } else {
                    if (pbLoadingList != null) pbLoadingList.setVisibility(View.GONE);
                    if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
