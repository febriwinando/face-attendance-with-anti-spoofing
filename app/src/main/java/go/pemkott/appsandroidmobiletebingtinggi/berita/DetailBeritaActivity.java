package go.pemkott.appsandroidmobiletebingtinggi.berita;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
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

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.model.berita.BeritaItem;
import go.pemkott.appsandroidmobiletebingtinggi.model.berita.BeritaResponse;
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
    private CardView cvSummary;
    private ProgressBar pbLoadingDetail;

    private HttpService httpService;
    private String slug, title, thumbnailUrl, webUrl;

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

        View clMainDetail = findViewById(R.id.clMainDetail);
        if (clMainDetail != null && rlDetailHeader != null) {
            ViewCompat.setOnApplyWindowInsetsListener(clMainDetail, (v, insets) -> {
                int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rlDetailHeader.getLayoutParams();
                params.topMargin = statusBarHeight;
                rlDetailHeader.setLayoutParams(params);
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

        String localUrl = "http://172.15.1.13:4321/api/berita?slug=" + slug;
        String publicUrl = "https://diskominfo.tebingtinggikota.go.id/api/berita?slug=" + slug;

        fetchDetailFromUrl(localUrl, () -> fetchDetailFromUrl(publicUrl, null));
    }

    private void fetchDetailFromUrl(String url, Runnable onFailureFallback) {
        httpService.getBeritaDiskominfo(url).enqueue(new Callback<BeritaResponse>() {
            @Override
            public void onResponse(@NonNull Call<BeritaResponse> call, @NonNull Response<BeritaResponse> response) {
                if (pbLoadingDetail != null) pbLoadingDetail.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    BeritaItem detail = response.body().getDetailItem();
                    if (detail != null) {
                        displayDetailData(detail);
                    } else if (onFailureFallback != null) {
                        onFailureFallback.run();
                    }
                } else if (onFailureFallback != null) {
                    onFailureFallback.run();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BeritaResponse> call, @NonNull Throwable t) {
                if (onFailureFallback != null) {
                    onFailureFallback.run();
                } else {
                    if (pbLoadingDetail != null) pbLoadingDetail.setVisibility(View.GONE);
                }
            }
        });
    }

    private void displayDetailData(BeritaItem item) {
        webUrl = item.getWebUrl();
        tvDetailTitle.setText(item.getTitle());
        tvDetailAuthor.setText(item.getAuthor());
        tvDetailDate.setText(item.getDateFormatted());
        tvDetailViews.setText("👁️ " + item.getViews() + "x dilihat");

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
    }
}
