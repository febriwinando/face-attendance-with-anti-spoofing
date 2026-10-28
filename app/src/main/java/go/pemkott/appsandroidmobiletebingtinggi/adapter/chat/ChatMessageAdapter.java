package go.pemkott.appsandroidmobiletebingtinggi.adapter.chat;

import android.content.Context;
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
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMessageItem;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_LEFT = 1;
    private static final int TYPE_RIGHT = 2;

    private final Context context;
    private List<ChatMessageItem> list;

    public ChatMessageAdapter(Context context, List<ChatMessageItem> list) {
        this.context = context;
        this.list = list != null ? list : new ArrayList<>();
    }

    public void setList(List<ChatMessageItem> list) {
        this.list = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addMessage(ChatMessageItem item) {
        if (item != null) {
            this.list.add(item);
            notifyItemInserted(this.list.size() - 1);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessageItem item = list.get(position);
        if (item.isMe()) {
            return TYPE_RIGHT;
        } else {
            return TYPE_LEFT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_bubble_right, parent, false);
            return new RightViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_bubble_left, parent, false);
            return new LeftViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessageItem item = list.get(position);

        if (holder instanceof RightViewHolder) {
            RightViewHolder rHolder = (RightViewHolder) holder;
            rHolder.tvMessage.setText(item.getMessage());
            rHolder.tvTime.setText(item.getCreatedAtFormatted());
        } else if (holder instanceof LeftViewHolder) {
            LeftViewHolder lHolder = (LeftViewHolder) holder;
            lHolder.tvMessage.setText(item.getMessage());
            lHolder.tvTime.setText(item.getCreatedAtFormatted());

            if ("opd".equals(item.getChatType()) && !TextUtils.isEmpty(item.getSenderName())) {
                lHolder.tvSenderName.setText(item.getSenderName());
                lHolder.tvSenderName.setVisibility(View.VISIBLE);
            } else {
                lHolder.tvSenderName.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class LeftViewHolder extends RecyclerView.ViewHolder {
        TextView tvSenderName, tvMessage, tvTime;

        public LeftViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    public static class RightViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        public RightViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
