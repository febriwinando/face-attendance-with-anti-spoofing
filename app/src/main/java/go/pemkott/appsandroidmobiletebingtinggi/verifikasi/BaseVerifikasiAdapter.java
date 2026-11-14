package go.pemkott.appsandroidmobiletebingtinggi.verifikasi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import go.pemkott.appsandroidmobiletebingtinggi.R;

public abstract class BaseVerifikasiAdapter<T> extends RecyclerView.Adapter<BaseVerifikasiAdapter.ListViewHolder> {

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int selectedCount, int totalCount);
    }

    private final List<T> items;
    private final Set<String> selectedIds = new LinkedHashSet<>();
    private OnSelectionChangeListener selectionChangeListener;

    protected BaseVerifikasiAdapter(List<T> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
        this.selectionChangeListener = listener;
        notifySelectionChanged();
    }

    @LayoutRes
    protected abstract int getItemLayoutRes();

    protected abstract String extractId(T item);

    protected abstract String extractNama(T item);

    protected abstract String extractTanggal(T item);

    protected abstract String extractJam(T item);

    protected abstract String extractPosisi(T item);

    protected abstract String extractKeterangan(T item);

    protected abstract int extractValidasi(T item);

    protected abstract void dispatchItemClick(T item);

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getItemLayoutRes(), parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        T item = items.get(position);
        String itemId = extractId(item);

        holder.tvNamaEmployee.setText(extractNama(item) != null ? extractNama(item) : "-");

        String tgl = extractTanggal(item) != null ? extractTanggal(item) : "";
        String jam = extractJam(item) != null ? extractJam(item) : "";
        holder.tvTanggalJam.setText("📅 " + tgl + " • " + jam + " WIB");

        String katName = mapKategoriName(extractPosisi(item));
        if (holder.tvStatus != null) {
            holder.tvStatus.setText(katName);
        }

        String rawKet = extractKeterangan(item);
        String ket = rawKet != null && !rawKet.trim().isEmpty() ? rawKet.trim() : katName;
        holder.tvAktivitas.setText("Keterangan: " + ket);

        int validasi = extractValidasi(item);
        if (validasi == 3 || validasi == 4) {
            holder.civPresensiReject.setVisibility(View.VISIBLE);
            holder.civPresensi.setVisibility(View.GONE);
        } else {
            holder.civPresensi.setVisibility(View.VISIBLE);
            holder.civPresensiReject.setVisibility(View.GONE);
        }

        if (holder.cbPilihValidasi != null) {
            holder.cbPilihValidasi.setOnCheckedChangeListener(null);
            boolean isValidId = itemId != null && !itemId.trim().isEmpty();
            holder.cbPilihValidasi.setEnabled(isValidId);
            holder.cbPilihValidasi.setChecked(isValidId && selectedIds.contains(itemId));
            holder.cbPilihValidasi.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isValidId) {
                    return;
                }
                if (isChecked) {
                    selectedIds.add(itemId);
                } else {
                    selectedIds.remove(itemId);
                }
                notifySelectionChanged();
            });
        }

        holder.cvValidasi.setOnClickListener(v -> dispatchItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<String> getSelectedIds() {
        return new ArrayList<>(selectedIds);
    }

    public int getSelectedCount() {
        return selectedIds.size();
    }

    public boolean isAllSelected() {
        if (items.isEmpty()) {
            return false;
        }
        int selectable = 0;
        for (T item : items) {
            String id = extractId(item);
            if (id != null && !id.trim().isEmpty()) {
                selectable++;
                if (!selectedIds.contains(id)) {
                    return false;
                }
            }
        }
        return selectable > 0;
    }

    public void setAllSelected(boolean selectAll) {
        selectedIds.clear();
        if (selectAll) {
            for (T item : items) {
                String id = extractId(item);
                if (id != null && !id.trim().isEmpty()) {
                    selectedIds.add(id);
                }
            }
        }
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    private void notifySelectionChanged() {
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(selectedIds.size(), items.size());
        }
    }

    private static String mapKategoriName(String posisi) {
        if ("tl-masuk".equals(posisi) || "tl-pulang".equals(posisi) || "tl".equals(posisi) || "luar".equals(posisi)) {
            return "Tugas Lapangan";
        } else if ("sk".equals(posisi)) {
            return "Sakit";
        } else if ("pd".equals(posisi)) {
            return "Perjalanan Dinas";
        } else if ("kp".equals(posisi)) {
            return "Keperluan Pribadi";
        } else if ("cuti".equals(posisi) || "ct".equals(posisi)) {
            return "Cuti";
        }
        return "Presensi";
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNamaEmployee, tvStatus, tvTanggalJam, tvAktivitas;
        final CardView cvValidasi;
        final CircleImageView civPresensiReject, civPresensi;
        final CheckBox cbPilihValidasi;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamaEmployee = itemView.findViewById(R.id.tvNamaValidasi);
            tvStatus = itemView.findViewById(R.id.txtSatusVerif);
            tvTanggalJam = itemView.findViewById(R.id.tvTanggalJamValidasi);
            tvAktivitas = itemView.findViewById(R.id.tvAktivitasValidasi);
            cvValidasi = itemView.findViewById(R.id.cvValidasi);
            civPresensiReject = itemView.findViewById(R.id.civPresensiReject);
            civPresensi = itemView.findViewById(R.id.civPresensi);
            cbPilihValidasi = itemView.findViewById(R.id.cbPilihValidasi);
        }
    }
}

