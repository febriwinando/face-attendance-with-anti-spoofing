package go.pemkott.appsandroidmobiletebingtinggi.kehadiransift;

import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.BULAN;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.TAHUN;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.TANGGALSIFT;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.JadwalSift;
import go.pemkott.appsandroidmobiletebingtinggi.model.WaktuSift;

public class GridJadwalSiftAdapter extends RecyclerView.Adapter<GridJadwalSiftAdapter.GridViewHolder> {
    private OnItemClickCallback onItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallback onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    private ArrayList<JadwalSift> listJadwal;
    private ArrayList<WaktuSift> waktuSifts;
    Context context;
    public GridJadwalSiftAdapter(Context context, ArrayList<JadwalSift> listJadwal, ArrayList<WaktuSift> waktuSifts) {
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
        holder.tanggal.setTextSize(12);
        String today = TANGGALSIFT.format(new Date());
        holder.tanggal.setText(tanggalCalendar.get(position));

        for (int i = 0; i<listJadwal.size(); i++){
            JadwalSift jadwalSift = listJadwal.get(i);

            if (tanggalJadwal.get(position).equals(jadwalSift.getTanggal())){

                for (int j = 0 ; j<waktuSifts.size(); j++){
                    WaktuSift waktuSift = waktuSifts.get(j);
                    if (jadwalSift.getShift_id().equals(waktuSift.getId())){
                        holder.tanggal.setTextColor(Color.parseColor("#87888c"));

                        holder.txtTanggalJadwalSift.setVisibility(View.VISIBLE);
                        if (waktuSift.getTipe().equals("pagi")){
                            holder.txttanggalJadwal.setText("P");
                            holder.rlTanggalSift.setBackgroundResource(R.drawable.pagi100);

                        }else if (waktuSift.getTipe().equals("siang")){
                            holder.rlTanggalSift.setBackgroundResource(R.drawable.siang100);
                            holder.txttanggalJadwal.setText("S");

                        }else if (waktuSift.getTipe().equals("malam")){
                            holder.rlTanggalSift.setBackgroundResource(R.drawable.malam100);
                            holder.txttanggalJadwal.setText("M");
                        }

                        holder.tanggal.setTextColor(Color.parseColor("#f4f4f4"));
                        holder.tanggal.setTypeface(null, Typeface.BOLD);

                        holder.rlTanggalSift.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onItemClickCallback.onItemClicked(tanggalJadwal.get(holder.getAdapterPosition()));
                            }
                        });

                    }


                }
            }
        }

        if (position == 0){
            holder.tanggal.setTextSize(14);
//            holder.tanggal.setTextColor(Color.parseColor("#87888c"));
//            holder.tanggal.setTypeface(null, Typeface.BOLD_ITALIC);
//            for (int i = 0; i<listJadwal.size(); i++){
//                JadwalSift jadwalSift = listJadwal.get(i);
//
//                if (tanggalJadwal.get(position).equals(jadwalSift.getTanggal())){
//
//                    for (int j = 0 ; j<waktuSifts.size(); j++){
//                        WaktuSift waktuSift = waktuSifts.get(j);
//                        if (jadwalSift.getShift_id().equals(waktuSift.getId())){
//                            holder.tanggal.setVisibility(View.GONE);
//
//                            holder.txtTanggalJadwalSift.setVisibility(View.VISIBLE);
////                            if (waktuSift.getTipe().equals("pagi")){
////                                holder.txttanggalJadwal.setText("P"+listJadwal.size());
////                                holder.rlTanggalSift.setBackgroundResource(R.drawable.pagi100);
////
////                            }else if (waktuSift.getTipe().equals("siang")){
////                                holder.rlTanggalSift.setBackgroundResource(R.drawable.siang100);
////                                holder.txttanggalJadwal.setText("S");
////
////                            }else if (waktuSift.getTipe().equals("malam")){
////                                holder.rlTanggalSift.setBackgroundResource(R.drawable.malam100);
////                                holder.txttanggalJadwal.setText("M");
////                            }
//
//                            holder.tanggal.setTextColor(Color.parseColor("#f4f4f4"));
//                            holder.tanggal.setTypeface(null, Typeface.BOLD_ITALIC);
//
//                            holder.rlTanggalSift.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    onItemClickCallback.onItemClicked(tanggalJadwal.get(holder.getAdapterPosition()));
//                                }
//                            });
//
//                        }
//
//
//                    }
//                }
//            }
        }

        if (tanggalCalendar.get(position).equals(today)){
            holder.tanggal.setTextSize(16);
            holder.tanggal.setTypeface(null, Typeface.BOLD_ITALIC);
            holder.llTodayJadwal.setVisibility(View.VISIBLE);

        }

        holder.rlTanggalSift.setEnabled(true);


    }


    @Override
    public int getItemCount() {
        return tanggalJadwal.size();
    }

    public class GridViewHolder extends RecyclerView.ViewHolder {
        TextView tanggal, txttanggalJadwal;
        RelativeLayout rlTanggalSift;
        View llTodayJadwal;
        LinearLayout txtTanggalJadwalSift, llTanggalGridJadwal;
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
