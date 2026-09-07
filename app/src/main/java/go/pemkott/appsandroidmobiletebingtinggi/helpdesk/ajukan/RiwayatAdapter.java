package go.pemkott.appsandroidmobiletebingtinggi.helpdesk.ajukan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat;
import go.pemkott.appsandroidmobiletebingtinggi.model.AduanRiwayat;

public class RiwayatAdapter extends RecyclerView.Adapter<RiwayatAdapter.ViewHolder> {

    private final List<AduanRiwayat> riwayats = new ArrayList<>();

    public void setRiwayats(List<AduanRiwayat> newRiwayats) {
        riwayats.clear();
        if (newRiwayats != null) {
            riwayats.addAll(newRiwayats);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_riwayat_aduan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AduanRiwayat riwayat = riwayats.get(position);
        holder.tvAksi.setText(riwayat.getAksi());
        holder.tvCatatan.setText(riwayat.getCatatan());
        holder.tvAktor.setText(riwayat.getPeran() + " (" + riwayat.getAktor() + ")");
        holder.tvWaktu.setText(TimeFormat.formatTimestampAduan(riwayat.getCreatedAt()));

        // Hide timeline part for first/last items if needed, or just let it be.
        holder.vTimelineLine.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return riwayats.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAksi, tvCatatan, tvAktor, tvWaktu;
        View vTimelineLine;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAksi = itemView.findViewById(R.id.tvAksi);
            tvCatatan = itemView.findViewById(R.id.tvCatatan);
            tvAktor = itemView.findViewById(R.id.tvAktor);
            tvWaktu = itemView.findViewById(R.id.tvWaktu);
            vTimelineLine = itemView.findViewById(R.id.vTimelineLine);
        }
    }
}
