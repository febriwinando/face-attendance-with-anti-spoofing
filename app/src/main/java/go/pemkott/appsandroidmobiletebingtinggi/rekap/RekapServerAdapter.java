package go.pemkott.appsandroidmobiletebingtinggi.rekap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.RekapServer;

public class RekapServerAdapter extends RecyclerView.Adapter<RekapServerAdapter.ListViewHolder> {


    private OnItemClickCallback onItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallback onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    Context context;
    private List<RekapServer> rekapServers;
    public RekapServerAdapter(List<RekapServer> rekapServers, Context context) {
        this.rekapServers = rekapServers;
        this.context = context;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rekap_server, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        holder.tvTanggalRekap.setText(rekapServers.get(position).getTanggal());

        if (rekapServers.get(position).getValidasi_masuk() == 0 || rekapServers.get(position).getValidasi_masuk() == 0){
            holder.civPresensi.setVisibility(View.VISIBLE);
        }

        if (rekapServers.get(position).getValidasi_masuk() == 2 || rekapServers.get(position).getValidasi_masuk() == 2){
            holder.civPresensiVerifikasi.setVisibility(View.VISIBLE);
        }

        if (rekapServers.get(position).getValidasi_masuk() == 3 || rekapServers.get(position).getValidasi_masuk() == 3){
            holder.civPresensiTolak.setVisibility(View.VISIBLE);
        }

        if (rekapServers.get(position).getValidasi_masuk() == 4 || rekapServers.get(position).getValidasi_masuk() == 4){
            holder.civPresensiTolak.setVisibility(View.VISIBLE);
        }


        if (rekapServers.get(position).getJam_masuk() == null ){
            holder.tvRekapJamMasuk.setText("--:--");
        }else{
            holder.tvRekapJamMasuk.setText(rekapServers.get(position).getJam_masuk());
        }

        if (rekapServers.get(position).getJam_pulang() == null ){
            holder.tvRekapJamPulang.setText("--:--");
        }else{
            holder.tvRekapJamPulang.setText(rekapServers.get(position).getJam_pulang());
        }

        holder.llRekapPresensi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickCallback.onItemClicked(rekapServers.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return rekapServers.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder{

        TextView tvTanggalRekap, tvRekapJamMasuk, tvRekapJamPulang, tvWaktuPresenceRekapServer;
        LinearLayout llRekapPresensi;
        CircleImageView civPresensi, civPresensiVerifikasi, civPresensiTolak;


        public ListViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTanggalRekap = itemView.findViewById(R.id.tvTanggalRekap);
            tvRekapJamMasuk = itemView.findViewById(R.id.tvRekapJamMasuk);
            tvRekapJamPulang = itemView.findViewById(R.id.tvRekapJamPulang);
            llRekapPresensi = itemView.findViewById(R.id.llRekapPresensi);
            civPresensi = itemView.findViewById(R.id.civPresensi);
            civPresensiVerifikasi = itemView.findViewById(R.id.civPresensiVerifikasi);
            civPresensiTolak = itemView.findViewById(R.id.civPresensiTolak);

        }
    }

    public interface OnItemClickCallback {
        void onItemClicked(RekapServer data);
    }
}
