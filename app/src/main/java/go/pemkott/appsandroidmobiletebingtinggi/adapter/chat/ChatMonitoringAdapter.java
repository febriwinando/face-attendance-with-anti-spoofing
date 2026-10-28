package go.pemkott.appsandroidmobiletebingtinggi.adapter.chat;

import android.content.Context;
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

        holder.tvSenderName.setText(thread.getSenderName() + " (" + thread.getSenderNip() + ")");
        holder.tvReceiverName.setText(thread.getReceiverName() + " (" + thread.getReceiverNip() + ")");
        holder.tvSenderOpd.setText(thread.getSenderOpd());
        holder.tvReceiverOpd.setText(thread.getReceiverOpd());
        holder.tvLastMsg.setText(thread.getLastMessage());
        holder.tvTime.setText(thread.getLastTime());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onThreadClick(thread);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSenderName, tvReceiverName, tvSenderOpd, tvReceiverOpd, tvLastMsg, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tvMonitoringSenderName);
            tvReceiverName = itemView.findViewById(R.id.tvMonitoringReceiverName);
            tvSenderOpd = itemView.findViewById(R.id.tvMonitoringSenderOpd);
            tvReceiverOpd = itemView.findViewById(R.id.tvMonitoringReceiverOpd);
            tvLastMsg = itemView.findViewById(R.id.tvMonitoringLastMsg);
            tvTime = itemView.findViewById(R.id.tvMonitoringTime);
        }
    }
}
