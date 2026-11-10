package go.pemkott.appsandroidmobiletebingtinggi.adapter.chat;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMessageItem;
import go.pemkott.appsandroidmobiletebingtinggi.widget.AudioWaveformView;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnMessageActionListener {
        void onReplyMessage(ChatMessageItem item);
        void onCopyText(String text);
        void onEditMessage(ChatMessageItem item);
        void onForwardMessage(ChatMessageItem item);
        void onViewMedia(String url, String type, String fileName);
        default void onViewMedia(String url, String type) {
            onViewMedia(url, type, "");
        }
    }

    private boolean isAudioType(String msgType, String url) {
        if ("audio".equals(msgType)) return true;
        if (url == null) return false;
        String l = url.toLowerCase();
        return l.endsWith(".mp3") || l.endsWith(".wav") || l.endsWith(".m4a") || l.endsWith(".aac") || l.endsWith(".ogg") || l.endsWith(".opus") || l.endsWith(".amr") || l.contains("/chat_media/audio/") || l.contains("voice_note");
    }

    private boolean isVideoType(String msgType, String url) {
        if (isAudioType(msgType, url)) return false;
        if ("video".equals(msgType)) return true;
        if (url == null) return false;
        String l = url.toLowerCase();
        return l.endsWith(".mp4") || l.endsWith(".mkv") || l.endsWith(".mov") || l.endsWith(".3gp") || l.endsWith(".avi") || l.endsWith(".webm");
    }

    private boolean isImageType(String msgType, String url) {
        if (isAudioType(msgType, url)) return false;
        if ("image".equals(msgType)) return true;
        if (url == null) return false;
        String l = url.toLowerCase();
        return l.endsWith(".jpg") || l.endsWith(".jpeg") || l.endsWith(".png") || l.endsWith(".webp") || l.endsWith(".gif") || l.endsWith(".heic");
    }

    private static final int TYPE_LEFT = 1;
    private static final int TYPE_RIGHT = 2;

    private final Context context;
    private List<ChatMessageItem> list;
    private final OnMessageActionListener actionListener;

    // Media Player and Audio Tracking state
    private MediaPlayer mediaPlayer = null;
    private long currentPlayingId = -1;
    private boolean isAudioPlaying = false;
    private final Handler audioHandler = new Handler(Looper.getMainLooper());
    private Runnable audioProgressRunnable = null;
    private RecyclerView attachedRecyclerView = null;

    // Inline Video Player Tracking state
    private long currentPlayingVideoId = -1;
    private VideoView currentVideoView = null;
    private boolean isVideoPlaying = false;
    private final Handler videoHandler = new Handler(Looper.getMainLooper());
    private Runnable videoProgressRunnable = null;

    // Search Highlight Support (Telegram Style)
    private int highlightedPosition = -1;

    public void setHighlightedPosition(int position) {
        int prev = this.highlightedPosition;
        this.highlightedPosition = position;
        if (prev != -1 && prev < list.size()) notifyItemChanged(prev);
        if (position != -1 && position < list.size()) notifyItemChanged(position);
    }

    public int getHighlightedPosition() {
        return highlightedPosition;
    }

    public ChatMessageAdapter(Context context, List<ChatMessageItem> list, OnMessageActionListener listener) {
        this.context = context;
        this.list = list != null ? list : new ArrayList<>();
        this.actionListener = listener;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.attachedRecyclerView = recyclerView;
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

    public ChatMessageItem getItemAt(int position) {
        if (position >= 0 && position < list.size()) {
            return list.get(position);
        }
        return null;
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

        boolean showDateHeader = (position == 0) || isDifferentDay(item, list.get(position - 1));

        if (holder instanceof RightViewHolder) {
            RightViewHolder r = (RightViewHolder) holder;

            // Date Header Separator
            if (r.llDateHeader != null) {
                if (showDateHeader) {
                    r.llDateHeader.setVisibility(View.VISIBLE);
                    if (r.tvDateHeader != null) r.tvDateHeader.setText(getDateLabel(item));
                } else {
                    r.llDateHeader.setVisibility(View.GONE);
                }
            }

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

            // 1. Image Attachment (Exclusive for static pictures - opens Zoomable Modal)
            if (isImage && !isVideo && !isAudio && !TextUtils.isEmpty(item.getFileUrl())) {
                r.cvImageContainer.setVisibility(View.VISIBLE);
                loadImageWithFallback(r.ivMessageImage, item.getFileUrl());
                r.cvImageContainer.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onViewMedia(item.getFileUrl(), "image", item.getFileName());
                    }
                });
            } else {
                r.cvImageContainer.setVisibility(View.GONE);
            }

            // 2. Inline Video Player (Played directly inside the chat item)
            if (isVideo && !isAudio && !TextUtils.isEmpty(item.getFileUrl())) {
                r.cvVideoContainer.setVisibility(View.VISIBLE);
                bindVideoView(item, r);
            } else {
                r.cvVideoContainer.setVisibility(View.GONE);
            }

            // 3. Modern Capsule Voice Note Player (Audio waveform pill)
            if (isAudio) {
                r.llAudioContainer.setVisibility(View.VISIBLE);
                bindAudioView(item, r);
            } else {
                r.llAudioContainer.setVisibility(View.GONE);
            }

            // 4. Document attachment (Non-audio, non-image, non-video)
            if (!isAudio && !isImage && !isVideo && !TextUtils.isEmpty(item.getFileUrl())) {
                r.cvDocumentContainer.setVisibility(View.VISIBLE);
                r.tvDocumentName.setText(item.getFileName());
                if (r.ivDocumentIcon != null) {
                    r.ivDocumentIcon.setImageResource(R.drawable.ic_document_scanner);
                    r.ivDocumentIcon.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                }
                r.cvDocumentContainer.setOnClickListener(v -> {
                    if (actionListener != null) actionListener.onViewMedia(item.getFileUrl(), "document", item.getFileName());
                });
            } else {
                r.cvDocumentContainer.setVisibility(View.GONE);
            }

            // Time & Checkmarks
            r.tvTime.setText(item.getCreatedAtFormatted());
            if (item.isRead()) {
                r.tvStatusCheck.setText("✓✓");
                r.tvStatusCheck.setTextColor(Color.parseColor("#93C5FD")); // Biru cerah double-check (sudah dibaca)
            } else {
                r.tvStatusCheck.setText("✓");
                r.tvStatusCheck.setTextColor(Color.parseColor("#E2E8F0")); // Abu-abu muda single-check (terkirim belum dibaca)
            }

            // Adjust padding & background for image-only messages and voice note capsule
            boolean hasRealCaption = !TextUtils.isEmpty(item.getMessage())
                    && !"Pesan Suara".equalsIgnoreCase(item.getMessage().trim())
                    && !"Voice Note".equalsIgnoreCase(item.getMessage().trim())
                    && !"Audio".equalsIgnoreCase(item.getMessage().trim())
                    && !item.getMessage().trim().toLowerCase().endsWith(".mp3")
                    && !item.getMessage().trim().toLowerCase().endsWith(".m4a")
                    && !item.getMessage().trim().toLowerCase().endsWith(".wav")
                    && !item.getMessage().trim().toLowerCase().endsWith(".aac")
                    && !item.getMessage().trim().toLowerCase().endsWith(".ogg")
                    && !item.getMessage().trim().toLowerCase().endsWith(".opus")
                    && (item.getFileName() == null || !item.getMessage().trim().equalsIgnoreCase(item.getFileName().trim()));
            boolean isVoiceNoteOnly = isAudio && !hasRealCaption;
            boolean isMediaOnly = (isImage && !isVideo && !isAudio && TextUtils.isEmpty(item.getMessage())) || isVoiceNoteOnly;
            if (r.llBubbleContainer != null) {
                if (position == highlightedPosition) {
                    int pHoriz = (int) (12 * context.getResources().getDisplayMetrics().density);
                    int pVert = (int) (10 * context.getResources().getDisplayMetrics().density);
                    r.llBubbleContainer.setPadding(pHoriz, pVert, pHoriz, pVert);
                    r.llBubbleContainer.setBackgroundResource(R.drawable.bg_bubble_highlight);
                } else if (isMediaOnly) {
                    r.llBubbleContainer.setPadding(0, 0, 0, 0);
                    r.llBubbleContainer.setBackgroundResource(0);
                } else {
                    int pHoriz = (int) (10 * context.getResources().getDisplayMetrics().density);
                    int pVert = (int) (8 * context.getResources().getDisplayMetrics().density);
                    r.llBubbleContainer.setPadding(pHoriz, pVert, pHoriz, pVert);
                    r.llBubbleContainer.setBackgroundResource(R.drawable.chat_bubble_right_bg);
                }
            }

            if (isVoiceNoteOnly) {
                if (r.llTimeMetadata != null) r.llTimeMetadata.setVisibility(View.GONE);
                if (r.tvMessage != null) r.tvMessage.setVisibility(View.GONE);
            } else {
                if (r.llTimeMetadata != null) r.llTimeMetadata.setVisibility(View.VISIBLE);
            }

            // Long Press Options
            r.itemView.setOnLongClickListener(v -> {
                showOptionsDialog(item);
                return true;
            });

        } else if (holder instanceof LeftViewHolder) {
            LeftViewHolder l = (LeftViewHolder) holder;

            // Date Header Separator
            if (l.llDateHeader != null) {
                if (showDateHeader) {
                    l.llDateHeader.setVisibility(View.VISIBLE);
                    if (l.tvDateHeader != null) l.tvDateHeader.setText(getDateLabel(item));
                } else {
                    l.llDateHeader.setVisibility(View.GONE);
                }
            }

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

            // 1. Image Attachment (Exclusive for static pictures - opens Zoomable Modal)
            if (isImageLeft && !isVideoLeft && !isAudioLeft && !TextUtils.isEmpty(item.getFileUrl())) {
                l.cvImageContainer.setVisibility(View.VISIBLE);
                loadImageWithFallback(l.ivMessageImage, item.getFileUrl());
                l.cvImageContainer.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onViewMedia(item.getFileUrl(), "image", item.getFileName());
                    }
                });
            } else {
                l.cvImageContainer.setVisibility(View.GONE);
            }

            // 2. Inline Video Player (Played directly inside the chat item)
            if (isVideoLeft && !isAudioLeft && !TextUtils.isEmpty(item.getFileUrl())) {
                l.cvVideoContainer.setVisibility(View.VISIBLE);
                bindVideoView(item, l);
            } else {
                l.cvVideoContainer.setVisibility(View.GONE);
            }

            // 3. Modern Capsule Voice Note Player (Audio waveform pill)
            if (isAudioLeft) {
                l.llAudioContainer.setVisibility(View.VISIBLE);
                bindAudioView(item, l);
            } else {
                l.llAudioContainer.setVisibility(View.GONE);
            }

            // 4. Document attachment (Non-audio, non-image, non-video)
            if (!isAudioLeft && !isImageLeft && !isVideoLeft && !TextUtils.isEmpty(item.getFileUrl())) {
                l.cvDocumentContainer.setVisibility(View.VISIBLE);
                l.tvDocumentName.setText(item.getFileName());
                if (l.ivDocumentIcon != null) {
                    l.ivDocumentIcon.setImageResource(R.drawable.ic_document_scanner);
                    l.ivDocumentIcon.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#DC2626")));
                }
                l.cvDocumentContainer.setOnClickListener(v -> {
                    if (actionListener != null) actionListener.onViewMedia(item.getFileUrl(), "document", item.getFileName());
                });
            } else {
                l.cvDocumentContainer.setVisibility(View.GONE);
            }

            l.tvTime.setText(item.getCreatedAtFormatted());

            // Adjust padding & background for image-only messages and voice note capsule
            boolean hasRealCaptionLeft = !TextUtils.isEmpty(item.getMessage())
                    && !"Pesan Suara".equalsIgnoreCase(item.getMessage().trim())
                    && !"Voice Note".equalsIgnoreCase(item.getMessage().trim())
                    && !"Audio".equalsIgnoreCase(item.getMessage().trim())
                    && !item.getMessage().trim().toLowerCase().endsWith(".mp3")
                    && !item.getMessage().trim().toLowerCase().endsWith(".m4a")
                    && !item.getMessage().trim().toLowerCase().endsWith(".wav")
                    && !item.getMessage().trim().toLowerCase().endsWith(".aac")
                    && !item.getMessage().trim().toLowerCase().endsWith(".ogg")
                    && !item.getMessage().trim().toLowerCase().endsWith(".opus")
                    && (item.getFileName() == null || !item.getMessage().trim().equalsIgnoreCase(item.getFileName().trim()));
            boolean isVoiceNoteOnlyLeft = isAudioLeft && !hasRealCaptionLeft;
            boolean isMediaOnlyLeft = (isImageLeft && !isVideoLeft && !isAudioLeft && TextUtils.isEmpty(item.getMessage())) || isVoiceNoteOnlyLeft;
            if (l.llBubbleContainer != null) {
                if (position == highlightedPosition) {
                    int pHoriz = (int) (12 * context.getResources().getDisplayMetrics().density);
                    int pVert = (int) (10 * context.getResources().getDisplayMetrics().density);
                    l.llBubbleContainer.setPadding(pHoriz, pVert, pHoriz, pVert);
                    l.llBubbleContainer.setBackgroundResource(R.drawable.bg_bubble_highlight);
                } else if (isMediaOnlyLeft) {
                    l.llBubbleContainer.setPadding(0, 0, 0, 0);
                    l.llBubbleContainer.setBackgroundResource(0);
                } else {
                    int pHoriz = (int) (10 * context.getResources().getDisplayMetrics().density);
                    int pVert = (int) (8 * context.getResources().getDisplayMetrics().density);
                    l.llBubbleContainer.setPadding(pHoriz, pVert, pHoriz, pVert);
                    l.llBubbleContainer.setBackgroundResource(R.drawable.chat_bubble_left_bg);
                }
            }

            if (isVoiceNoteOnlyLeft) {
                if (l.tvTime != null) l.tvTime.setVisibility(View.GONE);
                if (l.tvMessage != null) l.tvMessage.setVisibility(View.GONE);
            } else {
                if (l.tvTime != null) l.tvTime.setVisibility(View.VISIBLE);
            }

            // Long Press Options
            l.itemView.setOnLongClickListener(v -> {
                showOptionsDialog(item);
                return true;
            });
        }
    }

    // =========================================================================
    // INLINE VIDEO PLAYBACK HANDLERS
    // =========================================================================

    private void bindVideoView(ChatMessageItem item, RecyclerView.ViewHolder holder) {
        VideoView vv = null;
        ImageView ivThumb = null;
        ProgressBar pbBuff = null;
        ImageView ivPlayCenter = null;
        ImageView ivPlayMini = null;
        SeekBar sbProgress = null;
        TextView tvDuration = null;
        ImageView ivFullscreen = null;

        if (holder instanceof RightViewHolder) {
            RightViewHolder r = (RightViewHolder) holder;
            vv = r.vvInlineVideo;
            ivThumb = r.ivVideoThumbnail;
            pbBuff = r.pbVideoBuffering;
            ivPlayCenter = r.ivInlineVideoPlayCenter;
            ivPlayMini = r.ivInlineVideoPlayMini;
            sbProgress = r.sbInlineVideoProgress;
            tvDuration = r.tvInlineVideoDuration;
            ivFullscreen = r.ivInlineVideoFullscreen;
        } else if (holder instanceof LeftViewHolder) {
            LeftViewHolder l = (LeftViewHolder) holder;
            vv = l.vvInlineVideo;
            ivThumb = l.ivVideoThumbnail;
            pbBuff = l.pbVideoBuffering;
            ivPlayCenter = l.ivInlineVideoPlayCenter;
            ivPlayMini = l.ivInlineVideoPlayMini;
            sbProgress = l.sbInlineVideoProgress;
            tvDuration = l.tvInlineVideoDuration;
            ivFullscreen = l.ivInlineVideoFullscreen;
        }

        if (vv == null) return;

        boolean isThisActive = (currentPlayingVideoId == item.getId());
        String formattedUrl = formatMediaUrl(item.getFileUrl());

        if (isThisActive && currentVideoView != null) {
            if (isVideoPlaying) {
                if (ivThumb != null) ivThumb.setVisibility(View.GONE);
                if (ivPlayCenter != null) ivPlayCenter.setVisibility(View.GONE);
                if (pbBuff != null) pbBuff.setVisibility(View.GONE);
                if (ivPlayMini != null) ivPlayMini.setImageResource(R.drawable.ic_pause);
            } else {
                if (ivThumb != null) ivThumb.setVisibility(View.GONE);
                if (ivPlayCenter != null) ivPlayCenter.setVisibility(View.VISIBLE);
                if (pbBuff != null) pbBuff.setVisibility(View.GONE);
                if (ivPlayMini != null) ivPlayMini.setImageResource(R.drawable.ic_play_arrow);
            }
            try {
                int cur = currentVideoView.getCurrentPosition();
                int dur = currentVideoView.getDuration();
                if (sbProgress != null) sbProgress.setProgress(dur > 0 ? (int) (((float) cur / dur) * 100) : 0);
                if (tvDuration != null) tvDuration.setText(formatTime(cur) + " / " + formatTime(dur));
            } catch (Exception ignored) {}
        } else {
            if (ivThumb != null) {
                ivThumb.setVisibility(View.VISIBLE);
                loadVideoThumbnail(ivThumb, formattedUrl);
            }
            if (ivPlayCenter != null) ivPlayCenter.setVisibility(View.VISIBLE);
            if (pbBuff != null) pbBuff.setVisibility(View.GONE);
            if (ivPlayMini != null) ivPlayMini.setImageResource(R.drawable.ic_play_arrow);
            if (sbProgress != null) sbProgress.setProgress(0);
            if (tvDuration != null) tvDuration.setText("0:00");
        }

        if (ivPlayCenter != null) {
            ivPlayCenter.setOnClickListener(v -> handleVideoToggle(item, holder));
        }
        if (ivPlayMini != null) {
            ivPlayMini.setOnClickListener(v -> handleVideoToggle(item, holder));
        }
        if (ivFullscreen != null) {
            ivFullscreen.setOnClickListener(v -> {
                if (currentPlayingVideoId == item.getId()) {
                    pauseVideo();
                }
                if (actionListener != null) {
                    actionListener.onViewMedia(item.getFileUrl(), "video", item.getFileName());
                }
            });
        }
        if (sbProgress != null) {
            setupVideoSeekBar(sbProgress, item.getId());
        }
    }

    private void handleVideoToggle(ChatMessageItem item, RecyclerView.ViewHolder holder) {
        long msgId = item.getId();
        stopAudio();

        VideoView vv = null;
        ImageView ivThumb = null;
        ProgressBar pbBuff = null;
        ImageView ivPlayCenter = null;
        ImageView ivPlayMini = null;
        SeekBar sbProgress = null;
        TextView tvDuration = null;

        if (holder instanceof RightViewHolder) {
            RightViewHolder r = (RightViewHolder) holder;
            vv = r.vvInlineVideo;
            ivThumb = r.ivVideoThumbnail;
            pbBuff = r.pbVideoBuffering;
            ivPlayCenter = r.ivInlineVideoPlayCenter;
            ivPlayMini = r.ivInlineVideoPlayMini;
            sbProgress = r.sbInlineVideoProgress;
            tvDuration = r.tvInlineVideoDuration;
        } else if (holder instanceof LeftViewHolder) {
            LeftViewHolder l = (LeftViewHolder) holder;
            vv = l.vvInlineVideo;
            ivThumb = l.ivVideoThumbnail;
            pbBuff = l.pbVideoBuffering;
            ivPlayCenter = l.ivInlineVideoPlayCenter;
            ivPlayMini = l.ivInlineVideoPlayMini;
            sbProgress = l.sbInlineVideoProgress;
            tvDuration = l.tvInlineVideoDuration;
        }

        if (vv == null) return;

        if (currentPlayingVideoId == msgId && currentVideoView != null) {
            // Toggle play/pause for currently loaded video
            try {
                if (currentVideoView.isPlaying()) {
                    currentVideoView.pause();
                    isVideoPlaying = false;
                    if (ivPlayMini != null) ivPlayMini.setImageResource(R.drawable.ic_play_arrow);
                    if (ivPlayCenter != null) ivPlayCenter.setVisibility(View.VISIBLE);
                    if (videoProgressRunnable != null) videoHandler.removeCallbacks(videoProgressRunnable);
                } else {
                    currentVideoView.start();
                    isVideoPlaying = true;
                    if (ivPlayMini != null) ivPlayMini.setImageResource(R.drawable.ic_pause);
                    if (ivPlayCenter != null) ivPlayCenter.setVisibility(View.GONE);
                    if (ivThumb != null) ivThumb.setVisibility(View.GONE);
                    startVideoProgressTracker();
                }
            } catch (Exception e) {
                stopVideo();
            }
            return;
        }

        // New video playback
        stopVideo();
        currentPlayingVideoId = msgId;
        currentVideoView = vv;
        isVideoPlaying = false;

        if (ivPlayCenter != null) ivPlayCenter.setVisibility(View.GONE);
        if (pbBuff != null) pbBuff.setVisibility(View.VISIBLE);
        if (ivPlayMini != null) ivPlayMini.setImageResource(R.drawable.ic_play_arrow);

        String playUrl = formatMediaUrl(item.getFileUrl());
        if (TextUtils.isEmpty(playUrl)) {
            if (pbBuff != null) pbBuff.setVisibility(View.GONE);
            if (ivPlayCenter != null) ivPlayCenter.setVisibility(View.VISIBLE);
            return;
        }

        try {
            final VideoView targetVv = vv;
            final ImageView targetThumb = ivThumb;
            final ProgressBar targetPb = pbBuff;
            final ImageView targetCenter = ivPlayCenter;
            final ImageView targetMini = ivPlayMini;

            targetVv.setVideoURI(Uri.parse(playUrl));
            targetVv.setOnPreparedListener(mp -> {
                if (currentPlayingVideoId == msgId) {
                    if (targetPb != null) targetPb.setVisibility(View.GONE);
                    if (targetThumb != null) targetThumb.setVisibility(View.GONE);
                    if (targetCenter != null) targetCenter.setVisibility(View.GONE);
                    if (targetMini != null) targetMini.setImageResource(R.drawable.ic_pause);

                    mp.start();
                    isVideoPlaying = true;
                    startVideoProgressTracker();
                }
            });

            targetVv.setOnCompletionListener(mp -> {
                if (targetMini != null) targetMini.setImageResource(R.drawable.ic_play_arrow);
                if (targetCenter != null) targetCenter.setVisibility(View.VISIBLE);
                if (targetThumb != null) targetThumb.setVisibility(View.VISIBLE);
                isVideoPlaying = false;
                if (videoProgressRunnable != null) videoHandler.removeCallbacks(videoProgressRunnable);
                currentPlayingVideoId = -1;
            });

            targetVv.setOnErrorListener((mp, what, extra) -> {
                if (targetPb != null) targetPb.setVisibility(View.GONE);
                if (targetCenter != null) targetCenter.setVisibility(View.VISIBLE);
                if (targetThumb != null) targetThumb.setVisibility(View.VISIBLE);
                if (targetMini != null) targetMini.setImageResource(R.drawable.ic_play_arrow);
                stopVideo();
                return true;
            });

        } catch (Exception e) {
            stopVideo();
        }
    }

    private void startVideoProgressTracker() {
        if (videoProgressRunnable != null) {
            videoHandler.removeCallbacks(videoProgressRunnable);
        }
        videoProgressRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentVideoView != null && isVideoPlaying && currentPlayingVideoId != -1) {
                    try {
                        int cur = currentVideoView.getCurrentPosition();
                        int dur = currentVideoView.getDuration();
                        int progress = dur > 0 ? (int) (((float) cur / dur) * 100) : 0;

                        int pos = getPositionForMessageId(currentPlayingVideoId);
                        if (pos != -1 && attachedRecyclerView != null) {
                            RecyclerView.ViewHolder holder = attachedRecyclerView.findViewHolderForAdapterPosition(pos);
                            if (holder instanceof RightViewHolder) {
                                RightViewHolder r = (RightViewHolder) holder;
                                r.sbInlineVideoProgress.setProgress(progress);
                                r.tvInlineVideoDuration.setText(formatTime(cur) + " / " + formatTime(dur));
                            } else if (holder instanceof LeftViewHolder) {
                                LeftViewHolder l = (LeftViewHolder) holder;
                                l.sbInlineVideoProgress.setProgress(progress);
                                l.tvInlineVideoDuration.setText(formatTime(cur) + " / " + formatTime(dur));
                            }
                        }
                        videoHandler.postDelayed(this, 250);
                    } catch (Exception ignored) {}
                }
            }
        };
        videoHandler.post(videoProgressRunnable);
    }

    private void setupVideoSeekBar(SeekBar sb, long msgId) {
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && currentPlayingVideoId == msgId && currentVideoView != null) {
                    try {
                        int duration = currentVideoView.getDuration();
                        int targetPos = (int) (((float) progress / 100) * duration);
                        currentVideoView.seekTo(targetPos);
                    } catch (Exception ignored) {}
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    public void stopVideo() {
        if (videoProgressRunnable != null) {
            videoHandler.removeCallbacks(videoProgressRunnable);
            videoProgressRunnable = null;
        }
        if (currentVideoView != null) {
            try {
                if (currentVideoView.isPlaying()) {
                    currentVideoView.stopPlayback();
                }
            } catch (Exception ignored) {}
            currentVideoView = null;
        }
        long prevId = currentPlayingVideoId;
        currentPlayingVideoId = -1;
        isVideoPlaying = false;
        if (prevId != -1) {
            int pos = getPositionForMessageId(prevId);
            if (pos != -1) notifyItemChanged(pos);
        }
    }

    public void pauseVideo() {
        if (currentVideoView != null && isVideoPlaying) {
            try {
                currentVideoView.pause();
                isVideoPlaying = false;
                if (videoProgressRunnable != null) videoHandler.removeCallbacks(videoProgressRunnable);
                int pos = getPositionForMessageId(currentPlayingVideoId);
                if (pos != -1) notifyItemChanged(pos);
            } catch (Exception ignored) {}
        }
    }

    private void loadVideoThumbnail(ImageView imageView, String videoUrl) {
        if (imageView == null || TextUtils.isEmpty(videoUrl)) return;
        try {
            Glide.with(context)
                    .asBitmap()
                    .load(videoUrl)
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_image)
                    .into(imageView);
        } catch (Exception ignored) {}
    }

    // =========================================================================
    // CAPSULE VOICE NOTE (AUDIO) PLAYBACK HANDLERS
    // =========================================================================

    private void bindAudioView(ChatMessageItem item, RecyclerView.ViewHolder holder) {
        FrameLayout flPlayPause = null;
        ImageView ivPlayPause = null;
        AudioWaveformView waveform = null;
        TextView tvDuration = null;

        if (holder instanceof RightViewHolder) {
            RightViewHolder r = (RightViewHolder) holder;
            flPlayPause = r.flAudioPlayPause;
            ivPlayPause = r.ivAudioPlayPause;
            waveform = r.waveformAudio;
            tvDuration = r.tvAudioDuration;
        } else if (holder instanceof LeftViewHolder) {
            LeftViewHolder l = (LeftViewHolder) holder;
            flPlayPause = l.flAudioPlayPause;
            ivPlayPause = l.ivAudioPlayPause;
            waveform = l.waveformAudio;
            tvDuration = l.tvAudioDuration;
        }

        if (ivPlayPause == null || waveform == null || tvDuration == null) return;

        boolean isThisPlaying = (currentPlayingId == item.getId() && isAudioPlaying);
        boolean isThisPaused = (currentPlayingId == item.getId() && !isAudioPlaying && mediaPlayer != null);

        if (isThisPlaying) {
            ivPlayPause.setImageResource(R.drawable.ic_voice_pause_rounded);
            waveform.setPlaying(true);
            try {
                int cur = mediaPlayer.getCurrentPosition();
                int dur = mediaPlayer.getDuration();
                waveform.setProgress(dur > 0 ? ((float) cur / dur) : 0f);
                tvDuration.setText(formatTime(cur));
            } catch (Exception e) {
                tvDuration.setText("0:00");
            }
        } else if (isThisPaused) {
            ivPlayPause.setImageResource(R.drawable.ic_voice_play_rounded);
            waveform.setPlaying(false);
            try {
                int cur = mediaPlayer.getCurrentPosition();
                int dur = mediaPlayer.getDuration();
                waveform.setProgress(dur > 0 ? ((float) cur / dur) : 0f);
                tvDuration.setText(formatTime(cur));
            } catch (Exception e) {
                tvDuration.setText("0:00");
            }
        } else {
            ivPlayPause.setImageResource(R.drawable.ic_voice_play_rounded);
            waveform.setPlaying(false);
            waveform.setProgress(0f);
            loadAudioDurationAsync(item, tvDuration);
        }

        final ImageView fIv = ivPlayPause;
        final AudioWaveformView fWave = waveform;
        final TextView fTv = tvDuration;

        View.OnClickListener toggleClick = v -> handleAudioToggle(item, fIv, fWave, fTv);
        if (flPlayPause != null) {
            flPlayPause.setOnClickListener(toggleClick);
        } else {
            ivPlayPause.setOnClickListener(toggleClick);
        }

        if (holder instanceof RightViewHolder) {
            ((RightViewHolder) holder).llAudioContainer.setOnClickListener(toggleClick);
        } else if (holder instanceof LeftViewHolder) {
            ((LeftViewHolder) holder).llAudioContainer.setOnClickListener(toggleClick);
        }

        setupAudioWaveformSeek(waveform, item.getId());
    }

    private final java.util.Map<Long, Integer> audioDurationCache = new java.util.concurrent.ConcurrentHashMap<>();

    private void loadAudioDurationAsync(ChatMessageItem item, TextView tvDuration) {
        if (item == null || TextUtils.isEmpty(item.getFileUrl())) {
            tvDuration.setText("0:00");
            return;
        }
        long msgId = item.getId();
        if (audioDurationCache.containsKey(msgId)) {
            int dur = audioDurationCache.get(msgId);
            tvDuration.setText(formatTime(dur));
            return;
        }
        tvDuration.setText("0:00");
        new Thread(() -> {
            try {
                android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
                String formattedUrl = formatMediaUrl(item.getFileUrl());
                if (formattedUrl.startsWith("http://") || formattedUrl.startsWith("https://")) {
                    mmr.setDataSource(formattedUrl, new java.util.HashMap<>());
                } else {
                    mmr.setDataSource(context, Uri.parse(formattedUrl));
                }
                String durStr = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
                mmr.release();
                if (durStr != null) {
                    int dur = Integer.parseInt(durStr);
                    audioDurationCache.put(msgId, dur);
                    audioHandler.post(() -> {
                        if (currentPlayingId != msgId && tvDuration != null) {
                            tvDuration.setText(formatTime(dur));
                        }
                    });
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void handleAudioToggle(ChatMessageItem item, ImageView ivPlayPause, AudioWaveformView waveform, TextView tvDuration) {
        long msgId = item.getId();
        stopVideo();

        if (currentPlayingId == msgId && mediaPlayer != null) {
            // Same message: toggle play/pause
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    isAudioPlaying = false;
                    ivPlayPause.setImageResource(R.drawable.ic_voice_play_rounded);
                    if (waveform != null) waveform.setPlaying(false);
                    if (audioProgressRunnable != null) audioHandler.removeCallbacks(audioProgressRunnable);
                } else {
                    mediaPlayer.start();
                    isAudioPlaying = true;
                    ivPlayPause.setImageResource(R.drawable.ic_voice_pause_rounded);
                    if (waveform != null) waveform.setPlaying(true);
                    startProgressTracker();
                }
            } catch (Exception e) {
                stopAudio();
            }
            return;
        }

        // Different message or new playback
        stopAudio();
        currentPlayingId = msgId;
        isAudioPlaying = false;
        ivPlayPause.setImageResource(R.drawable.ic_voice_play_rounded);
        if (waveform != null) {
            waveform.setPlaying(false);
            waveform.setProgress(0f);
        }
        tvDuration.setText("0:00");

        String playUrl = formatMediaUrl(item.getFileUrl());
        if (TextUtils.isEmpty(playUrl)) {
            tvDuration.setText("0:00");
            return;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);

            if (playUrl.startsWith("http://") || playUrl.startsWith("https://")) {
                mediaPlayer.setDataSource(playUrl);
            } else {
                mediaPlayer.setDataSource(context, Uri.parse(playUrl));
            }

            mediaPlayer.setOnPreparedListener(mp -> {
                if (currentPlayingId == msgId) {
                    try {
                        int dur = mp.getDuration();
                        audioDurationCache.put(msgId, dur);
                    } catch (Exception ignored) {}
                    mp.start();
                    isAudioPlaying = true;
                    ivPlayPause.setImageResource(R.drawable.ic_voice_pause_rounded);
                    if (waveform != null) waveform.setPlaying(true);
                    startProgressTracker();
                }
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                ivPlayPause.setImageResource(R.drawable.ic_voice_play_rounded);
                if (waveform != null) {
                    waveform.setPlaying(false);
                    waveform.setProgress(0f);
                }
                try {
                    int dur = mp.getDuration();
                    audioDurationCache.put(msgId, dur);
                    tvDuration.setText(formatTime(dur));
                } catch (Exception ignored) {
                    tvDuration.setText("0:00");
                }
                isAudioPlaying = false;
                if (audioProgressRunnable != null) audioHandler.removeCallbacks(audioProgressRunnable);
                currentPlayingId = -1;
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                tvDuration.setText("0:00");
                ivPlayPause.setImageResource(R.drawable.ic_voice_play_rounded);
                if (waveform != null) waveform.setPlaying(false);
                stopAudio();
                return true;
            });

            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            tvDuration.setText("0:00");
            ivPlayPause.setImageResource(R.drawable.ic_voice_play_rounded);
            if (waveform != null) waveform.setPlaying(false);
            stopAudio();
        }
    }

    private void startProgressTracker() {
        if (audioProgressRunnable != null) {
            audioHandler.removeCallbacks(audioProgressRunnable);
        }
        audioProgressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isAudioPlaying && currentPlayingId != -1) {
                    try {
                        int currentPos = mediaPlayer.getCurrentPosition();
                        int duration = mediaPlayer.getDuration();
                        float progress = duration > 0 ? ((float) currentPos / duration) : 0f;

                        int pos = getPositionForMessageId(currentPlayingId);
                        if (pos != -1 && attachedRecyclerView != null) {
                            RecyclerView.ViewHolder holder = attachedRecyclerView.findViewHolderForAdapterPosition(pos);
                            if (holder instanceof RightViewHolder) {
                                RightViewHolder r = (RightViewHolder) holder;
                                if (r.waveformAudio != null) {
                                    r.waveformAudio.setPlaying(true);
                                    r.waveformAudio.setProgress(progress);
                                }
                                if (r.tvAudioDuration != null) r.tvAudioDuration.setText(formatTime(currentPos));
                                if (r.ivAudioPlayPause != null) r.ivAudioPlayPause.setImageResource(R.drawable.ic_voice_pause_rounded);
                            } else if (holder instanceof LeftViewHolder) {
                                LeftViewHolder l = (LeftViewHolder) holder;
                                if (l.waveformAudio != null) {
                                    l.waveformAudio.setPlaying(true);
                                    l.waveformAudio.setProgress(progress);
                                }
                                if (l.tvAudioDuration != null) l.tvAudioDuration.setText(formatTime(currentPos));
                                if (l.ivAudioPlayPause != null) l.ivAudioPlayPause.setImageResource(R.drawable.ic_voice_pause_rounded);
                            }
                        }
                        audioHandler.postDelayed(this, 100);
                    } catch (Exception ignored) {}
                }
            }
        };
        audioHandler.post(audioProgressRunnable);
    }

    private void setupAudioWaveformSeek(AudioWaveformView waveform, long msgId) {
        waveform.setOnSeekListener(progress -> {
            if (currentPlayingId == msgId && mediaPlayer != null) {
                try {
                    int duration = mediaPlayer.getDuration();
                    int targetPos = (int) (progress * duration);
                    mediaPlayer.seekTo(targetPos);
                } catch (Exception ignored) {}
            }
        });
    }

    public void stopAudio() {
        if (audioProgressRunnable != null) {
            audioHandler.removeCallbacks(audioProgressRunnable);
        }
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
        long prevId = currentPlayingId;
        currentPlayingId = -1;
        isAudioPlaying = false;
        if (prevId != -1) {
            int pos = getPositionForMessageId(prevId);
            if (pos != -1) notifyItemChanged(pos);
        }
    }

    public void releaseAudio() {
        stopAudio();
        stopVideo();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        int pos = holder.getAdapterPosition();
        if (pos != RecyclerView.NO_POSITION && pos < list.size()) {
            ChatMessageItem item = list.get(pos);
            if (item != null && currentPlayingVideoId == item.getId()) {
                stopVideo();
            }
        }
    }

    private int getPositionForMessageId(long msgId) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == msgId) {
                return i;
            }
        }
        return -1;
    }

    public String formatMediaUrl(String rawUrl) {
        if (TextUtils.isEmpty(rawUrl)) return "";
        String formatted = rawUrl.trim();
        String activeBaseUrl = go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig.getBaseUrl();
        if (activeBaseUrl.endsWith("/")) activeBaseUrl = activeBaseUrl.substring(0, activeBaseUrl.length() - 1);

        formatted = formatted.replaceAll("https?://(172\\.15\\.[0-9]+\\.[0-9]+|192\\.168\\.[0-9]+\\.[0-9]+|localhost|10\\.0\\.2\\.2)(:[0-9]+)?", activeBaseUrl);
        formatted = formatted.replaceAll("https?://svr-absensi\\.tebingtinggikota\\.go\\.id(:[0-9]+)?", activeBaseUrl);

        if (formatted.startsWith("/")) {
            formatted = activeBaseUrl + formatted;
        } else if (!formatted.startsWith("http://") && !formatted.startsWith("https://") && !formatted.startsWith("file://") && !formatted.startsWith("content://")) {
            formatted = activeBaseUrl + "/" + formatted;
        }
        return formatted.replaceAll("(?<!:)/{2,}", "/");
    }

    private String formatAudioUrl(String rawUrl) {
        return formatMediaUrl(rawUrl);
    }

    private String formatTime(int millis) {
        if (millis < 0) millis = 0;
        int totalSeconds = millis / 1000;
        int seconds = totalSeconds % 60;
        int minutes = totalSeconds / 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    private void loadImageWithFallback(ImageView imageView, String primaryUrl) {
        if (TextUtils.isEmpty(primaryUrl) || imageView == null) return;

        try {
            String activeBaseUrl = go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig.getBaseUrl();
            if (activeBaseUrl.endsWith("/")) activeBaseUrl = activeBaseUrl.substring(0, activeBaseUrl.length() - 1);

            String fallbackUrl = primaryUrl;
            if (primaryUrl.contains(":8443")) {
                fallbackUrl = primaryUrl.replace(":8443", ":8000").replace("https://", "http://");
            } else if (primaryUrl.contains(":8000")) {
                fallbackUrl = primaryUrl.replace(":8000", ":8443").replace("http://", "https://");
            }

            Glide.with(context)
                    .load(primaryUrl)
                    .error(Glide.with(context).load(fallbackUrl).error(R.drawable.ic_image))
                    .placeholder(R.drawable.ic_image)
                    .into(imageView);
        } catch (Exception ignored) {}
    }

    public String getDateLabel(ChatMessageItem item) {
        if (item == null) return "Hari Ini";
        long ts = item.getCreatedAtTs();
        java.util.Calendar msgCal = java.util.Calendar.getInstance();

        if (ts > 0) {
            msgCal.setTimeInMillis(ts * 1000L);
        } else {
            String raw = item.getCreatedAtFormatted();
            if (!TextUtils.isEmpty(raw) && raw.contains(" ")) {
                return raw.split(" ")[0];
            }
            return "Hari Ini";
        }

        java.util.Calendar nowCal = java.util.Calendar.getInstance();

        boolean isToday = nowCal.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR)
                && nowCal.get(java.util.Calendar.DAY_OF_YEAR) == msgCal.get(java.util.Calendar.DAY_OF_YEAR);

        if (isToday) {
            return "Hari Ini";
        }

        nowCal.add(java.util.Calendar.DAY_OF_YEAR, -1);
        boolean isYesterday = nowCal.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR)
                && nowCal.get(java.util.Calendar.DAY_OF_YEAR) == msgCal.get(java.util.Calendar.DAY_OF_YEAR);

        if (isYesterday) {
            return "Kemarin";
        }

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMMM yyyy", new java.util.Locale("id", "ID"));
        return sdf.format(msgCal.getTime());
    }

    public boolean isDifferentDay(ChatMessageItem cur, ChatMessageItem prev) {
        if (cur == null || prev == null) return true;
        long tsCur = cur.getCreatedAtTs();
        long tsPrev = prev.getCreatedAtTs();

        if (tsCur > 0 && tsPrev > 0) {
            java.util.Calendar c1 = java.util.Calendar.getInstance();
            c1.setTimeInMillis(tsCur * 1000L);
            java.util.Calendar c2 = java.util.Calendar.getInstance();
            c2.setTimeInMillis(tsPrev * 1000L);

            return c1.get(java.util.Calendar.YEAR) != c2.get(java.util.Calendar.YEAR)
                    || c1.get(java.util.Calendar.DAY_OF_YEAR) != c2.get(java.util.Calendar.DAY_OF_YEAR);
        }

        String dCur = cur.getCreatedAtFormatted();
        String dPrev = prev.getCreatedAtFormatted();
        if (dCur != null && dPrev != null && dCur.contains(" ") && dPrev.contains(" ")) {
            String dateCur = dCur.split(" ")[0];
            String datePrev = dPrev.split(" ")[0];
            return !dateCur.equals(datePrev);
        }
        return false;
    }

    private void showOptionsDialog(ChatMessageItem item) {
        List<String> options = new ArrayList<>();
        options.add("Balas Pesan");
        options.add("Teruskan Pesan");
        if (!TextUtils.isEmpty(item.getMessage())) {
            options.add("Salin Teks Pesan");
        }

        // Task 1: "menu hapus pesan jangan ada, tetapi edit pesan, kecuali pesan sudah dibaca, tidak dapat di edit"
        if (item.isMe() && !TextUtils.isEmpty(item.getMessage())) {
            if (!item.isRead()) {
                options.add("Edit Pesan");
            }
        }

        String[] optArray = options.toArray(new String[0]);
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Pilihan Pesan")
                .setItems(optArray, (dialog, which) -> {
                    String selected = optArray[which];
                    if ("Balas Pesan".equals(selected)) {
                        if (actionListener != null) actionListener.onReplyMessage(item);
                    } else if ("Teruskan Pesan".equals(selected)) {
                        if (actionListener != null) actionListener.onForwardMessage(item);
                    } else if ("Salin Teks Pesan".equals(selected)) {
                        if (actionListener != null) actionListener.onCopyText(item.getMessage());
                    } else if ("Edit Pesan".equals(selected)) {
                        if (actionListener != null) actionListener.onEditMessage(item);
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class LeftViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llDateHeader;
        TextView tvDateHeader;
        LinearLayout llBubbleContainer;
        TextView tvSenderName, tvMessage, tvTime;
        MaterialCardView cvReplyContainer, cvImageContainer, cvDocumentContainer;
        TextView tvReplySender, tvReplyMessage, tvDocumentName;
        ImageView ivMessageImage, ivDocumentIcon;

        // Video Views
        MaterialCardView cvVideoContainer;
        VideoView vvInlineVideo;
        ImageView ivVideoThumbnail;
        ProgressBar pbVideoBuffering;
        ImageView ivInlineVideoPlayCenter;
        LinearLayout llInlineVideoControls;
        ImageView ivInlineVideoPlayMini;
        SeekBar sbInlineVideoProgress;
        TextView tvInlineVideoDuration;
        ImageView ivInlineVideoFullscreen;

        // Audio Views (Capsule Voice Note)
        LinearLayout llAudioContainer;
        FrameLayout flAudioPlayPause;
        ImageView ivAudioPlayPause;
        AudioWaveformView waveformAudio;
        TextView tvAudioDuration;

        public LeftViewHolder(@NonNull View itemView) {
            super(itemView);
            llDateHeader = itemView.findViewById(R.id.llDateHeader);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
            llBubbleContainer = itemView.findViewById(R.id.llBubbleContainer);
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
            ivDocumentIcon = itemView.findViewById(R.id.ivDocumentIcon);

            cvVideoContainer = itemView.findViewById(R.id.cvVideoContainer);
            vvInlineVideo = itemView.findViewById(R.id.vvInlineVideo);
            ivVideoThumbnail = itemView.findViewById(R.id.ivVideoThumbnail);
            pbVideoBuffering = itemView.findViewById(R.id.pbVideoBuffering);
            ivInlineVideoPlayCenter = itemView.findViewById(R.id.ivInlineVideoPlayCenter);
            llInlineVideoControls = itemView.findViewById(R.id.llInlineVideoControls);
            ivInlineVideoPlayMini = itemView.findViewById(R.id.ivInlineVideoPlayMini);
            sbInlineVideoProgress = itemView.findViewById(R.id.sbInlineVideoProgress);
            tvInlineVideoDuration = itemView.findViewById(R.id.tvInlineVideoDuration);
            ivInlineVideoFullscreen = itemView.findViewById(R.id.ivInlineVideoFullscreen);

            llAudioContainer = itemView.findViewById(R.id.llAudioContainer);
            flAudioPlayPause = itemView.findViewById(R.id.flAudioPlayPause);
            ivAudioPlayPause = itemView.findViewById(R.id.ivAudioPlayPause);
            waveformAudio = itemView.findViewById(R.id.waveformAudio);
            tvAudioDuration = itemView.findViewById(R.id.tvAudioDuration);
        }
    }

    public static class RightViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llDateHeader;
        TextView tvDateHeader;
        LinearLayout llBubbleContainer;
        TextView tvMessage, tvTime, tvStatusCheck;
        LinearLayout llTimeMetadata;
        MaterialCardView cvReplyContainer, cvImageContainer, cvDocumentContainer;
        TextView tvReplySender, tvReplyMessage, tvDocumentName;
        ImageView ivMessageImage, ivDocumentIcon;

        // Video Views
        MaterialCardView cvVideoContainer;
        VideoView vvInlineVideo;
        ImageView ivVideoThumbnail;
        ProgressBar pbVideoBuffering;
        ImageView ivInlineVideoPlayCenter;
        LinearLayout llInlineVideoControls;
        ImageView ivInlineVideoPlayMini;
        SeekBar sbInlineVideoProgress;
        TextView tvInlineVideoDuration;
        ImageView ivInlineVideoFullscreen;

        // Audio Views (Capsule Voice Note)
        LinearLayout llAudioContainer;
        FrameLayout flAudioPlayPause;
        ImageView ivAudioPlayPause;
        AudioWaveformView waveformAudio;
        TextView tvAudioDuration;

        public RightViewHolder(@NonNull View itemView) {
            super(itemView);
            llDateHeader = itemView.findViewById(R.id.llDateHeader);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
            llBubbleContainer = itemView.findViewById(R.id.llBubbleContainer);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatusCheck = itemView.findViewById(R.id.tvStatusCheck);
            llTimeMetadata = itemView.findViewById(R.id.llTimeMetadata);
            cvReplyContainer = itemView.findViewById(R.id.cvReplyContainer);
            tvReplySender = itemView.findViewById(R.id.tvReplySender);
            tvReplyMessage = itemView.findViewById(R.id.tvReplyMessage);
            cvImageContainer = itemView.findViewById(R.id.cvImageContainer);
            ivMessageImage = itemView.findViewById(R.id.ivMessageImage);
            cvDocumentContainer = itemView.findViewById(R.id.cvDocumentContainer);
            tvDocumentName = itemView.findViewById(R.id.tvDocumentName);
            ivDocumentIcon = itemView.findViewById(R.id.ivDocumentIcon);

            cvVideoContainer = itemView.findViewById(R.id.cvVideoContainer);
            vvInlineVideo = itemView.findViewById(R.id.vvInlineVideo);
            ivVideoThumbnail = itemView.findViewById(R.id.ivVideoThumbnail);
            pbVideoBuffering = itemView.findViewById(R.id.pbVideoBuffering);
            ivInlineVideoPlayCenter = itemView.findViewById(R.id.ivInlineVideoPlayCenter);
            llInlineVideoControls = itemView.findViewById(R.id.llInlineVideoControls);
            ivInlineVideoPlayMini = itemView.findViewById(R.id.ivInlineVideoPlayMini);
            sbInlineVideoProgress = itemView.findViewById(R.id.sbInlineVideoProgress);
            tvInlineVideoDuration = itemView.findViewById(R.id.tvInlineVideoDuration);
            ivInlineVideoFullscreen = itemView.findViewById(R.id.ivInlineVideoFullscreen);

            llAudioContainer = itemView.findViewById(R.id.llAudioContainer);
            flAudioPlayPause = itemView.findViewById(R.id.flAudioPlayPause);
            ivAudioPlayPause = itemView.findViewById(R.id.ivAudioPlayPause);
            waveformAudio = itemView.findViewById(R.id.waveformAudio);
            tvAudioDuration = itemView.findViewById(R.id.tvAudioDuration);
        }
    }
}
