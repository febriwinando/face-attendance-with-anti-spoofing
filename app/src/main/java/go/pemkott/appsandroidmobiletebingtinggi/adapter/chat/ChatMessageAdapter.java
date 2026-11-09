package go.pemkott.appsandroidmobiletebingtinggi.adapter.chat;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
        void onViewMedia(String url, String type, String fileName);
        default void onViewMedia(String url, String type) {
            onViewMedia(url, type, "");
        }
    }

    private boolean isVideoType(String msgType, String url) {
        if ("video".equals(msgType)) return true;
        if (url == null) return false;
        String l = url.toLowerCase();
        return l.endsWith(".mp4") || l.endsWith(".mkv") || l.endsWith(".mov") || l.endsWith(".3gp") || l.endsWith(".avi") || l.endsWith(".webm");
    }

    private boolean isAudioType(String msgType, String url) {
        if ("audio".equals(msgType)) return true;
        if (url == null) return false;
        String l = url.toLowerCase();
        return l.endsWith(".mp3") || l.endsWith(".wav") || l.endsWith(".m4a") || l.endsWith(".aac") || l.endsWith(".ogg") || l.endsWith(".opus") || l.endsWith(".amr");
    }

    private boolean isImageType(String msgType, String url) {
        if ("image".equals(msgType)) return true;
        if (url == null) return false;
        String l = url.toLowerCase();
        return l.endsWith(".jpg") || l.endsWith(".jpeg") || l.endsWith(".png") || l.endsWith(".webp") || l.endsWith(".gif") || l.endsWith(".heic");
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

            boolean isVideo = isVideoType(item.getMsgType(), item.getFileUrl());
            boolean isImage = isImageType(item.getMsgType(), item.getFileUrl());
            boolean isAudio = isAudioType(item.getMsgType(), item.getFileUrl());

            // Image / Video attachment
            if ((isImage || isVideo) && !TextUtils.isEmpty(item.getFileUrl())) {
                r.cvImageContainer.setVisibility(View.VISIBLE);
                loadImageWithFallback(r.ivMessageImage, item.getFileUrl());
                if (r.flVideoOverlay != null) {
                    r.flVideoOverlay.setVisibility(isVideo ? View.VISIBLE : View.GONE);
                }

                r.cvImageContainer.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onViewMedia(item.getFileUrl(), isVideo ? "video" : "image", item.getFileName());
                    }
                });
            } else {
                r.cvImageContainer.setVisibility(View.GONE);
                if (r.flVideoOverlay != null) {
                    r.flVideoOverlay.setVisibility(View.GONE);
                }
            }

            // Document / Audio attachment
            if ((isAudio || "document".equals(item.getMsgType()) || !TextUtils.isEmpty(item.getFileUrl())) && !isImage && !isVideo) {
                r.cvDocumentContainer.setVisibility(View.VISIBLE);
                r.tvDocumentName.setText(item.getFileName());

                if (isAudio) {
                    if (r.ivDocumentIcon != null) {
                        r.ivDocumentIcon.setImageResource(R.drawable.ic_audiotrack);
                        r.ivDocumentIcon.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#93C5FD")));
                    }
                    r.cvDocumentContainer.setOnClickListener(v -> {
                        if (actionListener != null) actionListener.onViewMedia(item.getFileUrl(), "audio", item.getFileName());
                    });
                } else {
                    if (r.ivDocumentIcon != null) {
                        r.ivDocumentIcon.setImageResource(R.drawable.ic_document_scanner);
                        r.ivDocumentIcon.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                    }
                    r.cvDocumentContainer.setOnClickListener(v -> {
                        if (actionListener != null) actionListener.onViewMedia(item.getFileUrl(), "document", item.getFileName());
                    });
                }
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

            // Adjust padding & background for image-only messages
            boolean isMediaOnly = (isImage || isVideo) && TextUtils.isEmpty(item.getMessage());
            if (r.llBubbleContainer != null) {
                if (isMediaOnly) {
                    r.llBubbleContainer.setPadding(0, 0, 0, 0);
                    r.llBubbleContainer.setBackgroundResource(0);
                } else {
                    int pHoriz = (int) (10 * context.getResources().getDisplayMetrics().density);
                    int pVert = (int) (8 * context.getResources().getDisplayMetrics().density);
                    r.llBubbleContainer.setPadding(pHoriz, pVert, pHoriz, pVert);
                    r.llBubbleContainer.setBackgroundResource(R.drawable.chat_bubble_right_bg);
                }
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

            boolean isVideoLeft = isVideoType(item.getMsgType(), item.getFileUrl());
            boolean isImageLeft = isImageType(item.getMsgType(), item.getFileUrl());
            boolean isAudioLeft = isAudioType(item.getMsgType(), item.getFileUrl());

            // Image / Video attachment
            if ((isImageLeft || isVideoLeft) && !TextUtils.isEmpty(item.getFileUrl())) {
                l.cvImageContainer.setVisibility(View.VISIBLE);
                loadImageWithFallback(l.ivMessageImage, item.getFileUrl());
                if (l.flVideoOverlay != null) {
                    l.flVideoOverlay.setVisibility(isVideoLeft ? View.VISIBLE : View.GONE);
                }

                l.cvImageContainer.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onViewMedia(item.getFileUrl(), isVideoLeft ? "video" : "image", item.getFileName());
                    }
                });
            } else {
                l.cvImageContainer.setVisibility(View.GONE);
                if (l.flVideoOverlay != null) {
                    l.flVideoOverlay.setVisibility(View.GONE);
                }
            }

            // Document / Audio attachment
            if ((isAudioLeft || "document".equals(item.getMsgType()) || !TextUtils.isEmpty(item.getFileUrl())) && !isImageLeft && !isVideoLeft) {
                l.cvDocumentContainer.setVisibility(View.VISIBLE);
                l.tvDocumentName.setText(item.getFileName());

                if (isAudioLeft) {
                    if (l.ivDocumentIcon != null) {
                        l.ivDocumentIcon.setImageResource(R.drawable.ic_audiotrack);
                        l.ivDocumentIcon.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2563EB")));
                    }
                    l.cvDocumentContainer.setOnClickListener(v -> {
                        if (actionListener != null) actionListener.onViewMedia(item.getFileUrl(), "audio", item.getFileName());
                    });
                } else {
                    if (l.ivDocumentIcon != null) {
                        l.ivDocumentIcon.setImageResource(R.drawable.ic_document_scanner);
                        l.ivDocumentIcon.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#DC2626")));
                    }
                    l.cvDocumentContainer.setOnClickListener(v -> {
                        if (actionListener != null) actionListener.onViewMedia(item.getFileUrl(), "document", item.getFileName());
                    });
                }
            } else {
                l.cvDocumentContainer.setVisibility(View.GONE);
            }

            l.tvTime.setText(item.getCreatedAtFormatted());

            // Adjust padding & background for image-only messages
            boolean isMediaOnlyLeft = (isImageLeft || isVideoLeft) && TextUtils.isEmpty(item.getMessage());
            if (l.llBubbleContainer != null) {
                if (isMediaOnlyLeft) {
                    l.llBubbleContainer.setPadding(0, 0, 0, 0);
                    l.llBubbleContainer.setBackgroundResource(0);
                } else {
                    int pHoriz = (int) (10 * context.getResources().getDisplayMetrics().density);
                    int pVert = (int) (8 * context.getResources().getDisplayMetrics().density);
                    l.llBubbleContainer.setPadding(pHoriz, pVert, pHoriz, pVert);
                    l.llBubbleContainer.setBackgroundResource(R.drawable.chat_bubble_left_bg);
                }
            }

            // Long Press Options
            l.itemView.setOnLongClickListener(v -> {
                showOptionsDialog(item);
                return true;
            });
        }
    }

    private void loadImageWithFallback(ImageView imageView, String primaryUrl) {
        if (TextUtils.isEmpty(primaryUrl)) return;

        Glide.with(context)
                .load(primaryUrl)
                .centerCrop()
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        String fallbackUrl = primaryUrl;
                        if (primaryUrl.contains(":8443")) {
                            fallbackUrl = primaryUrl.replace("https://172.15.1.35:8443", "http://172.15.1.35:8000");
                        } else if (primaryUrl.contains(":8000")) {
                            fallbackUrl = primaryUrl.replace("http://172.15.1.35:8000", "https://172.15.1.35:8443");
                        }

                        if (!fallbackUrl.equals(primaryUrl)) {
                            Glide.with(context).load(fallbackUrl).centerCrop().into(imageView);
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(imageView);
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
        LinearLayout llBubbleContainer;
        TextView tvSenderName, tvMessage, tvTime;
        MaterialCardView cvReplyContainer, cvImageContainer, cvDocumentContainer;
        TextView tvReplySender, tvReplyMessage, tvDocumentName;
        ImageView ivMessageImage, ivDocumentIcon;
        FrameLayout flVideoOverlay;

        public LeftViewHolder(@NonNull View itemView) {
            super(itemView);
            llBubbleContainer = itemView.findViewById(R.id.llBubbleContainer);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            cvReplyContainer = itemView.findViewById(R.id.cvReplyContainer);
            tvReplySender = itemView.findViewById(R.id.tvReplySender);
            tvReplyMessage = itemView.findViewById(R.id.tvReplyMessage);
            cvImageContainer = itemView.findViewById(R.id.cvImageContainer);
            ivMessageImage = itemView.findViewById(R.id.ivMessageImage);
            flVideoOverlay = itemView.findViewById(R.id.flVideoOverlay);
            cvDocumentContainer = itemView.findViewById(R.id.cvDocumentContainer);
            tvDocumentName = itemView.findViewById(R.id.tvDocumentName);
            ivDocumentIcon = itemView.findViewById(R.id.ivDocumentIcon);
        }
    }

    public static class RightViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llBubbleContainer;
        TextView tvMessage, tvTime, tvStatusCheck;
        MaterialCardView cvReplyContainer, cvImageContainer, cvDocumentContainer;
        TextView tvReplySender, tvReplyMessage, tvDocumentName;
        ImageView ivMessageImage, ivDocumentIcon;
        FrameLayout flVideoOverlay;

        public RightViewHolder(@NonNull View itemView) {
            super(itemView);
            llBubbleContainer = itemView.findViewById(R.id.llBubbleContainer);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatusCheck = itemView.findViewById(R.id.tvStatusCheck);
            cvReplyContainer = itemView.findViewById(R.id.cvReplyContainer);
            tvReplySender = itemView.findViewById(R.id.tvReplySender);
            tvReplyMessage = itemView.findViewById(R.id.tvReplyMessage);
            cvImageContainer = itemView.findViewById(R.id.cvImageContainer);
            ivMessageImage = itemView.findViewById(R.id.ivMessageImage);
            flVideoOverlay = itemView.findViewById(R.id.flVideoOverlay);
            cvDocumentContainer = itemView.findViewById(R.id.cvDocumentContainer);
            tvDocumentName = itemView.findViewById(R.id.tvDocumentName);
            ivDocumentIcon = itemView.findViewById(R.id.ivDocumentIcon);
        }
    }
}
