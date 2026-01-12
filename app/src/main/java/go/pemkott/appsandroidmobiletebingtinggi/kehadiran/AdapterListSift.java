package go.pemkott.appsandroidmobiletebingtinggi.kehadiran;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.Jadwal;


public class AdapterListSift extends  RecyclerView.Adapter<AdapterListSift.ListViewHolder>{

    private OnItemClickCallback onItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallback onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }
    private ArrayList<Jadwal> jadwals;

    public AdapterListSift(ArrayList<Jadwal>jadwalSifts) {
        this.jadwals = jadwalSifts;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_sift, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        Jadwal jadwal = jadwals.get(position);
        holder.tvJadwalSift.setText(jadwal.getInisial()+" | "+jadwal.getMasuk()+" - "+jadwal.getPulang());
        holder.imgCheckedJadwal.setVisibility(jadwal.isChecked()? View.VISIBLE : View.GONE);

        holder.rlJadwalSift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jadwal.setChecked(!jadwal.isChecked());
                holder.imgCheckedJadwal.setVisibility(jadwal.isChecked() ? View.VISIBLE : View.GONE);
                onItemClickCallback.onItemClicked(jadwals.get(holder.getAdapterPosition()));
            }
        });

    }

    @Override
    public int getItemCount() {
        return jadwals.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder{

        TextView tvJadwalSift;
        LinearLayout imgCheckedJadwal;
        LinearLayout rlJadwalSift;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            rlJadwalSift = itemView.findViewById(R.id.rlJadwalSift);
            tvJadwalSift = itemView.findViewById(R.id.tvJadwalSift);
            imgCheckedJadwal = itemView.findViewById(R.id.imgCheckedJadwal);
        }
    }

    public interface OnItemClickCallback {
        void onItemClicked(Jadwal data);
    }
}
