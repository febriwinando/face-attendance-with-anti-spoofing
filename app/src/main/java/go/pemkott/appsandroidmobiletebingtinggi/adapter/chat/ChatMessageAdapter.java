package go.pemkott.appsandroidmobiletebingtinggi.adapter.chat;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMessageItem;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnMessageActionListener {
        void onReplyMessage(ChatMessageItem item);
        void onCopyText(String text);
        void onViewMedia(String url, String type);
    }

    private static final int TYPE_LEFT = 1;
    private static final int TYPE_RIGHT = 2;

    private final Context context;
    private List<ChatMessageItem> list;
    private OnMessageActionListener actionListener;

    public ChatMessageAdapter(Context context, List<ChatMessageItem> list, OnMessageActionListener listener) {
        this.context = context;
        this.list = list != null ? list : new ArrayList<>();
        this.actionListener = listener;
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
            RightViewHolder r = (RightViewHolder) holder;

            // Message text
            if (TextUtils.isEmpty(item.getMessage())) {
                r.tvMessage.setVisibility(View.GONE);
            } else {
                r.tvMessage.setText(item.getMessage());
                r.tvMessage.setVisibility(View.VISIBLE);
            }

            // Quoted / Replied message
            if (!TextUtils.isEmpty(item.getReplyToSenderName())) {
                r.cvReplyContainer.setVisibility(View.VISIBLE);
                r.tvReplySender.setText(item.getReplyToSenderName());
                r.tvReplyMessage.setText(item.getReplyToMessage());
            } else {
                r.cvReplyContainer.setVisibility(View.GONE);
            }

            // Image / Video attachment
            if (("image".equals(item.getMsgType()) || "video".equals(item.getMsgType())) && !TextUtils.isEmpty(item.getFileUrl())) {
                r.cvImageContainer.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(item.getFileUrl())
                        .placeholder(R.drawable.ic_image)
                        .error(R.drawable.ic_image)
                        .into(r.ivMessageImage);

                r.cvImageContainer.setOnClickListener(v -> {
                    if (actionListener != null) actionListener.onViewMedia(item.getFileUrl(), item.getMsgType());
                });
            } else {
                r.cvImageContainer.setVisibility(View.GONE);
            }

            // Document attachment
            if ("document".equals(item.getMsgType()) && !TextUtils.isEmpty(item.getFileUrl())) {
                r.cvDocumentContainer.setVisibility(View.VISIBLE);
                r.tvDocumentName.setText(item.getFileName());

                r.cvDocumentContainer.setOnClickListener(v -> {
                    if (actionListener != null) actionListener.onViewMedia(item.getFileUrl(), "document");
                });
            } else {
                r.cvDocumentContainer.setVisibility(View.GONE);
            }

            // Time & Checkmarks
            r.tvTime.setText(item.getCreatedAtFormatted());
            if (item.isRead()) {
                r.tvStatusCheck.setText("✓✓");
                r.tvStatusCheck.setTextColor(Color.parseColor("#60A5FA")); // Blue tick
            } else {
                r.tvStatusCheck.setText("✓");
                r.tvStatusCheck.setTextColor(Color.parseColor("#BFDBFE")); // Light tick
            }

            // Long Press Options
            r.itemView.setOnLongClickListener(v -> {
                showOptionsDialog(item);
                return true;
            });

        } else if (holder instanceof LeftViewHolder) {
            LeftViewHolder l = (LeftViewHolder) holder;

            // Sender name in OPD group chat
            if ("opd".equals(item.getChatType()) && !TextUtils.isEmpty(item.getSenderName())) {
                l.tvSenderName.setText(item.getSenderName());
                l.tvSenderName.setVisibility(View.VISIBLE);
            } else {
                l.tvSenderName.setVisibility(View.GONE);
            }

            // Message text
            if (TextUtils.isEmpty(item.getMessage())) {
                l.tvMessage.setVisibility(View.GONE);
            } else {
                l.tvMessage.setText(item.getMessage());
                l.tvMessage.setVisibility(View.VISIBLE);
            }

            // Quoted / Replied message
            if (!TextUtils.isEmpty(item.getReplyToSenderName())) {
                l.cvReplyContainer.setVisibility(View.VISIBLE);
                l.tvReplySender.setText(item.getReplyToSenderName());
                l.tvReplyMessage.setText(item.getReplyToMessage());
            } else {
                l.cvReplyContainer.setVisibility(View.GONE);
            }

            // Image / Video attachment
            if (("image".equals(item.getMsgType()) || "video".equals(item.getMsgType())) && !TextUtils.isEmpty(item.getFileUrl())) {
                l.cvImageContainer.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(item.getFileUrl())
                        .placeholder(R.drawable.ic_image)
                        .error(R.drawable.ic_image)
                        .into(l.ivMessageImage);

                l.cvImageContainer.setOnClickListener(v -> {
                    if (actionListener != null) actionListener.onViewMedia(item.getFileUrl(), item.getMsgType());
                });
            } else {
                l.cvImageContainer.setVisibility(View.GONE);
            }

            // Document attachment
            if ("document".equals(item.getMsgType()) && !TextUtils.isEmpty(item.getFileUrl())) {
                l.cvDocumentContainer.setVisibility(View.VISIBLE);
                l.tvDocumentName.setText(item.getFileName());

                l.cvDocumentContainer.setOnClickListener(v -> {
                    if (actionListener != null) actionListener.onViewMedia(item.getFileUrl(), "document");
                });
            } else {
                l.cvDocumentContainer.setVisibility(View.GONE);
            }

            l.tvTime.setText(item.getCreatedAtFormatted());

            // Long Press Options
            l.itemView.setOnLongClickListener(v -> {
                showOptionsDialog(item);
                return true;
            });
        }
    }

    private void showOptionsDialog(ChatMessageItem item) {
        String[] options;
        if (!TextUtils.isEmpty(item.getMessage())) {
            options = new String[]{"Balas Pesan", "Salin Teks Pesan"};
        } else {
            options = new String[]{"Balas Pesan"};
        }

        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Pilihan Pesan")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (actionListener != null) actionListener.onReplyMessage(item);
                    } else if (which == 1) {
                        if (actionListener != null) actionListener.onCopyText(item.getMessage());
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class LeftViewHolder extends RecyclerView.ViewHolder {
        TextView tvSenderName, tvMessage, tvTime;
        MaterialCardView cvReplyContainer, cvImageContainer, cvDocumentContainer;
        TextView tvReplySender, tvReplyMessage, tvDocumentName;
        ImageView ivMessageImage;

        public LeftViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            cvReplyContainer = itemView.findViewById(R.id.cvReplyContainer);
            tvReplySender = itemView.findViewById(R.id.tvReplySender);
            tvReplyMessage = itemView.findViewById(R.id.tvReplyMessage);
            cvImageContainer = itemView.findViewById(R.id.cvImageContainer);
            ivMessageImage = itemView.findViewById(R.id.ivMessageImage);
            cvDocumentContainer = itemView.findViewById(R.id.cvDocumentContainer);
            tvDocumentName = itemView.findViewById(R.id.tvDocumentName);
        }
    }

    public static class RightViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvStatusCheck;
        MaterialCardView cvReplyContainer, cvImageContainer, cvDocumentContainer;
        TextView tvReplySender, tvReplyMessage, tvDocumentName;
        ImageView ivMessageImage;

        public RightViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatusCheck = itemView.findViewById(R.id.tvStatusCheck);
            cvReplyContainer = itemView.findViewById(R.id.cvReplyContainer);
            tvReplySender = itemView.findViewById(R.id.tvReplySender);
            tvReplyMessage = itemView.findViewById(R.id.tvReplyMessage);
            cvImageContainer = itemView.findViewById(R.id.cvImageContainer);
            ivMessageImage = itemView.findViewById(R.id.ivMessageImage);
            cvDocumentContainer = itemView.findViewById(R.id.cvDocumentContainer);
            tvDocumentName = itemView.findViewById(R.id.tvDocumentName);
        }
    }
}
