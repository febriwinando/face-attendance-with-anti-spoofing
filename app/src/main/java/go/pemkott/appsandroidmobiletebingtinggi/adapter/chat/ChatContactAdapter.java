package go.pemkott.appsandroidmobiletebingtinggi.adapter.chat;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatContact;

public class ChatContactAdapter extends RecyclerView.Adapter<ChatContactAdapter.ViewHolder> {

    public interface OnContactClickListener {
        void onContactClick(ChatContact contact);
    }

    private final Context context;
    private List<ChatContact> list;
    private final OnContactClickListener listener;

    public ChatContactAdapter(Context context, List<ChatContact> list, OnContactClickListener listener) {
        this.context = context;
        this.list = list != null ? list : new ArrayList<>();
        this.listener = listener;
    }

    public void setList(List<ChatContact> list) {
        this.list = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatContact item = list.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvSubtitle.setText(item.getSubtitle());

        if (!TextUtils.isEmpty(item.getLastMessage())) {
            holder.tvLastMsg.setText(item.getLastMessage());
            holder.tvLastMsg.setVisibility(View.VISIBLE);
        } else {
            holder.tvLastMsg.setText("Belum ada pesan");
            holder.tvLastMsg.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(item.getLastTime())) {
            holder.tvTime.setText(item.getLastTime());
            holder.tvTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvTime.setVisibility(View.GONE);
        }

        if (item.getUnreadCount() > 0) {
            holder.tvUnreadBadge.setText(String.valueOf(item.getUnreadCount()));
            holder.tvUnreadBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvUnreadBadge.setVisibility(View.GONE);
        }

        if ("opd".equals(item.getChatType())) {
            holder.civAvatar.setVisibility(View.GONE);
            holder.ivOpdIcon.setVisibility(View.VISIBLE);
        } else {
            holder.civAvatar.setVisibility(View.VISIBLE);
            holder.ivOpdIcon.setVisibility(View.GONE);

            if (!TextUtils.isEmpty(item.getAvatarUrl())) {
                Glide.with(context)
                        .load(item.getAvatarUrl())
                        .placeholder(R.drawable.ic_baseline_person_24)
                        .error(R.drawable.ic_baseline_person_24)
                        .into(holder.civAvatar);
            } else {
                holder.civAvatar.setImageResource(R.drawable.ic_baseline_person_24);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView civAvatar;
        ImageView ivOpdIcon;
        TextView tvTitle, tvSubtitle, tvLastMsg, tvTime, tvUnreadBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            civAvatar = itemView.findViewById(R.id.civChatAvatar);
            ivOpdIcon = itemView.findViewById(R.id.ivOpdIcon);
            tvTitle = itemView.findViewById(R.id.tvChatTitle);
            tvSubtitle = itemView.findViewById(R.id.tvChatSubtitle);
            tvLastMsg = itemView.findViewById(R.id.tvChatLastMsg);
            tvTime = itemView.findViewById(R.id.tvChatTime);
            tvUnreadBadge = itemView.findViewById(R.id.tvUnreadBadge);
        }
    }
}
