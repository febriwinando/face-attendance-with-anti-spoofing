package go.pemkott.appsandroidmobiletebingtinggi.kehadiransift;

import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.BULAN;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.TAHUN;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.TANGGALSIFT;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.JadwalSift;
import go.pemkott.appsandroidmobiletebingtinggi.model.WaktuSift;

public class GridJadwalShiftAdapter extends RecyclerView.Adapter<GridJadwalShiftAdapter.GridViewHolder> {
    private OnItemClickCallback onItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallback onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    private ArrayList<JadwalSift> listJadwal;
    private ArrayList<WaktuSift> waktuSifts;
    Context context;
    public GridJadwalShiftAdapter(Context context, ArrayList<JadwalSift> listJadwal, ArrayList<WaktuSift> waktuSifts) {
        this.listJadwal = listJadwal;
        this.waktuSifts = waktuSifts;
        this.context = context;

        int bulan = Integer.parseInt(BULAN.format(new Date()));
        int tahun = Integer.parseInt(TAHUN.format(new Date()));

        printDatesInMonth(tahun, bulan);

    }


    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_grid_jadwal, viewGroup, false);
        return new GridViewHolder(view);

    }
    @Override
    public void onBindViewHolder(@NonNull final GridViewHolder holder, int position) {
        holder.rlTanggalSift.setEnabled(false);
        String today = TANGGALSIFT.format(new Date());
        holder.tanggal.setText(tanggalCalendar.get(position));
        holder.txtTanggalJadwalSift.setVisibility(View.INVISIBLE);
        holder.llTodayJadwal.setVisibility(View.GONE);

        // Reset state
        holder.tanggal.setTextColor(ContextCompat.getColor(context, R.color.text_hint));
        holder.tanggal.setTypeface(null, Typeface.NORMAL);
        holder.rlTanggalSift.setOnClickListener(null);

        for (int i = 0; i<listJadwal.size(); i++){
            JadwalSift jadwalSift = listJadwal.get(i);

            if (tanggalJadwal.get(position).equals(jadwalSift.getTanggal())){

                for (int j = 0 ; j<waktuSifts.size(); j++){
                    WaktuSift waktuSift = waktuSifts.get(j);
                    if (jadwalSift.getShift_id().equals(waktuSift.getId())){
                        
                        holder.txtTanggalJadwalSift.setVisibility(View.VISIBLE);
                        int accentColor;
                        String label;

                        if (waktuSift.getTipe().equalsIgnoreCase("pagi")){
                            accentColor = ContextCompat.getColor(context, R.color.biru);
                            label = "P";
                        } else if (waktuSift.getTipe().equalsIgnoreCase("siang") || waktuSift.getTipe().equalsIgnoreCase("sore")){
                            accentColor = ContextCompat.getColor(context, R.color.kuning);
                            label = "S";
                        } else if (waktuSift.getTipe().equalsIgnoreCase("malam")){
                            accentColor = ContextCompat.getColor(context, R.color.brand_secondary);
                            label = "M";
                        } else {
                            accentColor = ContextCompat.getColor(context, R.color.primary_brand);
                            label = "J";
                        }

                        holder.txtTanggalJadwalSift.setBackgroundTintList(ColorStateList.valueOf(accentColor));
                        holder.txttanggalJadwal.setText(label);
                        holder.txttanggalJadwal.setTextColor(Color.WHITE);

                        holder.tanggal.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
                        holder.tanggal.setTypeface(null, Typeface.BOLD);

                        holder.rlTanggalSift.setOnClickListener(v -> {
                            if (onItemClickCallback != null) {
                                onItemClickCallback.onItemClicked(tanggalJadwal.get(holder.getAdapterPosition()));
                            }
                        });

                    }
                }
            }
        }

        if (tanggalCalendar.get(position).equals(today)){
            holder.llTodayJadwal.setVisibility(View.VISIBLE);
        }

        holder.rlTanggalSift.setEnabled(true);
    }


    @Override
    public int getItemCount() {
        return tanggalJadwal.size();
    }

    public static class GridViewHolder extends RecyclerView.ViewHolder {
        TextView tanggal, txttanggalJadwal;
        RelativeLayout rlTanggalSift;
        View llTodayJadwal;
        FrameLayout txtTanggalJadwalSift;
        GridViewHolder(View itemView) {
            super(itemView);

            tanggal = itemView.findViewById(R.id.txtTanggal);
            txttanggalJadwal = itemView.findViewById(R.id.tanggalJadwal);
            llTodayJadwal = itemView.findViewById(R.id.llTodayJadwal);
            rlTanggalSift = itemView.findViewById(R.id.rlTanggalSift);
            txtTanggalJadwalSift = itemView.findViewById(R.id.txtTanggalJadwalSift);
        }
    }

    public interface OnItemClickCallback {

        void onItemClicked(String s);
    }


    static ArrayList<String> tanggalCalendar = new ArrayList<>();
    static ArrayList<String> tanggalJadwal = new ArrayList<>();

    public void printDatesInMonth(int year, int month) {
        tanggalCalendar.clear();
        tanggalJadwal.clear();

        SimpleDateFormat fmt = new SimpleDateFormat("d");
        SimpleDateFormat fmtJadwalSift = new SimpleDateFormat("yyyy-MM-dd");

        SimpleDateFormat fmtBefore = new SimpleDateFormat("d/M");
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month - 1, 1);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(cal.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, -1);

        String newDate = fmtBefore.format(calendar.getTime());
        String dateLasmonth = fmtJadwalSift.format(calendar.getTime());

        tanggalCalendar.add(newDate);
        tanggalJadwal.add(dateLasmonth);

        for (int i = 0; i < daysInMonth; i++) {
            tanggalCalendar.add(fmt.format(cal.getTime()));
            tanggalJadwal.add(fmtJadwalSift.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

    }

}
