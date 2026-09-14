package go.pemkott.appsandroidmobiletebingtinggi.helpdesk.log;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.LogAktivitas;

public class LogAktivitasAdapter extends RecyclerView.Adapter<LogAktivitasAdapter.ViewHolder> {

    private List<LogAktivitas> logs = new ArrayList<>();
    private List<LogAktivitas> logsFull = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.forLanguageTag("id-ID"));

    public void setLogs(List<LogAktivitas> newLogs) {
        this.logs = new ArrayList<>(newLogs);
        this.logsFull = new ArrayList<>(newLogs);
        notifyDataSetChanged();
    }

    public void filter(String query, String date) {
        logs.clear();
        if (query.isEmpty() && date.isEmpty()) {
            logs.addAll(logsFull);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (LogAktivitas log : logsFull) {
                boolean matchQuery = true;
                boolean matchDate = true;

                if (!lowerCaseQuery.isEmpty()) {
                    String kegiatan = log.getKegiatan() != null ? log.getKegiatan().toLowerCase() : "";
                    String nama = log.getEmployeeName() != null ? log.getEmployeeName().toLowerCase() : "";
                    matchQuery = kegiatan.contains(lowerCaseQuery) || nama.contains(lowerCaseQuery);
                }

                if (!date.isEmpty()) {
                    if (log.getTanggal() != null) {
                        matchDate = log.getTanggal().equals(date);
                    } else {
                        matchDate = false;
                    }
                }

                if (matchQuery && matchDate) {
                    logs.add(log);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log_aktivitas, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LogAktivitas log = logs.get(position);
        Context context = holder.itemView.getContext();

        holder.tvKegiatan.setText(log.getKegiatan());
        
        // Format Date for better readability
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"));
            Date date = dbFormat.parse(log.getTanggal());
            holder.tvTanggal.setText(displayFormat.format(date));
        } catch (Exception e) {
            holder.tvTanggal.setText(log.getTanggal());
        }

        holder.tvEmployeeName.setText(log.getEmployeeName());
        holder.tvOpdName.setText(log.getOpdName());
        
        String deviceId = log.getDeviceId() != null ? log.getDeviceId() : "-";
        holder.tvDevice.setText(String.format("ID Perangkat: %s", deviceId));

        try {
            long ts = Long.parseLong(log.getTimestamp());
            holder.tvTimestamp.setText(String.format("Waktu Sistem: %s", timeFormat.format(new Date(ts))));
        } catch (Exception e) {
            holder.tvTimestamp.setText(String.format("Waktu Sistem: %s", log.getTimestamp()));
        }

        // Status Styling based on Jenis Absen
        int color;
        int iconRes;
        String jenis = log.getJenisAbsen() != null ? log.getJenisAbsen().toLowerCase() : "";

        if (jenis.contains("masuk")) {
            color = ContextCompat.getColor(context, R.color.biru);
            iconRes = R.drawable.ic_masuk_kanan;
        } else if (jenis.contains("pulang")) {
            color = ContextCompat.getColor(context, R.color.brand_secondary);
            iconRes = R.drawable.ic_pulang_kiri;
        } else if (jenis.contains("login")) {
            color = ContextCompat.getColor(context, R.color.hijau);
            iconRes = R.drawable.ic_baseline_person_24;
        } else if (jenis.contains("logout")) {
            color = ContextCompat.getColor(context, R.color.merah);
            iconRes = R.drawable.ic_power_off;
        } else if (jenis.contains("cuti") || jenis.contains("izin") || jenis.contains("sakit")) {
            color = ContextCompat.getColor(context, R.color.kuning);
            iconRes = R.drawable.ic_artikel;
        } else if (jenis.contains("perjalanan") || jenis.contains("pd")) {
            color = ContextCompat.getColor(context, R.color.biru);
            iconRes = R.drawable.ic_search_location;
        } else if (jenis.contains("tugas") || jenis.contains("lapangan")) {
            color = ContextCompat.getColor(context, R.color.biru);
            iconRes = R.drawable.ic_search_location; // Or a specific field icon if available
        } else if (jenis.contains("sinkronisasi") || jenis.contains("sync")) {
            color = ContextCompat.getColor(context, R.color.abuabu_font);
            iconRes = R.drawable.ic_clock_line;
        } else {
            color = ContextCompat.getColor(context, R.color.primary_brand);
            iconRes = R.drawable.ic_baseline_list_alt_24;
        }

        holder.vStatusAccent.setBackgroundTintList(ColorStateList.valueOf(color));
        holder.ivIcon.setImageResource(iconRes);
        holder.ivIcon.setImageTintList(ColorStateList.valueOf(color));
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvKegiatan, tvTanggal, tvEmployeeName, tvOpdName, tvDevice, tvTimestamp;
        View vStatusAccent;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKegiatan = itemView.findViewById(R.id.tvLogKegiatan);
            tvTanggal = itemView.findViewById(R.id.tvLogTanggal);
            tvEmployeeName = itemView.findViewById(R.id.tvLogPegawai);
            tvOpdName = itemView.findViewById(R.id.tvLogInstansi);
            tvDevice = itemView.findViewById(R.id.tvLogDevice);
            tvTimestamp = itemView.findViewById(R.id.tvLogTimestamp);
            vStatusAccent = itemView.findViewById(R.id.vLogStatusAccent);
            ivIcon = itemView.findViewById(R.id.ivLogIcon);
        }
    }
}
