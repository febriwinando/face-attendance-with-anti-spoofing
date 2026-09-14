package go.pemkott.appsandroidmobiletebingtinggi.verifikasi;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.verifikasi.fragvalidasi.masuk.MasukFragment;
import go.pemkott.appsandroidmobiletebingtinggi.verifikasi.fragvalidasi.pulang.PulangFragment;

public class ValidasiNewActivity extends AppCompatActivity {

    private FrameLayout contentView;
    private BottomNavigationView menuvalidasi;

    private final MasukFragment masukFragment = new MasukFragment();
    private final PulangFragment pulangFragment = new PulangFragment();

    public static BottomNavigationView bottomNavigationView;
    public static AppCompatActivity validasi;

    private int shortAnimationDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_validasi_new);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        validasi = this;

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.background_main));
        window.setNavigationBarColor(getResources().getColor(R.color.background_main));

        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);

        contentView = findViewById(R.id.containervalidasi);
        menuvalidasi = findViewById(R.id.menuvalidasi);
        View cvMenuContainer = findViewById(R.id.cvMenuContainer);

        // Entrance Animation
        cvMenuContainer.setAlpha(0f);
        cvMenuContainer.setTranslationY(50f);

        cvMenuContainer.animate().alpha(1f).translationY(0f).setDuration(600).start();

        // Ambil durasi animasi bawaan Android
        shortAnimationDuration =
                getResources().getInteger(android.R.integer.config_shortAnimTime);

        // Tampilkan fragment pertama
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.containervalidasi, masukFragment)
                .commit();

        // Jalankan efek fade in
        crossfade();

        menuvalidasi.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.masukvalidasi) {

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.containervalidasi, masukFragment)
                        .commit();

                crossfade();
                return true;

            } else if (id == R.id.pulangvalidasi) {

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.containervalidasi, pulangFragment)
                        .commit();

                crossfade();
                return true;
            }

            return false;
        });
    }

    private void crossfade() {

        contentView.clearAnimation();

        contentView.setAlpha(0f);
        contentView.setVisibility(View.VISIBLE);

        contentView.animate()
                .alpha(1f) // Maksimal 1f
                .setDuration(shortAnimationDuration)
                .setListener(null)
                .start();
    }
}