package go.pemkott.appsandroidmobiletebingtinggi.chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

    // Real-time polling every 3 seconds
    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            loadMessages(false);
            pollHandler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
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

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getChatMessages(chatType, targetId).enqueue(new Callback<ChatMessageResponse>() {
            @Override
            public void onResponse(Call<ChatMessageResponse> call, Response<ChatMessageResponse> response) {
                pbChatRoom.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<ChatMessageItem> newMsgs = response.body().getMessages();
                    if (newMsgs != null) {
                        // Persist messages to local SQLite
                        chatDb.saveMessages(newMsgs);

                        if (newMsgs.size() != messageList.size()) {
                            messageList = newMsgs;
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

            boolean isImage = mimeType.startsWith("image/") || lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg") || lowerFileName.endsWith(".png") || lowerFileName.endsWith(".webp");

            File file = null;
            String msgTypeStr = "document";

            if (isImage) {
                msgTypeStr = "image";
                if ("application/octet-stream".equals(mimeType)) mimeType = "image/jpeg";
                file = go.pemkott.appsandroidmobiletebingtinggi.utils.ChatMediaCompressor.compressImage(this, uri);
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
        if (TextUtils.isEmpty(url)) return;

        if ("image".equals(type)) {
            showFullImageDialog(url);
        } else {
            openDocumentWithExternalApp(url);
        }
    }

    private void showFullImageDialog(String imageUrl) {
        android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_image_preview);

        ImageView ivFull = dialog.findViewById(R.id.ivFullImagePreview);
        ImageView ivClose = dialog.findViewById(R.id.ivCloseImagePreview);
        ProgressBar pb = dialog.findViewById(R.id.pbImagePreview);

        pb.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(imageUrl)
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

    private void openDocumentWithExternalApp(String documentUrl) {
        if (TextUtils.isEmpty(documentUrl)) return;
        try {
            Uri uri = Uri.parse(documentUrl);
            String lower = documentUrl.toLowerCase();
            String mimeType = "*/*";

            if (lower.endsWith(".pdf")) {
                mimeType = "application/pdf";
            } else if (lower.endsWith(".doc") || lower.endsWith(".docx")) {
                mimeType = "application/msword";
            } else if (lower.endsWith(".xls") || lower.endsWith(".xlsx")) {
                mimeType = "application/vnd.ms-excel";
            } else if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".webp")) {
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
