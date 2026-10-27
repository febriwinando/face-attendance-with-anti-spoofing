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

    private int currentPage = 1;
    private final int limit = 15;
    private boolean isLoading = false;
    private boolean hasNextPage = true;
    private String currentQuery = "";

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

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvListBerita.setLayoutManager(layoutManager);
        adapter = new BeritaVerticalAdapter(this);
        rvListBerita.setAdapter(adapter);

        rvListBerita.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) { // scrolling down
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && hasNextPage) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3
                                && firstVisibleItemPosition >= 0) {
                            loadMoreNews();
                        }
                    }
                }
            }
        });

        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> loadInitialNews(s.toString().trim());
                searchHandler.postDelayed(searchRunnable, 400);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadInitialNews("");
    }

    private void loadInitialNews(String query) {
        currentQuery = query;
        currentPage = 1;
        hasNextPage = true;
        isLoading = true;

        if (pbLoadingList != null) pbLoadingList.setVisibility(View.VISIBLE);
        if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);

        String newsUrl = buildNewsUrl(currentPage, query);
        fetchPageFromUrl(newsUrl, true);
    }

    private void loadMoreNews() {
        if (isLoading || !hasNextPage) return;
        isLoading = true;
        int nextPage = currentPage + 1;

        if (pbLoadingList != null) pbLoadingList.setVisibility(View.VISIBLE);

        String newsUrl = buildNewsUrl(nextPage, currentQuery);
        fetchPageFromUrl(newsUrl, false);
    }

    private String buildNewsUrl(int page, String query) {
        String baseUrl = "https://diskominfo.tebingtinggikota.go.id/api/berita";
        if (query != null && !query.trim().isEmpty()) {
            return baseUrl + "?type=lainnya&q=" + query.trim() + "&page=" + page + "&limit=" + limit;
        } else {
            return baseUrl + "?type=lainnya&page=" + page + "&limit=" + limit;
        }
    }

    private void fetchPageFromUrl(String url, boolean isInitial) {
        httpService.getBeritaDiskominfo(url).enqueue(new Callback<BeritaResponse>() {
            @Override
            public void onResponse(@NonNull Call<BeritaResponse> call, @NonNull Response<BeritaResponse> response) {
                isLoading = false;
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
                        if (isInitial) {
                            currentPage = 1;
                            adapter.setList(items);
                        } else {
                            currentPage++;
                            adapter.addList(items);
                        }
                        hasNextPage = items.size() >= limit;
                        if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);
                    } else {
                        if (isInitial) {
                            adapter.setList(null);
                            if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
                        }
                        hasNextPage = false;
                    }
                } else {
                    if (isInitial && llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BeritaResponse> call, @NonNull Throwable t) {
                isLoading = false;
                if (pbLoadingList != null) pbLoadingList.setVisibility(View.GONE);
                if (isInitial && llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }
}
