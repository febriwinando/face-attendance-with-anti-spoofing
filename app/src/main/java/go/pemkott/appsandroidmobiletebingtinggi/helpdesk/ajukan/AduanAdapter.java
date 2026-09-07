package go.pemkott.appsandroidmobiletebingtinggi.helpdesk.ajukan;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat;
import go.pemkott.appsandroidmobiletebingtinggi.model.AduanHelpdesk;
import go.pemkott.appsandroidmobiletebingtinggi.utils.ClsGlobal;

public class AduanAdapter extends RecyclerView.Adapter<AduanAdapter.ViewHolder> {

    private final List<AduanHelpdesk> aduans = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AduanHelpdesk aduan);
    }

    public AduanAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setAduans(List<AduanHelpdesk> newAduans) {
        aduans.clear();
        if (newAduans != null) {
            aduans.addAll(newAduans);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_status_aduan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AduanHelpdesk aduan = aduans.get(position);
        holder.bind(aduan, listener);
    }

    @Override
    public int getItemCount() {
        return aduans.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomor, tvStatus, tvJudul, tvKategori, tvTanggal, tvPrioritas;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomor = itemView.findViewById(R.id.tvNomorAduan);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvJudul = itemView.findViewById(R.id.tvJudulAduan);
            tvKategori = itemView.findViewById(R.id.tvKategori);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvPrioritas = itemView.findViewById(R.id.tvPrioritas);
        }

        public void bind(AduanHelpdesk aduan, OnItemClickListener listener) {
            tvNomor.setText(aduan.getNomor());
            tvJudul.setText(aduan.getJudul());
            tvKategori.setText(ClsGlobal.capitalizeEveryWord(aduan.getKategori().replace("_", " ")));
            tvTanggal.setText("Waktu Pelaporan: " + TimeFormat.formatTimestampAduan(aduan.getCreatedAt()));
            tvPrioritas.setText("Tingkat Prioritas: " + ClsGlobal.capitalizeEveryWord(aduan.getPrioritas()));
            
            // Status Styling
            String status = aduan.getStatus();
            tvStatus.setText(status.replace("_", " ").toUpperCase());
            
            int color;
            Context context = itemView.getContext();
            switch (status) {
                case "baru":
                    color = ContextCompat.getColor(context, R.color.primary_brand);
                    break;
                case "diproses":
                    color = ContextCompat.getColor(context, R.color.kuning);
                    break;
                case "diteruskan_kota":
                    color = Color.parseColor("#9C27B0"); // Purple
                    break;
                case "selesai":
                    color = ContextCompat.getColor(context, R.color.hijau);
                    break;
                case "ditolak":
                    color = ContextCompat.getColor(context, R.color.merah);
                    break;
                default:
                    color = ContextCompat.getColor(context, R.color.text_hint);
            }
            tvStatus.setBackgroundTintList(ColorStateList.valueOf(color));

            itemView.setOnClickListener(v -> listener.onItemClick(aduan));
        }
    }
}
