package go.pemkott.appsandroidmobiletebingtinggi.verifikasi.fragvalidasi.pulang;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.RekapPulangKeduaFragment;

public class AdapterPulangKeduaFragment extends RecyclerView.Adapter<AdapterPulangKeduaFragment.ListViewHolder> {

    private OnItemClickCallback onItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallback callback) {
        this.onItemClickCallback = callback;
    }

    private final Context context;
    private List<RekapPulangKeduaFragment> rekapServers;

    public AdapterPulangKeduaFragment(List<RekapPulangKeduaFragment> rekapServers, Context context) {
        this.rekapServers = rekapServers != null ? rekapServers : new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_validasi_new_kedua, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        RekapPulangKeduaFragment item = rekapServers.get(position);

        holder.tvNamaEmployee.setText(item.getNama() != null ? item.getNama() : "-");

        String tgl = item.getTanggal() != null ? item.getTanggal() : "";
        String jam = item.getJam_pulang() != null ? item.getJam_pulang() : "";
        holder.tvTanggalJam.setText("📅 " + tgl + " • " + jam + " WIB");

        String posisi = item.getPosisi_pulang();
        String katName = "Presensi";
        if ("tl-masuk".equals(posisi) || "tl-pulang".equals(posisi) || "tl".equals(posisi) || "luar".equals(posisi)) {
            katName = "Tugas Lapangan";
        } else if ("sk".equals(posisi)) {
            katName = "Sakit";
        } else if ("pd".equals(posisi)) {
            katName = "Perjalanan Dinas";
        } else if ("kp".equals(posisi)) {
            katName = "Keperluan Pribadi";
        } else if ("cuti".equals(posisi) || "ct".equals(posisi)) {
            katName = "Cuti";
        }

        if (holder.tvStatus != null) {
            holder.tvStatus.setText(katName);
        }

        String ket = item.getKet_pulang() != null && !item.getKet_pulang().trim().isEmpty() ? item.getKet_pulang().trim() : katName;
        holder.tvAktivitas.setText("Keterangan: " + ket);

        if (item.getValidasi_pulang() == 3 || item.getValidasi_pulang() == 4) {
            holder.civPresensiReject.setVisibility(View.VISIBLE);
            holder.civPresensi.setVisibility(View.GONE);
        } else {
            holder.civPresensi.setVisibility(View.VISIBLE);
            holder.civPresensiReject.setVisibility(View.GONE);
        }

        if (holder.btnVerifikasi != null) {
            holder.btnVerifikasi.setOnClickListener(v -> {
                if (onItemClickCallback != null) onItemClickCallback.onItemClicked(item);
            });
        }

        holder.cvValidasi.setOnClickListener(v -> {
            if (onItemClickCallback != null) onItemClickCallback.onItemClicked(item);
        });
    }

    @Override
    public int getItemCount() {
        return rekapServers != null ? rekapServers.size() : 0;
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {

        TextView tvNamaEmployee, tvStatus, tvTanggalJam, tvAktivitas;
        CardView cvValidasi;
        CircleImageView civPresensiReject, civPresensi;
        MaterialButton btnVerifikasi;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNamaEmployee = itemView.findViewById(R.id.tvNamaValidasi);
            tvStatus = itemView.findViewById(R.id.txtSatusVerif);
            tvTanggalJam = itemView.findViewById(R.id.tvTanggalJamValidasi);
            tvAktivitas = itemView.findViewById(R.id.tvAktivitasValidasi);
            cvValidasi = itemView.findViewById(R.id.cvValidasi);
            civPresensiReject = itemView.findViewById(R.id.civPresensiReject);
            civPresensi = itemView.findViewById(R.id.civPresensi);
            btnVerifikasi = itemView.findViewById(R.id.btnVerifikasiCard);
        }
    }

    public interface OnItemClickCallback {
        void onItemClicked(RekapPulangKeduaFragment data);
    }
}
