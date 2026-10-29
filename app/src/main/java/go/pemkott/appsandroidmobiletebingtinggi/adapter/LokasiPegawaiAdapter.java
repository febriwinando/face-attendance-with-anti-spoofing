package go.pemkott.appsandroidmobiletebingtinggi.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.LokasiPegawaiModel;

public class LokasiPegawaiAdapter extends RecyclerView.Adapter<LokasiPegawaiAdapter.ViewHolder> {

    private final Context context;
    private List<LokasiPegawaiModel> fullList;
    private List<LokasiPegawaiModel> filteredList;

    public LokasiPegawaiAdapter(Context context, List<LokasiPegawaiModel> list) {
        this.context = context;
        this.fullList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.fullList);
    }

    public void setList(List<LokasiPegawaiModel> list) {
        this.fullList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.fullList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            String lower = query.toLowerCase().trim();
            for (LokasiPegawaiModel item : fullList) {
                if ((item.getNamaAlamat() != null && item.getNamaAlamat().toLowerCase().contains(lower)) ||
                        (item.getTipeLokasi() != null && item.getTipeLokasi().toLowerCase().contains(lower)) ||
                        (item.getLat() != null && item.getLat().contains(lower)) ||
                        (item.getLng() != null && item.getLng().contains(lower))) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lokasi_pegawai, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LokasiPegawaiModel item = filteredList.get(position);

        holder.tvNama.setText(item.getNamaAlamat() != null ? item.getNamaAlamat() : "Lokasi Kantor");
        holder.tvTipe.setText(item.getTipeLokasi() != null ? item.getTipeLokasi() : "Lokasi Presensi");
        holder.tvKoordinat.setText("Lat: " + item.getLat() + ", Lng: " + item.getLng());

        holder.btnMap.setOnClickListener(v -> {
            try {
                String geoUri = "geo:" + item.getLat() + "," + item.getLng() + "?q=" + item.getLat() + "," + item.getLng() + "(" + Uri.encode(item.getNamaAlamat()) + ")";
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                } else {
                    Intent genericMapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + item.getLat() + "," + item.getLng()));
                    context.startActivity(genericMapIntent);
                }
            } catch (Exception e) {
                Toast.makeText(context, "Gagal membuka peta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvTipe, tvKoordinat;
        MaterialButton btnMap;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvNamaLokasi);
            tvTipe = itemView.findViewById(R.id.tvTipeLokasi);
            tvKoordinat = itemView.findViewById(R.id.tvKoordinatText);
            btnMap = itemView.findViewById(R.id.btnBukaPeta);
        }
    }
}
