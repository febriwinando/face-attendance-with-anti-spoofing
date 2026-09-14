package go.pemkott.appsandroidmobiletebingtinggi.verifikasi.fragvalidasi.pulang;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.RekapPulangKeduaFragment;


public class AdapterPulangKeduaFragment extends RecyclerView.Adapter<AdapterPulangKeduaFragment.ListViewHolder> {


    private OnItemClickCallback onItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallback onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    Context context;
    private List<RekapPulangKeduaFragment> rekapServers;
    public AdapterPulangKeduaFragment(List<RekapPulangKeduaFragment> rekapServers, Context context) {
        this.rekapServers = rekapServers;
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

                holder.tvNamaEmployee.setText(rekapServers.get(position).getNama());
                if (rekapServers.get(position).getPosisi_pulang().equals("tl-masuk")){
                    holder.tvStatus.setText("Tugas Lapangan");
                }else if (rekapServers.get(position).getPosisi_pulang().equals("tl-pulang")){
                    holder.tvStatus.setText("Tugas Lapangan");
                }else if (rekapServers.get(position).getPosisi_pulang().equals("sk")){
                    holder.tvStatus.setText("Sakit");
                }else if (rekapServers.get(position).getPosisi_pulang().equals("pd")){
                    holder.tvStatus.setText("Perjalanan Dinas");
                }else if (rekapServers.get(position).getPosisi_pulang().equals("kp")){
                    holder.tvStatus.setText("Keperluan Pribadi");
                }else if (rekapServers.get(position).getPosisi_pulang().equals("cuti")){
                    holder.tvStatus.setText("Cuti");
                }

                if (rekapServers.get(position).getValidasi_pulang() == 3 || rekapServers.get(position).getValidasi_pulang() == 4){
                    holder.civPresensiReject.setVisibility(View.VISIBLE);
                }else{
                    holder.civPresensi.setVisibility(View.VISIBLE);
                }


        holder.cvValidasi.setOnClickListener(new View.OnClickListener() {
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

        TextView tvNamaEmployee, tvStatus;
        CardView cvValidasi;
        CircleImageView civPresensiReject, civPresensi;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNamaEmployee = itemView.findViewById(R.id.tvNamaValidasi);
            tvStatus = itemView.findViewById(R.id.txtSatusVerif);
            cvValidasi = itemView.findViewById(R.id.cvValidasi);
            civPresensiReject = itemView.findViewById(R.id.civPresensiReject);
            civPresensi = itemView.findViewById(R.id.civPresensi);

        }
    }

    public interface OnItemClickCallback {
        void onItemClicked(RekapPulangKeduaFragment data);
    }
}
