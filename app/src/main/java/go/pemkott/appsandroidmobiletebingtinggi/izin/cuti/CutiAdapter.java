package go.pemkott.appsandroidmobiletebingtinggi.izin.cuti;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.Kegiatan;


public class CutiAdapter extends  RecyclerView.Adapter<CutiAdapter.ListViewHolder>{
    private OnItemClickCallback onItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallback onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }
    private List<Kegiatan> kegiatans;

    public CutiAdapter(List<Kegiatan> kegiatans) {
        this.kegiatans = kegiatans;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_kegiatan, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        Kegiatan kegiatan =kegiatans.get(position);
        holder.tvKegiatan.setText(kegiatan.getKegiatan());
        holder.ivChecked.setVisibility(kegiatan.isChecked()? View.VISIBLE : View.GONE);

        holder.rlKegiatanCuti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kegiatan.setChecked(!kegiatan.isChecked());
                holder.ivChecked.setVisibility(kegiatan.isChecked() ? View.VISIBLE : View.GONE);
                onItemClickCallback.onItemClicked(kegiatans.get(holder.getAdapterPosition()));
            }
        });

    }

    @Override
    public int getItemCount() {
        return kegiatans.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder{


        TextView tvKegiatan;
        LinearLayout ivChecked;
        LinearLayout rlKegiatanCuti;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);

            rlKegiatanCuti = itemView.findViewById(R.id.rlKegiatanPd);
            tvKegiatan = itemView.findViewById(R.id.tvListKegiatan);
            ivChecked = itemView.findViewById(R.id.imgChecked);
        }
    }

    public interface OnItemClickCallback {
        void onItemClicked(Kegiatan data);
    }
}
