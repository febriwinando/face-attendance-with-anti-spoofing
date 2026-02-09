package go.pemkott.appsandroidmobiletebingtinggi.verifikasi;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.verifikasi.fragvalidasi.masuk.MasukFragment;
import go.pemkott.appsandroidmobiletebingtinggi.verifikasi.fragvalidasi.pulang.PulangFragment;

public class ValidasiNewActivity extends AppCompatActivity {
    FrameLayout contentView;
    BottomNavigationView menuvalidasi;
    MasukFragment masukFragment = new MasukFragment();
    PulangFragment pulangFragment = new PulangFragment();
    public static BottomNavigationView bottomNavigationView;
    private int shortAnimationDuration;

    public static AppCompatActivity validasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validasi_new);
        validasi = this;
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.putih));
        window.setNavigationBarColor(getResources().getColor(R.color.biru));


        contentView = findViewById(R.id.containervalidasi);
        menuvalidasi = findViewById(R.id.menuvalidasi);
        shortAnimationDuration = 1000;

        crossfade();
        getSupportFragmentManager().beginTransaction().replace(R.id.containervalidasi, masukFragment).commit();

        menuvalidasi.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.masukvalidasi) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.containervalidasi, masukFragment)
                        .commit();
                return true;

            } else if (id == R.id.pulangvalidasi) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.containervalidasi, pulangFragment)
                        .commit();
                return true;
            }

            return false;
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void crossfade() {
        contentView.setAlpha(0f);
        contentView.setVisibility(View.VISIBLE);


        contentView.animate()
                .alpha(10f)
                .setDuration(shortAnimationDuration)
                .setListener(null);
    }

}