package go.pemkott.appsandroidmobiletebingtinggi.chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.ChatMessageAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.database.ChatLocalDatabase;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMessageItem;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMessageResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomActivity extends AppCompatActivity implements ChatMessageAdapter.OnMessageActionListener {

    private long targetId;
    private String chatType;
    private String title;
    private String subtitle;
    private String avatarUrl;

    private CircleImageView civRoomAvatar;
    private TextView tvRoomTitle, tvRoomSubtitle;
    private RecyclerView rvMessages;
    private ProgressBar pbChatRoom;
    private EditText etMessageInput;
    private MaterialButton btnSendMessage;
    private ImageView ivAttachFile;

    // Reply Preview UI
    private LinearLayout llReplyPreview;
    private TextView tvReplyPreviewSender, tvReplyPreviewText;
    private ImageView ivCancelReply;

    private Long activeReplyToId = null;

    private ChatMessageAdapter adapter;
    private ChatLocalDatabase chatDb;
    private List<ChatMessageItem> messageList = new ArrayList<>();

    private ActivityResultLauncher<String> filePickerLauncher;

    // Smart Adaptive Polling (every 8 seconds, 0% server overhead with incremental fetch)
    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            loadMessages(false);
            pollHandler.postDelayed(this, 8000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        go.pemkott.appsandroidmobiletebingtinggi.utils.WindowUtil.applyDashboardStyle(this);
        setContentView(R.layout.activity_chat_room);

        chatDb = new ChatLocalDatabase(this);

        targetId = getIntent().getLongExtra("target_id", 0);
        chatType = getIntent().getStringExtra("chat_type");
        title = getIntent().getStringExtra("title");
        subtitle = getIntent().getStringExtra("subtitle");
        avatarUrl = getIntent().getStringExtra("avatar_url");

        if (chatType == null) chatType = "personal";

        ImageView ivBack = findViewById(R.id.ivBackChatRoom);
        ImageView ivRefresh = findViewById(R.id.ivRefreshChatRoom);
        civRoomAvatar = findViewById(R.id.civRoomAvatar);
        tvRoomTitle = findViewById(R.id.tvRoomTitle);
        tvRoomSubtitle = findViewById(R.id.tvRoomSubtitle);
        rvMessages = findViewById(R.id.rvMessages);
        pbChatRoom = findViewById(R.id.pbChatRoom);
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        ivAttachFile = findViewById(R.id.ivAttachFile);

        llReplyPreview = findViewById(R.id.llReplyPreview);
        tvReplyPreviewSender = findViewById(R.id.tvReplyPreviewSender);
        tvReplyPreviewText = findViewById(R.id.tvReplyPreviewText);
        ivCancelReply = findViewById(R.id.ivCancelReply);

        ivBack.setOnClickListener(v -> finish());
        ivRefresh.setOnClickListener(v -> loadMessages(true));
        ivCancelReply.setOnClickListener(v -> cancelReply());
        ivAttachFile.setOnClickListener(v -> showAttachmentOptionsDialog());

        tvRoomTitle.setText(title != null ? title : "Chat");
        tvRoomSubtitle.setText(subtitle != null ? subtitle : "");

        if ("opd".equals(chatType)) {
            civRoomAvatar.setImageResource(R.drawable.ic_locasi_office);
        } else {
            if (!TextUtils.isEmpty(avatarUrl)) {
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_baseline_person_24)
                        .error(R.drawable.ic_baseline_person_24)
                        .into(civRoomAvatar);
            } else {
                civRoomAvatar.setImageResource(R.drawable.ic_baseline_person_24);
            }
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

        adapter = new ChatMessageAdapter(this, messageList, this);
        rvMessages.setAdapter(adapter);

        btnSendMessage.setOnClickListener(v -> sendMessage());

        // Dynamic TextWatcher for EditText & Send Button Activation
        etMessageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s != null && !s.toString().trim().isEmpty();
                btnSendMessage.setEnabled(hasText);
                if (hasText) {
                    btnSendMessage.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#008DDA")));
                } else {
                    btnSendMessage.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CBD5E1")));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Dynamic WindowInsets for Keyboard IME Handling
        View rootLayout = findViewById(R.id.llChatRoomRoot);
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            int navHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            int bottomPadding = Math.max(imeHeight, navHeight);
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottomPadding);

            if (imeHeight > 0 && adapter != null && adapter.getItemCount() > 0) {
                rvMessages.postDelayed(() -> rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1), 100);
            }
            return insets;
        });

        etMessageInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && adapter != null && adapter.getItemCount() > 0) {
                rvMessages.postDelayed(() -> rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1), 200);
            }
        });

        etMessageInput.setOnClickListener(v -> {
            if (adapter != null && adapter.getItemCount() > 0) {
                rvMessages.postDelayed(() -> rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1), 200);
            }
        });

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadAndSendFile(uri);
                    }
                }
        );

        // 1. Load local messages instantly
        loadLocalMessages();

        // 2. Fetch fresh messages from network
        loadMessages(true);
    }

    private void loadLocalMessages() {
        List<ChatMessageItem> localMsgs = chatDb.getMessages(chatType, targetId);
        if (localMsgs != null && !localMsgs.isEmpty()) {
            messageList = localMsgs;
            adapter.setList(messageList);
            rvMessages.scrollToPosition(messageList.size() - 1);
        }
    }

    private void loadMessages(boolean showProgress) {
        if (showProgress && messageList.isEmpty()) {
            pbChatRoom.setVisibility(View.VISIBLE);
        }

        long lastLocalMsgId = chatDb.getLastMessageId(chatType, targetId);

        HttpService api = RetroClient.getInstance(this).getApi2();
        Call<ChatMessageResponse> call;
        if (lastLocalMsgId > 0 && !messageList.isEmpty()) {
            call = api.getChatMessagesIncremental(chatType, targetId, lastLocalMsgId);
        } else {
            call = api.getChatMessages(chatType, targetId);
        }

        call.enqueue(new Callback<ChatMessageResponse>() {
            @Override
            public void onResponse(Call<ChatMessageResponse> call, Response<ChatMessageResponse> response) {
                pbChatRoom.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<ChatMessageItem> newMsgs = response.body().getMessages();
                    if (newMsgs != null && !newMsgs.isEmpty()) {
                        // Persist new messages to local SQLite database
                        chatDb.saveMessages(newMsgs);

                        // Reload entire ordered list from local SQLite
                        List<ChatMessageItem> updatedLocal = chatDb.getMessages(chatType, targetId);
                        if (updatedLocal != null) {
                            messageList = updatedLocal;
                            adapter.setList(messageList);
                            if (!messageList.isEmpty()) {
                                rvMessages.smoothScrollToPosition(messageList.size() - 1);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ChatMessageResponse> call, Throwable t) {
                pbChatRoom.setVisibility(View.GONE);
            }
        });
    }

    private void showAttachmentOptionsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_attachment_picker, null);
        dialog.setContentView(view);

        View cardPhoto = view.findViewById(R.id.cardOptionPhoto);
        View cardDoc = view.findViewById(R.id.cardOptionDoc);

        cardPhoto.setOnClickListener(v -> {
            dialog.dismiss();
            filePickerLauncher.launch("image/*");
        });

        cardDoc.setOnClickListener(v -> {
            dialog.dismiss();
            filePickerLauncher.launch("*/*");
        });

        dialog.show();
    }

    private void uploadAndSendFile(Uri uri) {
        try {
            String mimeType = getContentResolver().getType(uri);
            if (mimeType == null) mimeType = "application/octet-stream";

            String fileName = getFileNameFromUri(uri);
            String lowerFileName = fileName.toLowerCase();

            boolean isImage = mimeType.startsWith("image/") || lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg") || lowerFileName.endsWith(".png") || lowerFileName.endsWith(".webp") || lowerFileName.endsWith(".heic");
            boolean isVideo = mimeType.startsWith("video/") || lowerFileName.endsWith(".mp4") || lowerFileName.endsWith(".mkv") || lowerFileName.endsWith(".mov") || lowerFileName.endsWith(".3gp") || lowerFileName.endsWith(".avi") || lowerFileName.endsWith(".webm");
            boolean isAudio = mimeType.startsWith("audio/") || lowerFileName.endsWith(".mp3") || lowerFileName.endsWith(".wav") || lowerFileName.endsWith(".m4a") || lowerFileName.endsWith(".aac") || lowerFileName.endsWith(".ogg") || lowerFileName.endsWith(".opus") || lowerFileName.endsWith(".amr");

            File file = null;
            String msgTypeStr = "document";

            if (isImage) {
                msgTypeStr = "image";
                if ("application/octet-stream".equals(mimeType)) mimeType = "image/jpeg";
                file = go.pemkott.appsandroidmobiletebingtinggi.utils.ChatMediaCompressor.compressImage(this, uri);
            } else if (isVideo) {
                msgTypeStr = "video";
                file = getFileFromUri(uri);
            } else if (isAudio) {
                msgTypeStr = "audio";
                file = getFileFromUri(uri);
            } else {
                msgTypeStr = "document";
                file = getFileFromUri(uri);
            }

            if (file == null || !file.exists()) {
                file = getFileFromUri(uri);
            }

            if (file == null || !file.exists()) {
                Toast.makeText(this, "Gagal mengambil file lampiran", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSendMessage.setEnabled(false);
            pbChatRoom.setVisibility(View.VISIBLE);

            RequestBody chatTypeBody = RequestBody.create(MediaType.parse("text/plain"), chatType);
            RequestBody targetIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(targetId));
            RequestBody messageBody = RequestBody.create(MediaType.parse("text/plain"), etMessageInput.getText().toString().trim());
            RequestBody msgTypeBody = RequestBody.create(MediaType.parse("text/plain"), msgTypeStr);
            RequestBody replyToBody = RequestBody.create(MediaType.parse("text/plain"), activeReplyToId != null ? String.valueOf(activeReplyToId) : "");

            RequestBody fileReqBody = RequestBody.create(MediaType.parse(mimeType), file);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", getFileNameFromUri(uri), fileReqBody);

            HttpService api = RetroClient.getInstance(this).getApi2();
            api.sendChatMessageMultipart(chatTypeBody, targetIdBody, messageBody, msgTypeBody, replyToBody, filePart).enqueue(new Callback<SendMessageResponse>() {
                @Override
                public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                    btnSendMessage.setEnabled(true);
                    pbChatRoom.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        ChatMessageItem newMsgItem = response.body().getData();
                        if (newMsgItem != null) {
                            chatDb.insertMessage(newMsgItem);
                            adapter.addMessage(newMsgItem);
                            rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                        } else {
                            loadMessages(false);
                        }
                        etMessageInput.setText("");
                        cancelReply();
                    } else {
                        String err = (response.body() != null && response.body().getMessage() != null) ? response.body().getMessage() : "Gagal mengunggah lampiran";
                        go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                                ChatRoomActivity.this, err, go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.ERROR
                        );
                    }
                }

                @Override
                public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                    btnSendMessage.setEnabled(true);
                    pbChatRoom.setVisibility(View.GONE);
                    go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                            ChatRoomActivity.this, "Koneksi gagal: " + t.getMessage(), go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.ERROR
                    );
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error memproses file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File getFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null) return null;
        String fileName = getFileNameFromUri(uri);
        File tempFile = new File(getCacheDir(), fileName != null ? fileName : "upload_temp");
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
        } finally {
            inputStream.close();
        }
        return tempFile;
    }

    private String getFileNameFromUri(Uri uri) {
        if (uri == null) return "file_lampiran";
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) result = cursor.getString(index);
                }
            } catch (Exception ignored) {}
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) result = result.substring(cut + 1);
            }
        }
        if (result == null || result.isEmpty()) {
            result = "lampiran_" + System.currentTimeMillis();
        }

        String mimeType = getContentResolver().getType(uri);
        if (mimeType != null) {
            if (mimeType.startsWith("video/") && !result.toLowerCase().endsWith(".mp4") && !result.toLowerCase().endsWith(".mkv") && !result.toLowerCase().endsWith(".mov") && !result.contains(".")) {
                result += ".mp4";
            } else if (mimeType.startsWith("image/") && !result.toLowerCase().endsWith(".jpg") && !result.toLowerCase().endsWith(".png") && !result.contains(".")) {
                result += ".jpg";
            }
        }
        return result;
    }

    private void sendMessage() {
        String msg = etMessageInput.getText().toString().trim();
        if (TextUtils.isEmpty(msg)) {
            return;
        }

        String replyStr = activeReplyToId != null ? String.valueOf(activeReplyToId) : "";

        etMessageInput.setText("");
        cancelReply();
        btnSendMessage.setEnabled(false);

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.sendChatMessage(chatType, targetId, msg, "text", replyStr).enqueue(new Callback<SendMessageResponse>() {
            @Override
            public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                btnSendMessage.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    ChatMessageItem newMsgItem = response.body().getData();
                    if (newMsgItem != null) {
                        chatDb.insertMessage(newMsgItem);
                        adapter.addMessage(newMsgItem);
                        rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                    } else {
                        loadMessages(false);
                    }
                } else {
                    Toast.makeText(ChatRoomActivity.this, "Gagal mengirim pesan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                btnSendMessage.setEnabled(true);
                Toast.makeText(ChatRoomActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReplyMessage(ChatMessageItem item) {
        if (item == null) return;
        activeReplyToId = item.getId();
        llReplyPreview.setVisibility(View.VISIBLE);
        tvReplyPreviewSender.setText(item.getSenderName());
        tvReplyPreviewText.setText(item.getMessage());
        etMessageInput.requestFocus();
    }

    private void cancelReply() {
        activeReplyToId = null;
        llReplyPreview.setVisibility(View.GONE);
    }

    @Override
    public void onCopyText(String text) {
        if (!TextUtils.isEmpty(text)) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Pesan Chat", text);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Teks pesan disalin", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onViewMedia(String url, String type) {
        onViewMedia(url, type, "");
    }

    @Override
    public void onViewMedia(String url, String type, String fileName) {
        if (TextUtils.isEmpty(url)) return;

        String lower = url.toLowerCase();
        boolean isVideo = "video".equals(type) || lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".mov") || lower.endsWith(".3gp") || lower.endsWith(".avi") || lower.endsWith(".webm");
        boolean isAudio = "audio".equals(type) || lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".m4a") || lower.endsWith(".aac") || lower.endsWith(".ogg") || lower.endsWith(".opus") || lower.endsWith(".amr");
        boolean isImage = "image".equals(type) || lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".webp") || lower.endsWith(".gif") || lower.endsWith(".heic");

        if (isVideo) {
            showVideoPlayerModal(url, fileName);
        } else if (isAudio) {
            showAudioPlayerModal(url, fileName);
        } else if (isImage) {
            showFullImageDialog(url, fileName);
        } else {
            openDocumentWithExternalApp(url);
        }
    }

    private String formatMediaUrl(String mediaUrl) {
        if (TextUtils.isEmpty(mediaUrl)) return "";
        String formatted = mediaUrl.trim();
        if (formatted.contains("172.15.1.35") || formatted.contains("172.15.1.13") || formatted.contains("localhost") || formatted.contains("10.0.2.2")) {
            formatted = formatted.replaceAll("https?://(172\\.15\\.1\\.35|172\\.15\\.1\\.13|localhost|10\\.0\\.2\\.2)(:[0-9]+)?", "http://172.15.1.35:8000");
        } else {
            String activeBaseUrl = go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig.getBaseUrl();
            if (activeBaseUrl.endsWith("/")) activeBaseUrl = activeBaseUrl.substring(0, activeBaseUrl.length() - 1);
            formatted = formatted.replaceAll("https?://svr-absensi\\.tebingtinggikota\\.go\\.id(:[0-9]+)?", activeBaseUrl);
        }

        if (formatted.startsWith("/")) {
            formatted = "http://172.15.1.35:8000" + formatted;
        } else if (!formatted.startsWith("http://") && !formatted.startsWith("https://") && !formatted.startsWith("file://") && !formatted.startsWith("content://")) {
            formatted = "http://172.15.1.35:8000/" + formatted;
        }
        return formatted.replaceAll("(?<!:)/{2,}", "/");
    }

    private String formatTime(int millis) {
        if (millis < 0) millis = 0;
        int seconds = (millis / 1000) % 60;
        int minutes = (millis / (1000 * 60)) % 60;
        int hours = (millis / (1000 * 60 * 60));
        if (hours > 0) {
            return String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }

    private void showFullImageDialog(String imageUrl) {
        showFullImageDialog(imageUrl, "");
    }

    private void showFullImageDialog(String imageUrl, String fileName) {
        if (TextUtils.isEmpty(imageUrl)) return;

        go.pemkott.appsandroidmobiletebingtinggi.api.SSLHelper.disableSSLCertificateChecking();
        String formattedUrl = formatMediaUrl(imageUrl);

        android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_image_preview);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    android.view.WindowManager.LayoutParams.MATCH_PARENT,
                    android.view.WindowManager.LayoutParams.MATCH_PARENT
            );
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(dialog.getWindow(), false);
        }

        ImageView ivFull = dialog.findViewById(R.id.ivFullImagePreview);
        ImageView ivClose = dialog.findViewById(R.id.ivCloseImagePreview);
        ProgressBar pb = dialog.findViewById(R.id.pbImagePreview);
        TextView tvTitle = dialog.findViewById(R.id.tvPreviewTitle);

        if (tvTitle != null && !TextUtils.isEmpty(fileName)) {
            tvTitle.setText(fileName);
        }

        pb.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(formattedUrl)
                .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                    @Override
                    public void onResourceReady(@androidx.annotation.NonNull android.graphics.drawable.Drawable resource, @androidx.annotation.Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.drawable.Drawable> transition) {
                        pb.setVisibility(View.GONE);
                        ivFull.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadCleared(@androidx.annotation.Nullable android.graphics.drawable.Drawable placeholder) {}

                    @Override
                    public void onLoadFailed(@androidx.annotation.Nullable android.graphics.drawable.Drawable errorDrawable) {
                        pb.setVisibility(View.GONE);
                        Toast.makeText(ChatRoomActivity.this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
                    }
                });

        ivClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showVideoPlayerModal(String videoUrl, String fileName) {
        if (TextUtils.isEmpty(videoUrl)) return;

        go.pemkott.appsandroidmobiletebingtinggi.api.SSLHelper.disableSSLCertificateChecking();
        String formattedUrl = formatMediaUrl(videoUrl);

        android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_video_preview);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    android.view.WindowManager.LayoutParams.MATCH_PARENT,
                    android.view.WindowManager.LayoutParams.MATCH_PARENT
            );
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(dialog.getWindow(), false);
        }

        ImageView ivClose = dialog.findViewById(R.id.ivCloseVideoPreview);
        TextView tvTitle = dialog.findViewById(R.id.tvVideoTitle);
        VideoView vvPlayer = dialog.findViewById(R.id.vvVideoPlayer);
        ProgressBar pbLoading = dialog.findViewById(R.id.pbVideoLoading);
        ImageView ivCenterPlayPause = dialog.findViewById(R.id.ivCenterPlayPause);
        SeekBar sbProgress = dialog.findViewById(R.id.sbVideoProgress);
        TextView tvCurrentTime = dialog.findViewById(R.id.tvVideoCurrentTime);
        TextView tvTotalDuration = dialog.findViewById(R.id.tvVideoTotalDuration);
        ImageView ivBtnPlayPause = dialog.findViewById(R.id.ivVideoBtnPlayPause);
        ImageView ivReplay10 = dialog.findViewById(R.id.ivVideoReplay10);
        ImageView ivForward10 = dialog.findViewById(R.id.ivVideoForward10);
        View bottomControls = dialog.findViewById(R.id.llBottomControlVideo);

        if (tvTitle != null) {
            tvTitle.setText(!TextUtils.isEmpty(fileName) ? fileName : "Pemutar Video");
        }

        Handler videoHandler = new Handler(Looper.getMainLooper());
        final boolean[] isUserSeeking = {false};

        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (vvPlayer != null && vvPlayer.isPlaying() && !isUserSeeking[0]) {
                        int currentPos = vvPlayer.getCurrentPosition();
                        sbProgress.setProgress(currentPos);
                        tvCurrentTime.setText(formatTime(currentPos));
                    }
                } catch (Exception ignored) {}
                videoHandler.postDelayed(this, 300);
            }
        };

        vvPlayer.setVideoURI(Uri.parse(formattedUrl));

        vvPlayer.setOnPreparedListener(mp -> {
            pbLoading.setVisibility(View.GONE);
            int duration = vvPlayer.getDuration();
            sbProgress.setMax(duration);
            tvTotalDuration.setText(formatTime(duration));
            vvPlayer.start();
            ivBtnPlayPause.setImageResource(R.drawable.ic_pause);
            ivCenterPlayPause.setVisibility(View.GONE);
            videoHandler.post(progressRunnable);
        });

        vvPlayer.setOnCompletionListener(mp -> {
            ivBtnPlayPause.setImageResource(R.drawable.ic_play_arrow);
            ivCenterPlayPause.setImageResource(R.drawable.ic_play_arrow);
            ivCenterPlayPause.setVisibility(View.VISIBLE);
            sbProgress.setProgress(vvPlayer.getDuration());
            tvCurrentTime.setText(formatTime(vvPlayer.getDuration()));
        });

        vvPlayer.setOnErrorListener((mp, what, extra) -> {
            pbLoading.setVisibility(View.GONE);
            go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                    ChatRoomActivity.this, "Gagal memutar video dari server", go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.ERROR
            );
            return true;
        });

        View.OnClickListener togglePlayPause = v -> {
            try {
                if (vvPlayer.isPlaying()) {
                    vvPlayer.pause();
                    ivBtnPlayPause.setImageResource(R.drawable.ic_play_arrow);
                    ivCenterPlayPause.setImageResource(R.drawable.ic_play_arrow);
                    ivCenterPlayPause.setVisibility(View.VISIBLE);
                } else {
                    vvPlayer.start();
                    ivBtnPlayPause.setImageResource(R.drawable.ic_pause);
                    ivCenterPlayPause.setVisibility(View.GONE);
                    videoHandler.post(progressRunnable);
                }
            } catch (Exception ignored) {}
        };

        ivBtnPlayPause.setOnClickListener(togglePlayPause);
        ivCenterPlayPause.setOnClickListener(togglePlayPause);

        vvPlayer.setOnClickListener(v -> {
            if (bottomControls.getVisibility() == View.VISIBLE) {
                bottomControls.setVisibility(View.GONE);
            } else {
                bottomControls.setVisibility(View.VISIBLE);
            }
        });

        ivReplay10.setOnClickListener(v -> {
            try {
                int newPos = Math.max(0, vvPlayer.getCurrentPosition() - 10000);
                vvPlayer.seekTo(newPos);
                sbProgress.setProgress(newPos);
                tvCurrentTime.setText(formatTime(newPos));
            } catch (Exception ignored) {}
        });

        ivForward10.setOnClickListener(v -> {
            try {
                int newPos = Math.min(vvPlayer.getDuration(), vvPlayer.getCurrentPosition() + 10000);
                vvPlayer.seekTo(newPos);
                sbProgress.setProgress(newPos);
                tvCurrentTime.setText(formatTime(newPos));
            } catch (Exception ignored) {}
        });

        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking[0] = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking[0] = false;
                try {
                    vvPlayer.seekTo(seekBar.getProgress());
                } catch (Exception ignored) {}
            }
        });

        ivClose.setOnClickListener(v -> dialog.dismiss());

        dialog.setOnDismissListener(d -> {
            videoHandler.removeCallbacks(progressRunnable);
            try {
                if (vvPlayer.isPlaying()) {
                    vvPlayer.stopPlayback();
                }
            } catch (Exception ignored) {}
        });

        dialog.show();
    }

    private void showAudioPlayerModal(String audioUrl, String fileName) {
        if (TextUtils.isEmpty(audioUrl)) return;

        go.pemkott.appsandroidmobiletebingtinggi.api.SSLHelper.disableSSLCertificateChecking();
        String formattedUrl = formatMediaUrl(audioUrl);

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_audio_preview, null);
        dialog.setContentView(view);

        ImageView ivClose = view.findViewById(R.id.ivCloseAudioPreview);
        TextView tvFileName = view.findViewById(R.id.tvAudioFileName);
        TextView tvStatus = view.findViewById(R.id.tvAudioStatusSubtitle);
        ProgressBar pbLoading = view.findViewById(R.id.pbAudioLoading);
        SeekBar sbProgress = view.findViewById(R.id.sbAudioProgress);
        TextView tvCurrentTime = view.findViewById(R.id.tvAudioCurrentTime);
        TextView tvTotalDuration = view.findViewById(R.id.tvAudioTotalDuration);
        com.google.android.material.floatingactionbutton.FloatingActionButton fabPlayPause = view.findViewById(R.id.fabAudioPlayPause);
        ImageView ivReplay10 = view.findViewById(R.id.ivAudioReplay10);
        ImageView ivForward10 = view.findViewById(R.id.ivAudioForward10);

        tvFileName.setText(!TextUtils.isEmpty(fileName) ? fileName : "Berkas Audio");
        tvStatus.setText("Menghubungkan ke server...");

        final android.media.MediaPlayer mediaPlayer = new android.media.MediaPlayer();
        final Handler audioHandler = new Handler(Looper.getMainLooper());
        final boolean[] isUserSeeking = {false};

        Runnable updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (mediaPlayer.isPlaying() && !isUserSeeking[0]) {
                        int pos = mediaPlayer.getCurrentPosition();
                        sbProgress.setProgress(pos);
                        tvCurrentTime.setText(formatTime(pos));
                    }
                } catch (Exception ignored) {}
                audioHandler.postDelayed(this, 200);
            }
        };

        try {
            mediaPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(this, Uri.parse(formattedUrl));
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            pbLoading.setVisibility(View.GONE);
            tvStatus.setText("Gagal memuat sumber audio");
            Toast.makeText(this, "Gagal memutar audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        mediaPlayer.setOnPreparedListener(mp -> {
            pbLoading.setVisibility(View.GONE);
            tvStatus.setText("Sedang memutar");
            int dur = mp.getDuration();
            sbProgress.setMax(dur);
            tvTotalDuration.setText(formatTime(dur));
            mp.start();
            fabPlayPause.setImageResource(R.drawable.ic_pause);
            audioHandler.post(updateProgressRunnable);
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            fabPlayPause.setImageResource(R.drawable.ic_play_arrow);
            tvStatus.setText("Selesai diputar");
            sbProgress.setProgress(mediaPlayer.getDuration());
            tvCurrentTime.setText(formatTime(mediaPlayer.getDuration()));
        });

        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            pbLoading.setVisibility(View.GONE);
            tvStatus.setText("Terjadi kesalahan pemutaran");
            Toast.makeText(ChatRoomActivity.this, "Format audio tidak didukung atau server tidak merespons", Toast.LENGTH_SHORT).show();
            return true;
        });

        fabPlayPause.setOnClickListener(v -> {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    fabPlayPause.setImageResource(R.drawable.ic_play_arrow);
                    tvStatus.setText("Jeda");
                } else {
                    mediaPlayer.start();
                    fabPlayPause.setImageResource(R.drawable.ic_pause);
                    tvStatus.setText("Sedang memutar");
                    audioHandler.post(updateProgressRunnable);
                }
            } catch (Exception e) {
                Toast.makeText(ChatRoomActivity.this, "Audio belum siap", Toast.LENGTH_SHORT).show();
            }
        });

        ivReplay10.setOnClickListener(v -> {
            try {
                int newPos = Math.max(0, mediaPlayer.getCurrentPosition() - 10000);
                mediaPlayer.seekTo(newPos);
                sbProgress.setProgress(newPos);
                tvCurrentTime.setText(formatTime(newPos));
            } catch (Exception ignored) {}
        });

        ivForward10.setOnClickListener(v -> {
            try {
                int newPos = Math.min(mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition() + 10000);
                mediaPlayer.seekTo(newPos);
                sbProgress.setProgress(newPos);
                tvCurrentTime.setText(formatTime(newPos));
            } catch (Exception ignored) {}
        });

        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking[0] = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking[0] = false;
                try {
                    mediaPlayer.seekTo(seekBar.getProgress());
                } catch (Exception ignored) {}
            }
        });

        ivClose.setOnClickListener(v -> dialog.dismiss());

        dialog.setOnDismissListener(d -> {
            audioHandler.removeCallbacks(updateProgressRunnable);
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception ignored) {}
        });

        dialog.show();
    }

    private void openDocumentWithExternalApp(String documentUrl) {
        if (TextUtils.isEmpty(documentUrl)) return;
        try {
            String lower = documentUrl.toLowerCase();

            // Direct In-App Built-In PDF Viewer
            if (lower.endsWith(".pdf") || lower.contains(".pdf")) {
                Intent intent = new Intent(this, go.pemkott.appsandroidmobiletebingtinggi.pdf.ReadPdfActivity.class);
                intent.putExtra("PDF_URL", documentUrl);
                startActivity(intent);
                return;
            }

            Uri uri = Uri.parse(documentUrl);
            String mimeType = "*/*";

            if (lower.endsWith(".pdf")) {
                mimeType = "application/pdf";
            } else if (lower.endsWith(".doc") || lower.endsWith(".docx")) {
                mimeType = "application/msword";
            } else if (lower.endsWith(".xls") || lower.endsWith(".xlsx")) {
                mimeType = "application/vnd.ms-excel";
            } else if (lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".m4a") || lower.endsWith(".aac") || lower.endsWith(".ogg") || lower.endsWith(".opus") || lower.endsWith(".amr")) {
                mimeType = "audio/*";
            } else if (lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".mov") || lower.endsWith(".3gp") || lower.endsWith(".avi") || lower.endsWith(".webm")) {
                mimeType = "video/*";
            } else if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".webp") || lower.endsWith(".heic")) {
                mimeType = "image/*";
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(intent, "Buka Dokumen Kedinasan Dengan..."));
            } else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(browserIntent);
            }
        } catch (Exception e) {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(documentUrl));
                startActivity(browserIntent);
            } catch (Exception ex) {
                go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                        this, "Tidak ada aplikasi pembuka dokumen yang cocok", go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.ERROR
                );
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        pollHandler.postDelayed(pollRunnable, 3000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pollHandler.removeCallbacks(pollRunnable);
    }
}
