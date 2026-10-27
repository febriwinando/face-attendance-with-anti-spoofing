package go.pemkott.appsandroidmobiletebingtinggi.berita;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.NewDashboard.DashboardVersiOne;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.berita.KomentarAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.model.berita.BeritaItem;
import go.pemkott.appsandroidmobiletebingtinggi.model.berita.BeritaResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.berita.KomentarItem;
import go.pemkott.appsandroidmobiletebingtinggi.model.berita.KomentarResponse;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailBeritaActivity extends AppCompatActivity {

    public static final String EXTRA_SLUG = "extra_slug";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_THUMBNAIL = "extra_thumbnail";

    private ImageView ivHeaderThumbnail;
    private MaterialCardView cvBack, cvShare;
    private RelativeLayout rlDetailHeader;
    private TextView tvDetailCategory, tvDetailViews, tvDetailTitle, tvDetailAuthor, tvDetailDate, tvDetailSummary, tvDetailContent;
    private TextView tvHeaderKomentar, tvEmptyKomentar;
    private EditText etKomentarBaru;
    private MaterialButton btnKirimKomentar;
    private RecyclerView rvKomentar;
    private KomentarAdapter komentarAdapter;

    private CardView cvSummary;
    private ProgressBar pbLoadingDetail;
    private NestedScrollView nsvDetailContent;

    private HttpService httpService;
    private String slug, title, thumbnailUrl, webUrl;
    private long beritaId = 0;

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

        setContentView(R.layout.activity_detail_berita);

        httpService = RetroClient.getInstance().getApi2();

        rlDetailHeader = findViewById(R.id.rlDetailHeader);
        cvBack = findViewById(R.id.cvBack);
        cvShare = findViewById(R.id.cvShare);
        ivHeaderThumbnail = findViewById(R.id.ivHeaderThumbnail);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailViews = findViewById(R.id.tvDetailViews);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailAuthor = findViewById(R.id.tvDetailAuthor);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvDetailSummary = findViewById(R.id.tvDetailSummary);
        tvDetailContent = findViewById(R.id.tvDetailContent);
        cvSummary = findViewById(R.id.cvSummary);
        pbLoadingDetail = findViewById(R.id.pbLoadingDetail);

        nsvDetailContent = findViewById(R.id.nsvDetailContent);
        tvHeaderKomentar = findViewById(R.id.tvHeaderKomentar);
        tvEmptyKomentar = findViewById(R.id.tvEmptyKomentar);
        etKomentarBaru = findViewById(R.id.etKomentarBaru);
        btnKirimKomentar = findViewById(R.id.btnKirimKomentar);
        rvKomentar = findViewById(R.id.rvKomentar);

        if (rvKomentar != null) {
            rvKomentar.setLayoutManager(new LinearLayoutManager(this));
            komentarAdapter = new KomentarAdapter();
            rvKomentar.setAdapter(komentarAdapter);
        }

        if (btnKirimKomentar != null) {
            btnKirimKomentar.setOnClickListener(v -> kirimKomentar());
        }

        if (etKomentarBaru != null) {
            etKomentarBaru.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && nsvDetailContent != null) {
                    nsvDetailContent.postDelayed(() -> nsvDetailContent.fullScroll(View.FOCUS_DOWN), 300);
                }
            });
            etKomentarBaru.setOnClickListener(v -> {
                if (nsvDetailContent != null) {
                    nsvDetailContent.postDelayed(() -> nsvDetailContent.fullScroll(View.FOCUS_DOWN), 300);
                }
            });
        }

        View clMainDetail = findViewById(R.id.clMainDetail);
        if (clMainDetail != null && rlDetailHeader != null) {
            ViewCompat.setOnApplyWindowInsetsListener(clMainDetail, (v, insets) -> {
                int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                int bottomInsets = insets.getInsets(WindowInsetsCompat.Type.ime() | WindowInsetsCompat.Type.navigationBars()).bottom;

                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rlDetailHeader.getLayoutParams();
                params.topMargin = statusBarHeight;
                rlDetailHeader.setLayoutParams(params);

                clMainDetail.setPadding(0, 0, 0, bottomInsets);

                return insets;
            });
        }

        slug = getIntent().getStringExtra(EXTRA_SLUG);
        title = getIntent().getStringExtra(EXTRA_TITLE);
        thumbnailUrl = getIntent().getStringExtra(EXTRA_THUMBNAIL);

        cvBack.setOnClickListener(v -> finish());

        cvShare.setOnClickListener(v -> {
            if (title != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareBody = title + "\n\nSelengkapnya: " + (webUrl != null ? webUrl : "https://diskominfo.tebingtinggikota.go.id");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(shareIntent, "Bagikan Berita"));
            }
        });

        if (title != null) {
            tvDetailTitle.setText(title);
        }

        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            Glide.with(this)
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.one_person)
                    .error(R.drawable.one_person)
                    .into(ivHeaderThumbnail);
        }

        if (slug != null && !slug.isEmpty()) {
            loadBeritaDetail(slug);
        } else {
            Toast.makeText(this, "Berita tidak ditemukan", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBeritaDetail(String slug) {
        if (pbLoadingDetail != null) pbLoadingDetail.setVisibility(View.VISIBLE);
        String newsUrl = "https://diskominfo.tebingtinggikota.go.id/api/berita?slug=" + slug;
        fetchDetailFromUrl(newsUrl);
    }

    private void fetchDetailFromUrl(String url) {
        httpService.getBeritaDiskominfo(url).enqueue(new Callback<BeritaResponse>() {
            @Override
            public void onResponse(@NonNull Call<BeritaResponse> call, @NonNull Response<BeritaResponse> response) {
                if (pbLoadingDetail != null) pbLoadingDetail.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    BeritaItem detail = response.body().getDetailItem();
                    if (detail != null) {
                        displayDetailData(detail);
                    } else {
                        Toast.makeText(DetailBeritaActivity.this, "Gagal memuat detail berita", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DetailBeritaActivity.this, "Gagal memuat detail berita", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BeritaResponse> call, @NonNull Throwable t) {
                if (pbLoadingDetail != null) pbLoadingDetail.setVisibility(View.GONE);
                Toast.makeText(DetailBeritaActivity.this, "Kendala koneksi ke server berita", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayDetailData(BeritaItem item) {
        beritaId = item.getId();
        webUrl = item.getWebUrl();
        tvDetailTitle.setText(item.getTitle());
        tvDetailAuthor.setText(item.getAuthor());
        tvDetailDate.setText(item.getDateFormatted());
        tvDetailViews.setText(item.getViews() + "x dilihat");

        if (item.getCategory() != null && !item.getCategory().isEmpty()) {
            tvDetailCategory.setText(item.getCategory().toUpperCase());
        }

        String img = item.getThumbnailUrl();
        if (img != null && !img.isEmpty()) {
            Glide.with(this)
                    .load(img)
                    .placeholder(R.drawable.one_person)
                    .error(R.drawable.one_person)
                    .into(ivHeaderThumbnail);
        }

        if (item.getSummary() != null && !item.getSummary().trim().isEmpty()) {
            cvSummary.setVisibility(View.VISIBLE);
            tvDetailSummary.setText(item.getSummary().trim());
        } else {
            cvSummary.setVisibility(View.GONE);
        }

        String contentHtml = item.getContent();
        if (contentHtml != null && !contentHtml.trim().isEmpty()) {
            Spanned spanned;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                spanned = Html.fromHtml(contentHtml, Html.FROM_HTML_MODE_LEGACY);
            } else {
                spanned = Html.fromHtml(contentHtml);
            }
            tvDetailContent.setText(spanned);
        } else {
            tvDetailContent.setText(item.getSummary());
        }

        if (beritaId > 0) {
            loadKomentar(beritaId);
        }
    }

    private void loadKomentar(long bId) {
        if (bId <= 0) return;
        String commentsUrl = "https://diskominfo.tebingtinggikota.go.id/api/berita/komentar?beritaId=" + bId;
        fetchKomentarFromUrl(commentsUrl);
    }

    private void fetchKomentarFromUrl(String url) {
        httpService.getKomentarBerita(url).enqueue(new Callback<KomentarResponse>() {
            @Override
            public void onResponse(@NonNull Call<KomentarResponse> call, @NonNull Response<KomentarResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<KomentarItem> comments = response.body().getData();
                    int total = response.body().getTotal();
                    if (tvHeaderKomentar != null) {
                        tvHeaderKomentar.setText("Komentar (" + total + ")");
                    }
                    if (comments != null && !comments.isEmpty()) {
                        if (tvEmptyKomentar != null) tvEmptyKomentar.setVisibility(View.GONE);
                        if (rvKomentar != null) rvKomentar.setVisibility(View.VISIBLE);
                        if (komentarAdapter != null) komentarAdapter.setList(comments);
                    } else {
                        if (tvEmptyKomentar != null) tvEmptyKomentar.setVisibility(View.VISIBLE);
                        if (rvKomentar != null) rvKomentar.setVisibility(View.GONE);
                    }
                } else {
                    if (tvEmptyKomentar != null) tvEmptyKomentar.setVisibility(View.VISIBLE);
                    if (rvKomentar != null) rvKomentar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<KomentarResponse> call, @NonNull Throwable t) {
                if (tvEmptyKomentar != null) tvEmptyKomentar.setVisibility(View.VISIBLE);
                if (rvKomentar != null) rvKomentar.setVisibility(View.GONE);
            }
        });
    }

    private void kirimKomentar() {
        if (beritaId <= 0) {
            Toast.makeText(this, "ID Berita tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }
        String text = etKomentarBaru != null ? etKomentarBaru.getText().toString().trim() : "";
        if (text.isEmpty()) {
            Toast.makeText(this, "Isi komentar tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        String namaPegawai = DashboardVersiOne.sUsername != null && !DashboardVersiOne.sUsername.trim().isEmpty() ? DashboardVersiOne.sUsername : "Pegawai Pemko Tebing Tinggi";
        String nipPegawai = DashboardVersiOne.sNip != null && !DashboardVersiOne.sNip.trim().isEmpty() ? DashboardVersiOne.sNip : "pegawai";
        String emailPegawai = nipPegawai + "@tebingtinggikota.go.id";

        try {
            JSONObject json = new JSONObject();
            json.put("beritaId", beritaId);
            json.put("nama", namaPegawai);
            json.put("email", emailPegawai);
            json.put("kontak", "08123456789");
            json.put("isi", text);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            if (btnKirimKomentar != null) btnKirimKomentar.setEnabled(false);

            String postCommentUrl = "https://diskominfo.tebingtinggikota.go.id/api/berita/komentar";
            postKomentarFromUrl(postCommentUrl, body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void postKomentarFromUrl(String url, RequestBody body) {
        httpService.postKomentarBerita(url, body).enqueue(new Callback<KomentarResponse>() {
            @Override
            public void onResponse(@NonNull Call<KomentarResponse> call, @NonNull Response<KomentarResponse> response) {
                if (btnKirimKomentar != null) btnKirimKomentar.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(DetailBeritaActivity.this, "Komentar berhasil dikirim!", Toast.LENGTH_SHORT).show();
                    if (etKomentarBaru != null) etKomentarBaru.setText("");
                    loadKomentar(beritaId);
                    if (nsvDetailContent != null) {
                        nsvDetailContent.postDelayed(() -> nsvDetailContent.fullScroll(View.FOCUS_DOWN), 400);
                    }
                } else {
                    Toast.makeText(DetailBeritaActivity.this, "Gagal mengirim komentar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<KomentarResponse> call, @NonNull Throwable t) {
                if (btnKirimKomentar != null) btnKirimKomentar.setEnabled(true);
                Toast.makeText(DetailBeritaActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
