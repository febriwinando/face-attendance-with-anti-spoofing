package go.pemkott.appsandroidmobiletebingtinggi.helpdesk.panduan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import go.pemkott.appsandroidmobiletebingtinggi.R;

public class PanduanHelpdeskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_panduan_helpdesk);

        View mainView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.rlBack).setOnClickListener(v -> finish());

        setupPanduanList();
    }

    private void setupPanduanList() {
        LinearLayout container = findViewById(R.id.llPanduanContainer);
        LayoutInflater inflater = LayoutInflater.from(this);

        String[][] items = {
                {"Kendala Otentikasi & Akun", "• Gagal Login: Lupa kata sandi, akun terkunci, atau kredensial tidak terdeteksi.\n• Masalah Biometrik: Foto atau lokasi tidak berhasil terdeteksi.\n• Perubahan Data Profil: Pengajuan perubahan nomor telepon, email, atau perangkat terdaftar."},
                {"Masalah Presensi & Fitur Absensi", "• Gagal Catat Absen: Tombol absen tidak merespons, muncul eror saat klik absen masuk/keluar, atau status absen tidak tersimpan.\n• Kendala Lokasi (GPS): Lokasi pengguna terdeteksi di luar radius (geofencing), GPS tidak akurat, atau peta tidak muncul.\n• Masalah Selfie/Foto: Kamera aplikasi tidak bisa terbuka, hasil foto corrupt, atau ukuran berkas terlalu besar.\n• Selisih Jam Kerja: Jam pada aplikasi tidak sesuai dengan waktu server."},
                {"Pengajuan Izin, Cuti, & Koreksi", "• Pengajuan Cuti/Izin: Gagal mengunggah dokumen pendukung (surat dokter/dinas) atau form tidak bisa dikirim.\n• Koreksi Absensi: Pengajuan lupa absen (lupa clock-in/out) tidak masuk ke sistem supervisor.\n• Kendala Approval: Atasan tidak menerima notifikasi pengajuan cuti/izin bawahan."},
                {"Gangguan Sistem & Teknis", "• Aplikasi Crash / Freeze: Aplikasi tiba-tiba keluar sendiri atau berhenti merespons.\n• Eror Server / Koneksi: Muncul pesan eror jaringan, timeout, atau kode eror server.\n• Lambat (Lag): Proses pemuatan memakan waktu sangat lama.\n• Masalah Notifikasi: Pengingat absen tidak muncul di perangkat."},
                {"Kendala Laporan & Data Rekap", "• Ketidaksesuaian Data: Jumlah jam kerja atau sisa kuota cuti tidak sesuai.\n• Gagal Unduh Laporan: Rekap format PDF/Excel tidak bisa diunduh."},
                {"Kendala lainnya", "• Permasalahan yang tidak termasuk kategori di atas. Uraikan secara jelas pada deskripsi."}
        };

        for (String[] item : items) {
            View itemView = inflater.inflate(R.layout.item_panduan_helpdesk, container, false);
            TextView tvLabel = itemView.findViewById(R.id.tvCategoryLabel);
            TextView tvPoints = itemView.findViewById(R.id.tvCategoryPoints);

            tvLabel.setText(item[0]);
            tvPoints.setText(item[1]);

            container.addView(itemView);
        }
    }
}
