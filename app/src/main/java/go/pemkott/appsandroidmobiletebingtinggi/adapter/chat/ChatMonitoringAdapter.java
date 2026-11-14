package go.pemkott.appsandroidmobiletebingtinggi.adapter.chat;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMonitoringThread;

public class ChatMonitoringAdapter extends RecyclerView.Adapter<ChatMonitoringAdapter.ViewHolder> {

    public interface OnThreadClickListener {
        void onThreadClick(ChatMonitoringThread thread);
        default void onThreadLongClick(ChatMonitoringThread thread) {}
    }

    private final Context context;
    private List<ChatMonitoringThread> list;
    private final OnThreadClickListener listener;

    public ChatMonitoringAdapter(Context context, List<ChatMonitoringThread> list, OnThreadClickListener listener) {
        this.context = context;
        this.list = list != null ? list : new ArrayList<>();
        this.listener = listener;
    }

    public void setList(List<ChatMonitoringThread> list) {
        this.list = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_monitoring_thread, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMonitoringThread thread = list.get(position);
        boolean isOpd = thread.isOpdGroup();

        if (isOpd) {
            holder.tvPairName.setText(thread.getSenderName() != null ? thread.getSenderName() : "Grup OPD");
            holder.tvTypeBadge.setText("GRUP OPD");
            holder.tvTypeBadge.setBackgroundColor(Color.parseColor("#EDE9FE"));
            holder.tvTypeBadge.setTextColor(Color.parseColor("#6D28D9"));
            holder.tvOpdInfo.setText("OPD: " + (!TextUtils.isEmpty(thread.getOpdName()) ? thread.getOpdName() : "-"));
        } else {
            String sName = !TextUtils.isEmpty(thread.getSenderName()) ? thread.getSenderName() : "-";
            String rName = !TextUtils.isEmpty(thread.getReceiverName()) ? thread.getReceiverName() : "-";
            holder.tvPairName.setText(sName + " ↔ " + rName);
            holder.tvTypeBadge.setText("PERSONAL");
            holder.tvTypeBadge.setBackgroundColor(Color.parseColor("#E0F2FE"));
            holder.tvTypeBadge.setTextColor(Color.parseColor("#0369A1"));

            String nips = (!TextUtils.isEmpty(thread.getSenderNip()) ? thread.getSenderNip() : "-")
                    + " / "
                    + (!TextUtils.isEmpty(thread.getReceiverNip()) ? thread.getReceiverNip() : "-");
            String opd = !TextUtils.isEmpty(thread.getOpdName()) ? thread.getOpdName()
                    : (!TextUtils.isEmpty(thread.getSenderOpd()) ? thread.getSenderOpd() : "-");
            holder.tvOpdInfo.setText("NIP: " + nips + " • " + opd);
        }

        holder.tvTime.setText(!TextUtils.isEmpty(thread.getLastTime()) ? thread.getLastTime() : "");
        holder.tvLastMessage.setText(!TextUtils.isEmpty(thread.getLastMessage()) ? thread.getLastMessage() : "-");

        String stats = thread.getTotalMessages() + " pesan • "
                + thread.getDeletedCount() + " dihapus • "
                + thread.getUnreadCount() + " belum dibaca";
        holder.tvStats.setText(stats);

        if (thread.isLastMessageDeleted()) {
            String st = thread.getLastMessageStatus();
            if ("deleted_for_everyone".equalsIgnoreCase(st)) {
                holder.tvLastStatusBadge.setText("Terakhir: Hapus Semua");
                holder.tvLastStatusBadge.setBackgroundColor(Color.parseColor("#FEE2E2"));
                holder.tvLastStatusBadge.setTextColor(Color.parseColor("#B91C1C"));
            } else {
                holder.tvLastStatusBadge.setText("Terakhir: Dihapus Logis");
                holder.tvLastStatusBadge.setBackgroundColor(Color.parseColor("#FEF3C7"));
                holder.tvLastStatusBadge.setTextColor(Color.parseColor("#B45309"));
            }
        } else {
            holder.tvLastStatusBadge.setText("Aktif");
            holder.tvLastStatusBadge.setBackgroundColor(Color.parseColor("#DCFCE7"));
            holder.tvLastStatusBadge.setTextColor(Color.parseColor("#166534"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onThreadClick(thread);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onThreadLongClick(thread);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPairName, tvTypeBadge, tvTime, tvOpdInfo, tvLastMessage, tvStats, tvLastStatusBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPairName = itemView.findViewById(R.id.tvThreadPairName);
            tvTypeBadge = itemView.findViewById(R.id.tvThreadTypeBadge);
            tvTime = itemView.findViewById(R.id.tvThreadTime);
            tvOpdInfo = itemView.findViewById(R.id.tvThreadOpdInfo);
            tvLastMessage = itemView.findViewById(R.id.tvThreadLastMessage);
            tvStats = itemView.findViewById(R.id.tvThreadStats);
            tvLastStatusBadge = itemView.findViewById(R.id.tvThreadLastStatusBadge);
        }
    }
}
