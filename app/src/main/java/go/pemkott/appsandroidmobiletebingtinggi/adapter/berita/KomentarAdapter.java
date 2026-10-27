package go.pemkott.appsandroidmobiletebingtinggi.adapter.berita;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.berita.KomentarItem;

public class KomentarAdapter extends RecyclerView.Adapter<KomentarAdapter.ViewHolder> {

    private final List<KomentarItem> list = new ArrayList<>();

    public void setList(List<KomentarItem> items) {
        list.clear();
        if (items != null) {
            list.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_komentar_berita, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        KomentarItem item = list.get(position);
        holder.tvNamaKomentar.setText(item.getNama());
        holder.tvIsiKomentar.setText(item.getIsi());

        String dateStr = item.getCreatedAt();
        if (dateStr != null && dateStr.contains("T")) {
            dateStr = dateStr.split("T")[0];
        }
        holder.tvWaktuKomentar.setText(dateStr != null ? dateStr : "");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaKomentar, tvWaktuKomentar, tvIsiKomentar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamaKomentar = itemView.findViewById(R.id.tvNamaKomentar);
            tvWaktuKomentar = itemView.findViewById(R.id.tvWaktuKomentar);
            tvIsiKomentar = itemView.findViewById(R.id.tvIsiKomentar);
        }
    }
}
