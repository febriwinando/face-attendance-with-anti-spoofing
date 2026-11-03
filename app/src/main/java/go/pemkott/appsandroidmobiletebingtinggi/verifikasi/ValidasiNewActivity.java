package go.pemkott.appsandroidmobiletebingtinggi.verifikasi;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import go.pemkott.appsandroidmobiletebingtinggi.NewDashboard.DashboardVersiOne;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;
import go.pemkott.appsandroidmobiletebingtinggi.verifikasi.fragvalidasi.masuk.MasukFragment;
import go.pemkott.appsandroidmobiletebingtinggi.verifikasi.fragvalidasi.pulang.PulangFragment;

public class ValidasiNewActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private CardView btnTabMasuk, btnTabPulang;
    private TextView tvTabMasuk, tvTabPulang;

    public static AppCompatActivity validasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(
                        getWindow(),
                        getWindow().getDecorView()
                );

        boolean isDarkMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        controller.setAppearanceLightStatusBars(!isDarkMode);
        controller.setAppearanceLightNavigationBars(!isDarkMode);

        setContentView(R.layout.activity_validasi_new);

        validasi = this;

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBack();
            }
        });

        // Header Back Action
        ImageView ivBack = findViewById(R.id.ivBackValidasi);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> navigateBack());
        }

        // View Binding
        viewPager = findViewById(R.id.vpValidasi);
        btnTabMasuk = findViewById(R.id.btnTabMasuk);
        btnTabPulang = findViewById(R.id.btnTabPulang);
        tvTabMasuk = findViewById(R.id.tvTabMasuk);
        tvTabPulang = findViewById(R.id.tvTabPulang);

        // ViewPager2 Setup
        ValidasiPagerAdapter adapter = new ValidasiPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);

        // Top Switcher Click Listeners
        if (btnTabMasuk != null) {
            btnTabMasuk.setOnClickListener(v -> viewPager.setCurrentItem(0, true));
        }
        if (btnTabPulang != null) {
            btnTabPulang.setOnClickListener(v -> viewPager.setCurrentItem(1, true));
        }

        // ViewPager Page Callback
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateTabStyle(position);
            }
        });
    }



    private void navigateBack() {
        if (isTaskRoot()) {
            Intent intent = new Intent(this, DashboardVersiOne.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
        finish();
    }

    private void updateTabStyle(int position) {
        int primaryBrand = ContextCompat.getColor(this, R.color.primary_brand);
        int surfaceMain = ContextCompat.getColor(this, R.color.surface_main);
        int textHint = ContextCompat.getColor(this, R.color.text_hint);

        if (position == 0) {
            // Tab Masuk Active
            if (btnTabMasuk != null) {
                btnTabMasuk.setCardBackgroundColor(surfaceMain);
                btnTabMasuk.setCardElevation(4f);
            }
            if (btnTabPulang != null) {
                btnTabPulang.setCardBackgroundColor(Color.TRANSPARENT);
                btnTabPulang.setCardElevation(0f);
            }
            if (tvTabMasuk != null) {
                tvTabMasuk.setTextColor(primaryBrand);
            }
            if (tvTabPulang != null) {
                tvTabPulang.setTextColor(textHint);
            }
        } else {
            // Tab Pulang Active
            if (btnTabPulang != null) {
                btnTabPulang.setCardBackgroundColor(surfaceMain);
                btnTabPulang.setCardElevation(4f);
            }
            if (btnTabMasuk != null) {
                btnTabMasuk.setCardBackgroundColor(Color.TRANSPARENT);
                btnTabMasuk.setCardElevation(0f);
            }
            if (tvTabPulang != null) {
                tvTabPulang.setTextColor(primaryBrand);
            }
            if (tvTabMasuk != null) {
                tvTabMasuk.setTextColor(textHint);
            }
        }
    }

    private static class ValidasiPagerAdapter extends FragmentStateAdapter {

        public ValidasiPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 1) {
                return new PulangFragment();
            }
            return new MasukFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
