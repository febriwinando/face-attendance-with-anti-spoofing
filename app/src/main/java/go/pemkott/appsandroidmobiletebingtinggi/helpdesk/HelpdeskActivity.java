package go.pemkott.appsandroidmobiletebingtinggi.helpdesk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.helpdesk.ajukan.AjukanAduanActivity;
import go.pemkott.appsandroidmobiletebingtinggi.helpdesk.ajukan.StatusAduanActivity;
import go.pemkott.appsandroidmobiletebingtinggi.helpdesk.log.LogAktivitasActivity;
import go.pemkott.appsandroidmobiletebingtinggi.helpdesk.panduan.PanduanHelpdeskActivity;

public class HelpdeskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_helpdesk);

        View mainView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Tombol Kembali
        findViewById(R.id.rlBack).setOnClickListener(v -> finish());

        // Menu 1: Mengajukan aduan/permohonan bantuan
        findViewById(R.id.llAjukanAduan).setOnClickListener(v -> {
            startActivity(new Intent(this, AjukanAduanActivity.class));
        });

        // Menu 2: Panduan helpdesk
        findViewById(R.id.llPanduanHelpdesk).setOnClickListener(v -> {
            startActivity(new Intent(this, PanduanHelpdeskActivity.class));
        });

        // Menu 3: Cek aduan/permohonan bantuan
        findViewById(R.id.llCekAduan).setOnClickListener(v -> {
            startActivity(new Intent(this, StatusAduanActivity.class));
        });

        // Menu 4: Log Aktivitas
        findViewById(R.id.llLogAktivitas).setOnClickListener(v -> {
            startActivity(new Intent(this, LogAktivitasActivity.class));
        });
    }
}
