package go.pemkott.appsandroidmobiletebingtinggi.admin;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.PreviewItemModel;

public class PreviewListAdapter extends RecyclerView.Adapter<PreviewListAdapter.ViewHolder> {

    private final Context context;
    private final List<PreviewItemModel> itemList;

    public PreviewListAdapter(Context context, List<PreviewItemModel> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_preview_massal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PreviewItemModel item = itemList.get(position);

        holder.tvNama.setText(item.getNama());
        holder.tvNip.setText(item.getNip());

        String opdTipe = item.getOpd();
        if (item.getTipePegawai() != null && !item.getTipePegawai().isEmpty()) {
            opdTipe += " [" + item.getTipePegawai() + "]";
        }
        holder.tvOpd.setText(opdTipe);

        holder.tvStatusBadge.setText(item.getStatus());
        applyBadgeColor(holder.tvStatusBadge, item.getBadge());

        if (item.getJamMasuk().contains("<") || item.getJamMasuk().contains("&")) {
            holder.tvJamMasuk.setText(Html.fromHtml(item.getJamMasuk()));
        } else {
            holder.tvJamMasuk.setText(item.getJamMasuk());
        }

        if (item.getJamPulang().contains("<") || item.getJamPulang().contains("&")) {
            holder.tvJamPulang.setText(Html.fromHtml(item.getJamPulang()));
        } else {
            holder.tvJamPulang.setText(item.getJamPulang());
        }

        if (item.getKeterangan().contains("<") || item.getKeterangan().contains("&")) {
            holder.tvKeterangan.setText(Html.fromHtml(item.getKeterangan()));
        } else {
            holder.tvKeterangan.setText(item.getKeterangan());
        }

        holder.itemView.setOnClickListener(v -> showDetailDialog(item));
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    private void applyBadgeColor(TextView tvBadge, String badgeType) {
        String colorHex;
        if ("success".equalsIgnoreCase(badgeType)) {
            colorHex = "#22C55E";
        } else if ("primary".equalsIgnoreCase(badgeType) || "info".equalsIgnoreCase(badgeType)) {
            colorHex = "#2563EB";
        } else if ("warning".equalsIgnoreCase(badgeType)) {
            colorHex = "#EAB308";
        } else if ("danger".equalsIgnoreCase(badgeType)) {
            colorHex = "#EF4444";
        } else {
            colorHex = "#64748B";
        }

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(16f);
        shape.setColor(Color.parseColor(colorHex));
        tvBadge.setBackground(shape);
        tvBadge.setTextColor(Color.WHITE);
    }

    private void showDetailDialog(PreviewItemModel item) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_search_selection);

        TextView tvTitle = dialog.findViewById(R.id.tvSearchDialogTitle);
        View etSearch = dialog.findViewById(R.id.etSearchDialogInput);
        if (etSearch != null && etSearch.getParent() instanceof View) {
            ((View) etSearch.getParent()).setVisibility(View.GONE);
        }

        if (tvTitle != null) {
            tvTitle.setText("Detail Presensi - " + item.getNama());
        }

        android.widget.ListView lvItems = dialog.findViewById(R.id.lvSearchDialogItems);
        if (lvItems != null) {
            java.util.List<String> details = new java.util.ArrayList<>();
            details.add("NIP Pegawai: " + item.getNip());
            details.add("Nama Lengkap: " + item.getNama());
            details.add("OPD / Unit Kerja: " + item.getOpd());
            details.add("Status Shift: " + item.getTipePegawai());
            details.add("Status Aksi: " + item.getStatus());
            details.add("Aksi Database: " + (item.isCanExecute() ? item.getActionType().toUpperCase() : "DILEWATI / CUKUP"));
            if (!"-".equals(item.getPresensiId())) {
                details.add("ID Record DB: #" + item.getPresensiId());
            }
            details.add("Jam Masuk: " + stripHtml(item.getJamMasuk()));
            details.add("Jam Pulang: " + stripHtml(item.getJamPulang()));
            details.add("Catatan / Ket: " + stripHtml(item.getKeterangan()));

            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                    context, android.R.layout.simple_list_item_1, details);
            lvItems.setAdapter(adapter);
        }

        View ivClose = dialog.findViewById(R.id.ivCloseSearchDialog);
        if (ivClose != null) ivClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private String stripHtml(String html) {
        if (html == null) return "-";
        return html.replaceAll("<[^>]*>", "").trim();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvStatusBadge, tvNip, tvOpd, tvJamMasuk, tvJamPulang, tvKeterangan;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvPreviewNama);
            tvStatusBadge = itemView.findViewById(R.id.tvPreviewStatusBadge);
            tvNip = itemView.findViewById(R.id.tvPreviewNip);
            tvOpd = itemView.findViewById(R.id.tvPreviewOpd);
            tvJamMasuk = itemView.findViewById(R.id.tvPreviewJamMasuk);
            tvJamPulang = itemView.findViewById(R.id.tvPreviewJamPulang);
            tvKeterangan = itemView.findViewById(R.id.tvPreviewKeterangan);
        }
    }
}
