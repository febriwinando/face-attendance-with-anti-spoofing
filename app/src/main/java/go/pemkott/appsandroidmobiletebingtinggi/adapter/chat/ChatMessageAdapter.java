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
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMessageItem;

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

            // Image / Video attachment (Excluded if Audio)
            if (!isAudio && (isImage || isVideo) && !TextUtils.isEmpty(item.getFileUrl())) {
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

            // Inline Telegram Audio Player
            if (isAudio) {
                r.cvAudioContainer.setVisibility(View.VISIBLE);
                r.cvDocumentContainer.setVisibility(View.GONE);

                boolean isThisPlaying = (currentPlayingId == item.getId() && isAudioPlaying);
                boolean isThisPaused = (currentPlayingId == item.getId() && !isAudioPlaying && mediaPlayer != null);

                if (isThisPlaying) {
                    r.ivAudioPlayPause.setImageResource(R.drawable.ic_pause);
                    try {
                        int cur = mediaPlayer.getCurrentPosition();
                        int dur = mediaPlayer.getDuration();
                        r.sbAudioProgress.setProgress(dur > 0 ? (int) (((float) cur / dur) * 100) : 0);
                        r.tvAudioDuration.setText(formatTime(cur) + " / " + formatTime(dur));
                    } catch (Exception e) {
                        r.tvAudioDuration.setText("00:00");
                    }
                } else if (isThisPaused) {
                    r.ivAudioPlayPause.setImageResource(R.drawable.ic_play_arrow);
                    try {
                        int cur = mediaPlayer.getCurrentPosition();
                        int dur = mediaPlayer.getDuration();
                        r.sbAudioProgress.setProgress(dur > 0 ? (int) (((float) cur / dur) * 100) : 0);
                        r.tvAudioDuration.setText(formatTime(cur) + " / " + formatTime(dur));
                    } catch (Exception e) {
                        r.tvAudioDuration.setText("00:00");
                    }
                } else {
                    r.ivAudioPlayPause.setImageResource(R.drawable.ic_play_arrow);
                    r.sbAudioProgress.setProgress(0);
                    r.tvAudioDuration.setText("00:00");
                }

                r.ivAudioPlayPause.setOnClickListener(v -> handleAudioToggle(item, r.ivAudioPlayPause, r.sbAudioProgress, r.tvAudioDuration));
                setupAudioSeekBar(r.sbAudioProgress, item.getId());

            } else {
                r.cvAudioContainer.setVisibility(View.GONE);

                // Document attachment (Non-audio, non-image, non-video)
                if (!TextUtils.isEmpty(item.getFileUrl()) && !isImage && !isVideo) {
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

            // Adjust padding & background for image-only messages and search highlight
            boolean isMediaOnly = !isAudio && (isImage || isVideo) && TextUtils.isEmpty(item.getMessage());
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

            // Image / Video attachment (Excluded if Audio)
            if (!isAudioLeft && (isImageLeft || isVideoLeft) && !TextUtils.isEmpty(item.getFileUrl())) {
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

            // Inline Telegram Audio Player
            if (isAudioLeft) {
                l.cvAudioContainer.setVisibility(View.VISIBLE);
                l.cvDocumentContainer.setVisibility(View.GONE);

                boolean isThisPlaying = (currentPlayingId == item.getId() && isAudioPlaying);
                boolean isThisPaused = (currentPlayingId == item.getId() && !isAudioPlaying && mediaPlayer != null);

                if (isThisPlaying) {
                    l.ivAudioPlayPause.setImageResource(R.drawable.ic_pause);
                    try {
                        int cur = mediaPlayer.getCurrentPosition();
                        int dur = mediaPlayer.getDuration();
                        l.sbAudioProgress.setProgress(dur > 0 ? (int) (((float) cur / dur) * 100) : 0);
                        l.tvAudioDuration.setText(formatTime(cur) + " / " + formatTime(dur));
                    } catch (Exception e) {
                        l.tvAudioDuration.setText("00:00");
                    }
                } else if (isThisPaused) {
                    l.ivAudioPlayPause.setImageResource(R.drawable.ic_play_arrow);
                    try {
                        int cur = mediaPlayer.getCurrentPosition();
                        int dur = mediaPlayer.getDuration();
                        l.sbAudioProgress.setProgress(dur > 0 ? (int) (((float) cur / dur) * 100) : 0);
                        l.tvAudioDuration.setText(formatTime(cur) + " / " + formatTime(dur));
                    } catch (Exception e) {
                        l.tvAudioDuration.setText("00:00");
                    }
                } else {
                    l.ivAudioPlayPause.setImageResource(R.drawable.ic_play_arrow);
                    l.sbAudioProgress.setProgress(0);
                    l.tvAudioDuration.setText("00:00");
                }

                l.ivAudioPlayPause.setOnClickListener(v -> handleAudioToggle(item, l.ivAudioPlayPause, l.sbAudioProgress, l.tvAudioDuration));
                setupAudioSeekBar(l.sbAudioProgress, item.getId());

            } else {
                l.cvAudioContainer.setVisibility(View.GONE);

                // Document attachment (Non-audio, non-image, non-video)
                if (!TextUtils.isEmpty(item.getFileUrl()) && !isImageLeft && !isVideoLeft) {
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
            }

            l.tvTime.setText(item.getCreatedAtFormatted());

            // Adjust padding & background for image-only messages and search highlight
            boolean isMediaOnlyLeft = !isAudioLeft && (isImageLeft || isVideoLeft) && TextUtils.isEmpty(item.getMessage());
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

            // Long Press Options
            l.itemView.setOnLongClickListener(v -> {
                showOptionsDialog(item);
                return true;
            });
        }
    }

    private void handleAudioToggle(ChatMessageItem item, ImageView ivPlayPause, SeekBar sbProgress, TextView tvDuration) {
        long msgId = item.getId();
        if (currentPlayingId == msgId && mediaPlayer != null) {
            // Same message: toggle play/pause
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    isAudioPlaying = false;
                    ivPlayPause.setImageResource(R.drawable.ic_play_arrow);
                    if (audioProgressRunnable != null) audioHandler.removeCallbacks(audioProgressRunnable);
                } else {
                    mediaPlayer.start();
                    isAudioPlaying = true;
                    ivPlayPause.setImageResource(R.drawable.ic_pause);
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
        ivPlayPause.setImageResource(R.drawable.ic_play_arrow);
        sbProgress.setProgress(0);
        tvDuration.setText("Memuat...");

        String playUrl = formatAudioUrl(item.getFileUrl());
        if (TextUtils.isEmpty(playUrl)) {
            tvDuration.setText("Gagal: URL kosong");
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
                    mp.start();
                    isAudioPlaying = true;
                    ivPlayPause.setImageResource(R.drawable.ic_pause);
                    startProgressTracker();
                }
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                ivPlayPause.setImageResource(R.drawable.ic_play_arrow);
                sbProgress.setProgress(0);
                try {
                    tvDuration.setText("00:00 / " + formatTime(mp.getDuration()));
                } catch (Exception ignored) {
                    tvDuration.setText("00:00");
                }
                isAudioPlaying = false;
                if (audioProgressRunnable != null) audioHandler.removeCallbacks(audioProgressRunnable);
                currentPlayingId = -1;
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                tvDuration.setText("Gagal memutar");
                ivPlayPause.setImageResource(R.drawable.ic_play_arrow);
                stopAudio();
                return true;
            });

            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            tvDuration.setText("Gagal memutar");
            ivPlayPause.setImageResource(R.drawable.ic_play_arrow);
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
                        int progress = duration > 0 ? (int) (((float) currentPos / duration) * 100) : 0;

                        int pos = getPositionForMessageId(currentPlayingId);
                        if (pos != -1 && attachedRecyclerView != null) {
                            RecyclerView.ViewHolder holder = attachedRecyclerView.findViewHolderForAdapterPosition(pos);
                            if (holder instanceof RightViewHolder) {
                                RightViewHolder r = (RightViewHolder) holder;
                                r.sbAudioProgress.setProgress(progress);
                                r.tvAudioDuration.setText(formatTime(currentPos) + " / " + formatTime(duration));
                                r.ivAudioPlayPause.setImageResource(R.drawable.ic_pause);
                            } else if (holder instanceof LeftViewHolder) {
                                LeftViewHolder l = (LeftViewHolder) holder;
                                l.sbAudioProgress.setProgress(progress);
                                l.tvAudioDuration.setText(formatTime(currentPos) + " / " + formatTime(duration));
                                l.ivAudioPlayPause.setImageResource(R.drawable.ic_pause);
                            }
                        }
                        audioHandler.postDelayed(this, 150);
                    } catch (Exception ignored) {}
                }
            }
        };
        audioHandler.post(audioProgressRunnable);
    }

    private void setupAudioSeekBar(SeekBar sbProgress, long msgId) {
        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && currentPlayingId == msgId && mediaPlayer != null) {
                    try {
                        int duration = mediaPlayer.getDuration();
                        int targetPos = (int) (((float) progress / 100) * duration);
                        mediaPlayer.seekTo(targetPos);
                    } catch (Exception ignored) {}
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
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
    }

    private int getPositionForMessageId(long msgId) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == msgId) {
                return i;
            }
        }
        return -1;
    }

    private String formatAudioUrl(String rawUrl) {
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

    private String formatTime(int millis) {
        if (millis < 0) millis = 0;
        int seconds = (millis / 1000) % 60;
        int minutes = (millis / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
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
        MaterialCardView cvReplyContainer, cvImageContainer, cvDocumentContainer, cvAudioContainer;
        TextView tvReplySender, tvReplyMessage, tvDocumentName, tvAudioDuration;
        ImageView ivMessageImage, ivDocumentIcon, ivAudioPlayPause;
        SeekBar sbAudioProgress;
        FrameLayout flVideoOverlay;

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
            flVideoOverlay = itemView.findViewById(R.id.flVideoOverlay);
            cvDocumentContainer = itemView.findViewById(R.id.cvDocumentContainer);
            tvDocumentName = itemView.findViewById(R.id.tvDocumentName);
            ivDocumentIcon = itemView.findViewById(R.id.ivDocumentIcon);
            cvAudioContainer = itemView.findViewById(R.id.cvAudioContainer);
            ivAudioPlayPause = itemView.findViewById(R.id.ivAudioPlayPause);
            sbAudioProgress = itemView.findViewById(R.id.sbAudioProgress);
            tvAudioDuration = itemView.findViewById(R.id.tvAudioDuration);
        }
    }

    public static class RightViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llDateHeader;
        TextView tvDateHeader;
        LinearLayout llBubbleContainer;
        TextView tvMessage, tvTime, tvStatusCheck;
        MaterialCardView cvReplyContainer, cvImageContainer, cvDocumentContainer, cvAudioContainer;
        TextView tvReplySender, tvReplyMessage, tvDocumentName, tvAudioDuration;
        ImageView ivMessageImage, ivDocumentIcon, ivAudioPlayPause;
        SeekBar sbAudioProgress;
        FrameLayout flVideoOverlay;

        public RightViewHolder(@NonNull View itemView) {
            super(itemView);
            llDateHeader = itemView.findViewById(R.id.llDateHeader);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
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
            cvAudioContainer = itemView.findViewById(R.id.cvAudioContainer);
            ivAudioPlayPause = itemView.findViewById(R.id.ivAudioPlayPause);
            sbAudioProgress = itemView.findViewById(R.id.sbAudioProgress);
            tvAudioDuration = itemView.findViewById(R.id.tvAudioDuration);
        }
    }
}
