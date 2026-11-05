package go.pemkott.appsandroidmobiletebingtinggi.utils;

import android.content.res.Configuration;
import android.view.WindowManager;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class WindowUtil {

    /**
     * Menerapkan gaya window & status bar yang persis seperti pada DashboardVersiOne:
     * - Full EdgeToEdge
     * - FLAG_LAYOUT_NO_LIMITS (Latar belakang meluas hingga balik status bar & nav bar)
     * - Ikon Status Bar & Navigation Bar menyesuaikan Dark/Light Mode secara otomatis
     */
    public static void applyDashboardStyle(ComponentActivity activity) {
        if (activity == null) return;

        EdgeToEdge.enable(activity);
        activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);

        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(
                activity.getWindow(),
                activity.getWindow().getDecorView()
        );

        boolean isDarkMode = (activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        controller.setAppearanceLightStatusBars(!isDarkMode);
        controller.setAppearanceLightNavigationBars(!isDarkMode);
    }
}
