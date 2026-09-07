package go.pemkott.appsandroidmobiletebingtinggi.helpdesk.log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.LogAktivitas;

public class LogAktivitasAdapter extends RecyclerView.Adapter<LogAktivitasAdapter.ViewHolder> {

    private List<LogAktivitas> logs = new ArrayList<>();

    public void setLogs(List<LogAktivitas> newLogs) {
        this.logs = newLogs;
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
        holder.tvKegiatan.setText(log.getKegiatan());
        holder.tvTanggal.setText(log.getTanggal());
        holder.tvIds.setText(log.getEmployeeName() + " | " + log.getOpdName());
        holder.tvDevice.setText("Identitas Perangkat: " + log.getDeviceId());
        holder.tvTimestamp.setText("Waktu Sistem: " + log.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvKegiatan, tvTanggal, tvIds, tvDevice, tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKegiatan = itemView.findViewById(R.id.tvLogKegiatan);
            tvTanggal = itemView.findViewById(R.id.tvLogTanggal);
            tvIds = itemView.findViewById(R.id.tvLogIds);
            tvDevice = itemView.findViewById(R.id.tvLogDevice);
            tvTimestamp = itemView.findViewById(R.id.tvLogTimestamp);
        }
    }
}
