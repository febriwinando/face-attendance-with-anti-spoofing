package go.pemkott.appsandroidmobiletebingtinggi.adapter.chat;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMonitoringMessage;

/**
 * Adapter pesan monitoring khusus Administrator (NIP 199402092020121005).
 * Menampilkan konten asli pesan aktif & terhapus secara utuh, status baca & hapus,
 * serta checkbox seleksi untuk penghapusan permanen satu atau banyak pesan sekaligus.
 */
public class ChatMonitoringMessageAdapter extends RecyclerView.Adapter<ChatMonitoringMessageAdapter.ViewHolder> {

    public interface Listener {
        void onInspectMessage(ChatMonitoringMessage message);
        void onPermanentDeleteSingle(ChatMonitoringMessage message);
        void onSelectionCountChanged(int selectedCount);
    }

    private final Context context;
    private List<ChatMonitoringMessage> list;
    private final Listener listener;

    public ChatMonitoringMessageAdapter(Context context, List<ChatMonitoringMessage> list, Listener listener) {
        this.context = context;
        this.list = list != null ? list : new ArrayList<>();
        this.listener = listener;
    }

    public void setList(List<ChatMonitoringMessage> list) {
        this.list = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public List<ChatMonitoringMessage> getSelectedMessages() {
        List<ChatMonitoringMessage> selected = new ArrayList<>();
        for (ChatMonitoringMessage m : list) {
            if (m.isSelected()) selected.add(m);
        }
        return selected;
    }

    public void clearSelections() {
        for (ChatMonitoringMessage m : list) {
            m.setSelected(false);
        }
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    private void notifySelectionChanged() {
        if (listener != null) {
            listener.onSelectionCountChanged(getSelectedMessages().size());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_monitoring_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMonitoringMessage msg = list.get(position);
        boolean isOpd = "opd".equalsIgnoreCase(msg.getChatType());

        String sName = !TextUtils.isEmpty(msg.getSenderName()) ? msg.getSenderName() : "-";
        String rName = !TextUtils.isEmpty(msg.getReceiverName()) ? msg.getReceiverName() : "-";
        holder.tvParticipants.setText(sName + " -> " + rName);

        String nipInfo = !TextUtils.isEmpty(msg.getSenderNip()) ? msg.getSenderNip() : "-";
        if (!isOpd && !TextUtils.isEmpty(msg.getReceiverNip())) {
            nipInfo += " -> " + msg.getReceiverNip();
        }
        String opdInfo = !TextUtils.isEmpty(msg.getOpdName()) ? msg.getOpdName() : "-";
        holder.tvNipOpd.setText("NIP: " + nipInfo + " • " + opdInfo);

        holder.tvTypeBadge.setText(isOpd ? "GRUP OPD" : "PERSONAL");
        holder.tvTypeBadge.setBackgroundColor(Color.parseColor(isOpd ? "#EDE9FE" : "#DBEAFE"));
        holder.tvTypeBadge.setTextColor(Color.parseColor(isOpd ? "#6D28D9" : "#1D4ED8"));

        String body = !TextUtils.isEmpty(msg.getMessage()) ? msg.getMessage() : "";
        if (body.isEmpty() && !TextUtils.isEmpty(msg.getFileName())) {
            body = "[Lampiran: " + msg.getFileName() + "]";
        } else if (body.isEmpty() && !TextUtils.isEmpty(msg.getFileUrl())) {
            body = "[Media: " + msg.getMsgType() + "]";
        } else if (!TextUtils.isEmpty(msg.getFileName())) {
            body = body + "\n[Lampiran: " + msg.getFileName() + "]";
        }
        holder.tvBody.setText(body);

        // Status Hapus badge
        holder.tvDeleteStatus.setText(msg.getReadableDeleteStatusLabel());
        if (msg.isDeletedForEveryone()) {
            holder.tvDeleteStatus.setBackgroundColor(Color.parseColor("#FEE2E2"));
            holder.tvDeleteStatus.setTextColor(Color.parseColor("#B91C1C"));
        } else if (msg.isDeletedAny()) {
            holder.tvDeleteStatus.setBackgroundColor(Color.parseColor("#FEF3C7"));
            holder.tvDeleteStatus.setTextColor(Color.parseColor("#B45309"));
        } else {
            holder.tvDeleteStatus.setBackgroundColor(Color.parseColor("#DCFCE7"));
            holder.tvDeleteStatus.setTextColor(Color.parseColor("#166534"));
        }

        // Status Baca badge
        if (msg.isRead()) {
            String readLabel = "Dibaca" + (!TextUtils.isEmpty(msg.getReadAt()) ? " (" + msg.getReadAt() + ")" : "");
            holder.tvReadStatus.setText(readLabel);
            holder.tvReadStatus.setBackgroundColor(Color.parseColor("#E0F2FE"));
            holder.tvReadStatus.setTextColor(Color.parseColor("#0369A1"));
        } else {
            holder.tvReadStatus.setText("Belum Dibaca");
            holder.tvReadStatus.setBackgroundColor(Color.parseColor("#F1F5F9"));
            holder.tvReadStatus.setTextColor(Color.parseColor("#475569"));
        }

        holder.tvTimestamp.setText(!TextUtils.isEmpty(msg.getCreatedAtFormatted())
                ? msg.getCreatedAtFormatted()
                : (!TextUtils.isEmpty(msg.getCreatedAt()) ? msg.getCreatedAt() : ""));

        if (!TextUtils.isEmpty(msg.getDeletedBy()) || !TextUtils.isEmpty(msg.getDeletedAt())) {
            holder.tvDeletionMeta.setText("Hapus oleh: "
                    + (!TextUtils.isEmpty(msg.getDeletedBy()) ? msg.getDeletedBy() : "-")
                    + " • "
                    + (!TextUtils.isEmpty(msg.getDeletedAt()) ? msg.getDeletedAt() : ""));
        } else {
            holder.tvDeletionMeta.setText("ID #" + msg.getId() + " • Tap kartu untuk detail & riwayat audit");
        }

        int defaultStroke = ContextCompat.getColor(context, R.color.divider_color);
        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(msg.isSelected());
        holder.cardRoot.setStrokeColor(msg.isSelected() ? Color.parseColor("#DC2626") : defaultStroke);
        holder.cardRoot.setStrokeWidth(msg.isSelected() ? 3 : 2);

        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            msg.setSelected(isChecked);
            holder.cardRoot.setStrokeColor(isChecked ? Color.parseColor("#DC2626") : defaultStroke);
            holder.cardRoot.setStrokeWidth(isChecked ? 3 : 2);
            notifySelectionChanged();
        });

        holder.itemView.setOnClickListener(v -> {
            if (!getSelectedMessages().isEmpty()) {
                holder.cbSelect.setChecked(!holder.cbSelect.isChecked());
            } else if (listener != null) {
                listener.onInspectMessage(msg);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            holder.cbSelect.setChecked(!holder.cbSelect.isChecked());
            return true;
        });

        holder.btnPermanentDelete.setOnClickListener(v -> {
            if (listener != null) listener.onPermanentDeleteSingle(msg);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardRoot;
        CheckBox cbSelect;
        TextView tvParticipants, tvNipOpd, tvTypeBadge, tvBody;
        TextView tvDeleteStatus, tvReadStatus, tvTimestamp, tvDeletionMeta, btnPermanentDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot = itemView.findViewById(R.id.cardMonitoringMsg);
            cbSelect = itemView.findViewById(R.id.cbSelectMonitoringMsg);
            tvParticipants = itemView.findViewById(R.id.tvMonMsgParticipants);
            tvNipOpd = itemView.findViewById(R.id.tvMonMsgNipOpd);
            tvTypeBadge = itemView.findViewById(R.id.tvMonMsgTypeBadge);
            tvBody = itemView.findViewById(R.id.tvMonMsgBody);
            tvDeleteStatus = itemView.findViewById(R.id.tvMonMsgDeleteStatus);
            tvReadStatus = itemView.findViewById(R.id.tvMonMsgReadStatus);
            tvTimestamp = itemView.findViewById(R.id.tvMonMsgTimestamp);
            tvDeletionMeta = itemView.findViewById(R.id.tvMonMsgDeletionMeta);
            btnPermanentDelete = itemView.findViewById(R.id.btnMonMsgPermanentDelete);
        }
    }
}

