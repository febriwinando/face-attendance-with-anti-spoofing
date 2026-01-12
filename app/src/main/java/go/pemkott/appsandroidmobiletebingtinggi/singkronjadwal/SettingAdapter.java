package go.pemkott.appsandroidmobiletebingtinggi.singkronjadwal;

import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.texthari;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;


public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.GridViewHolder>{

    List<TimeTebleSetting> timeTables = new ArrayList<>();

    public SettingAdapter(List<TimeTebleSetting> timeTables) {
        this.timeTables.clear();
        this.timeTables = timeTables;
    }


    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_jadwal, parent, false);
        return new GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {
        TimeTebleSetting timeTable = timeTables.get(position);


        holder.txtHariJadwal.setText(texthari(Integer.parseInt(timeTable.getHari())));
        holder.txtMasukJadwal.setText(timeTable.getMasuk());
        holder.txtPulangJadwal.setText(timeTable.getPulang());

    }

    @Override
    public int getItemCount() {
        return timeTables.size();
    }

    public class GridViewHolder extends RecyclerView.ViewHolder {

        TextView txtHariJadwal, txtMasukJadwal, txtPulangJadwal;

        GridViewHolder(View itemView) {
            super(itemView);

            txtHariJadwal = itemView.findViewById(R.id.txtHariJadwal);
            txtMasukJadwal = itemView.findViewById(R.id.txtMasukJadwal);
            txtPulangJadwal = itemView.findViewById(R.id.txtPulangJadwal);


        }
    }
}
