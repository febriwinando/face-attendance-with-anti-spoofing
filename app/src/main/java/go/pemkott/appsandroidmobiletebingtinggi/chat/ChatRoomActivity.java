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

import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.ChatContactAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.OpdMemberAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatContact;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatContactResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.OpdGroupProfileResponse;

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
    private ImageView ivDirectMic;

    // Search UI (Telegram Style)
    private LinearLayout llChatSearchBar;
    private EditText etSearchInChat;
    private TextView tvSearchMatchCount;
    private ImageView ivSearchPrev, ivSearchNext, ivCloseChatSearch;
    private final List<Integer> searchMatchPositions = new ArrayList<>();
    private int currentSearchIndex = -1;

    // Edit Message UI
    private LinearLayout llEditPreview;
    private TextView tvEditPreviewText;
    private ImageView ivCancelEdit;
    private ChatMessageItem editingMessage = null;

    // Reply Preview UI
    private LinearLayout llReplyPreview;
    private TextView tvReplyPreviewSender, tvReplyPreviewText;
    private ImageView ivCancelReply;

    private Long activeReplyToId = null;

    private ChatMessageAdapter adapter;
    private ChatLocalDatabase chatDb;
    private List<ChatMessageItem> messageList = new ArrayList<>();

    private ActivityResultLauncher<String> filePickerLauncher;
    private Uri cameraPhotoUri;
    private ActivityResultLauncher<Uri> cameraLauncher;

    // Smart Adaptive Polling (every 8 seconds, 0% server overhead with incremental fetch)
    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private boolean isContactBlocked = false;
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
        isContactBlocked = getIntent().getBooleanExtra("is_blocked", false);

        if (chatType == null) chatType = "personal";

        // Tandai pesan sudah dibaca dan reset unread count di database lokal saat obrolan dibuka
        chatDb.markMessagesAsRead(chatType, targetId);
        chatDb.resetUnreadCount(chatType, targetId);

        ImageView ivBack = findViewById(R.id.ivBackChatRoom);
        ImageView ivRefresh = findViewById(R.id.ivRefreshChatRoom);
        ImageView ivMoreOptions = findViewById(R.id.ivMoreOptionsChatRoom);
        civRoomAvatar = findViewById(R.id.civRoomAvatar);
        tvRoomTitle = findViewById(R.id.tvRoomTitle);
        tvRoomSubtitle = findViewById(R.id.tvRoomSubtitle);
        rvMessages = findViewById(R.id.rvMessages);
        pbChatRoom = findViewById(R.id.pbChatRoom);
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        ivAttachFile = findViewById(R.id.ivAttachFile);
        ivDirectMic = findViewById(R.id.ivDirectMic);

        llChatSearchBar = findViewById(R.id.llChatSearchBar);
        etSearchInChat = findViewById(R.id.etSearchInChat);
        tvSearchMatchCount = findViewById(R.id.tvSearchMatchCount);
        ivSearchPrev = findViewById(R.id.ivSearchPrev);
        ivSearchNext = findViewById(R.id.ivSearchNext);
        ivCloseChatSearch = findViewById(R.id.ivCloseChatSearch);

        llReplyPreview = findViewById(R.id.llReplyPreview);
        tvReplyPreviewSender = findViewById(R.id.tvReplyPreviewSender);
        tvReplyPreviewText = findViewById(R.id.tvReplyPreviewText);
        ivCancelReply = findViewById(R.id.ivCancelReply);

        llEditPreview = findViewById(R.id.llEditPreview);
        tvEditPreviewText = findViewById(R.id.tvEditPreviewText);
        ivCancelEdit = findViewById(R.id.ivCancelEdit);

        ivBack.setOnClickListener(v -> finish());
        ivRefresh.setOnClickListener(v -> loadMessages(true));
        if (ivMoreOptions != null) {
            ivMoreOptions.setOnClickListener(this::showMoreOptionsMenu);
        }
        if (ivCloseChatSearch != null) {
            ivCloseChatSearch.setOnClickListener(v -> closeChatSearch());
        }
        if (ivSearchPrev != null) {
            ivSearchPrev.setOnClickListener(v -> navigateSearchPrev());
        }
        if (ivSearchNext != null) {
            ivSearchNext.setOnClickListener(v -> navigateSearchNext());
        }
        if (etSearchInChat != null) {
            etSearchInChat.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    performInChatSearch(s != null ? s.toString() : "");
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
        ivCancelReply.setOnClickListener(v -> cancelReply());
        if (ivCancelEdit != null) {
            ivCancelEdit.setOnClickListener(v -> cancelEdit());
        }
        ivAttachFile.setOnClickListener(v -> showAttachmentOptionsDialog());
        if (ivDirectMic != null) {
            ivDirectMic.setOnClickListener(v -> checkAndShowVoiceRecorder());
        }

        ImageView ivDirectCamera = findViewById(R.id.ivDirectCamera);
        if (ivDirectCamera != null) {
            ivDirectCamera.setOnClickListener(v -> launchCameraCapture());
        }

        ImageView ivCallAction = findViewById(R.id.ivCallAction);
        if (ivCallAction != null) {
            ivCallAction.setOnClickListener(v -> {
                Toast.makeText(this, "Panggilan kontak: " + (title != null ? title : ""), Toast.LENGTH_SHORT).show();
            });
        }

        tvRoomTitle.setText(title != null ? title : "Chat");
        tvRoomSubtitle.setText(subtitle != null ? subtitle : "");

        if ("opd".equals(chatType)) {
            civRoomAvatar.setImageResource(R.drawable.ic_locasi_office);
            View.OnClickListener headerClickListener = v -> showOpdGroupProfileDialog();
            civRoomAvatar.setOnClickListener(headerClickListener);
            tvRoomTitle.setOnClickListener(headerClickListener);
            tvRoomSubtitle.setOnClickListener(headerClickListener);
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
                    btnSendMessage.setVisibility(View.VISIBLE);
                    btnSendMessage.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#16A34A")));
                } else {
                    btnSendMessage.setVisibility(View.GONE);
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

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && cameraPhotoUri != null) {
                        uploadAndSendFile(cameraPhotoUri);
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
                    if (newMsgs != null && !newMsgs.isEmpty()) {
                        // Persist new messages and updated is_read / edit status to local SQLite database
                        chatDb.saveMessages(newMsgs);
                        chatDb.markMessagesAsRead(chatType, targetId);
                        chatDb.resetUnreadCount(chatType, targetId);

                        // Reload entire ordered list from local SQLite
                        List<ChatMessageItem> updatedLocal = chatDb.getMessages(chatType, targetId);
                        if (updatedLocal != null) {
                            boolean wasAtBottom = isLastItemVisible();
                            int oldSize = messageList.size();
                            messageList = updatedLocal;
                            adapter.setList(messageList);
                            if (showProgress || wasAtBottom || updatedLocal.size() > oldSize) {
                                rvMessages.scrollToPosition(messageList.size() - 1);
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

    private boolean isLastItemVisible() {
        if (rvMessages == null || rvMessages.getLayoutManager() == null || adapter == null || adapter.getItemCount() == 0) {
            return true;
        }
        LinearLayoutManager lm = (LinearLayoutManager) rvMessages.getLayoutManager();
        int lastVisible = lm.findLastVisibleItemPosition();
        return lastVisible >= adapter.getItemCount() - 2;
    }

    private void showOpdGroupProfileDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_group_opd_profile, null);
        dialog.setContentView(view);

        dialog.setOnShowListener(d -> {
            try {
                BottomSheetDialog bsd = (BottomSheetDialog) d;
                android.widget.FrameLayout bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    com.google.android.material.bottomsheet.BottomSheetBehavior<android.widget.FrameLayout> behavior =
                            com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
                    behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
                    behavior.setSkipCollapsed(true);
                }
            } catch (Exception ignored) {}
        });

        ImageView ivClose = view.findViewById(R.id.ivCloseGroupProfile);
        CircleImageView civAvatar = view.findViewById(R.id.civGroupProfileAvatar);
        TextView tvName = view.findViewById(R.id.tvGroupProfileName);
        TextView tvSubtitle = view.findViewById(R.id.tvGroupProfileSubtitle);
        TextView tvDesc = view.findViewById(R.id.tvGroupProfileDesc);
        EditText etSearch = view.findViewById(R.id.etSearchGroupMembers);
        TextView tvMemberHeader = view.findViewById(R.id.tvGroupMemberHeader);
        RecyclerView rv = view.findViewById(R.id.rvGroupMembers);
        ProgressBar pb = view.findViewById(R.id.pbLoadingGroupMembers);
        TextView tvEmpty = view.findViewById(R.id.tvEmptyGroupMembers);

        if (ivClose != null) ivClose.setOnClickListener(v -> dialog.dismiss());
        if (tvName != null) tvName.setText(title != null ? title : "Grup OPD");
        if (tvSubtitle != null) tvSubtitle.setText("Grup Resmi Kedinasan OPD");

        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(this));
        }

        if (pb != null) pb.setVisibility(View.VISIBLE);

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getOpdGroupProfile(targetId).enqueue(new Callback<OpdGroupProfileResponse>() {
            @Override
            public void onResponse(Call<OpdGroupProfileResponse> call, Response<OpdGroupProfileResponse> response) {
                if (pb != null) pb.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus() && response.body().getData() != null) {
                    OpdGroupProfileResponse.OpdGroupData data = response.body().getData();

                    if (tvName != null && !TextUtils.isEmpty(data.getOpdName())) {
                        tvName.setText("Grup OPD - " + data.getOpdName());
                    }
                    if (tvSubtitle != null) {
                        tvSubtitle.setText("Grup Resmi OPD (" + data.getMemberCount() + " Pegawai)");
                    }
                    if (tvDesc != null && !TextUtils.isEmpty(data.getDescription())) {
                        tvDesc.setText(data.getDescription());
                    }
                    if (tvMemberHeader != null) {
                        tvMemberHeader.setText("DAFTAR ANGGOTA (" + data.getMemberCount() + " PEGAWAI)");
                    }

                    List<OpdGroupProfileResponse.OpdMemberItem> members = data.getMembers();
                    if (members != null && !members.isEmpty()) {
                        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
                        OpdMemberAdapter memberAdapter = new OpdMemberAdapter(ChatRoomActivity.this, members, member -> {
                            dialog.dismiss();
                            // Buka obrolan 1-on-1 personal dengan rekan OPD
                            Intent intent = new Intent(ChatRoomActivity.this, ChatRoomActivity.class);
                            intent.putExtra("target_id", member.getId());
                            intent.putExtra("chat_type", "personal");
                            intent.putExtra("title", member.getNama());
                            intent.putExtra("subtitle", member.getJabatan() != null ? member.getJabatan() : (data.getOpdName() != null ? data.getOpdName() : "Pegawai"));
                            intent.putExtra("avatar_url", member.getAvatarUrl());
                            startActivity(intent);
                        });
                        if (rv != null) {
                            rv.setAdapter(memberAdapter);
                        }

                        if (etSearch != null) {
                            etSearch.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    memberAdapter.filter(s != null ? s.toString() : "");
                                }

                                @Override
                                public void afterTextChanged(Editable s) {}
                            });
                        }
                    } else {
                        if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (tvEmpty != null) {
                        tvEmpty.setText("Gagal memuat daftar anggota OPD");
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<OpdGroupProfileResponse> call, Throwable t) {
                if (pb != null) pb.setVisibility(View.GONE);
                if (tvEmpty != null) {
                    tvEmpty.setText("Koneksi gagal: " + t.getMessage());
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }
        });

        dialog.show();
    }

    private void showAttachmentOptionsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_attachment_picker, null);
        dialog.setContentView(view);

        View cardCamera = view.findViewById(R.id.cardOptionCamera);
        View cardPhoto = view.findViewById(R.id.cardOptionPhoto);
        View cardAudio = view.findViewById(R.id.cardOptionAudio);
        View cardDoc = view.findViewById(R.id.cardOptionDoc);

        if (cardCamera != null) {
            cardCamera.setOnClickListener(v -> {
                dialog.dismiss();
                launchCameraCapture();
            });
        }

        if (cardPhoto != null) {
            cardPhoto.setOnClickListener(v -> {
                dialog.dismiss();
                filePickerLauncher.launch("image/*");
            });
        }

        if (cardAudio != null) {
            cardAudio.setOnClickListener(v -> {
                dialog.dismiss();
                checkAndShowVoiceRecorder();
            });
        }

        if (cardDoc != null) {
            cardDoc.setOnClickListener(v -> {
                dialog.dismiss();
                filePickerLauncher.launch("*/*");
            });
        }

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
                        String err = "Gagal mengunggah lampiran";
                        try {
                            if (response.errorBody() != null) {
                                String errBody = response.errorBody().string();
                                org.json.JSONObject obj = new org.json.JSONObject(errBody);
                                if (obj.has("message")) err = obj.getString("message");
                            } else if (response.body() != null && response.body().getMessage() != null) {
                                err = response.body().getMessage();
                            }
                        } catch (Exception ignored) {}
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

        // Mode Edit Pesan
        if (editingMessage != null) {
            final ChatMessageItem targetToEdit = editingMessage;
            cancelEdit();
            btnSendMessage.setEnabled(false);

            HttpService api = RetroClient.getInstance(this).getApi2();
            api.editChatMessage(targetToEdit.getId(), msg).enqueue(new Callback<SendMessageResponse>() {
                @Override
                public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                    btnSendMessage.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        chatDb.updateMessageText(targetToEdit.getId(), msg);
                        targetToEdit.setMessage(msg);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(ChatRoomActivity.this, "Pesan berhasil diedit", Toast.LENGTH_SHORT).show();
                    } else {
                        String errMsg = (response.body() != null && response.body().getMessage() != null)
                                ? response.body().getMessage()
                                : "Gagal mengedit pesan";
                        Toast.makeText(ChatRoomActivity.this, errMsg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                    btnSendMessage.setEnabled(true);
                    Toast.makeText(ChatRoomActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
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
                    String err = "Gagal mengirim pesan";
                    try {
                        if (response.errorBody() != null) {
                            String errBody = response.errorBody().string();
                            org.json.JSONObject obj = new org.json.JSONObject(errBody);
                            if (obj.has("message")) err = obj.getString("message");
                        } else if (response.body() != null && response.body().getMessage() != null) {
                            err = response.body().getMessage();
                        }
                    } catch (Exception ignored) {}
                    go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                            ChatRoomActivity.this, err, go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.ERROR
                    );
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
        cancelEdit();
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
    public void onEditMessage(ChatMessageItem item) {
        if (item == null) return;
        if (item.isRead()) {
            Toast.makeText(this, "Pesan yang sudah dibaca tidak dapat diedit", Toast.LENGTH_SHORT).show();
            return;
        }

        cancelReply();

        editingMessage = item;
        if (llEditPreview != null) {
            llEditPreview.setVisibility(View.VISIBLE);
        }
        if (tvEditPreviewText != null) {
            tvEditPreviewText.setText(item.getMessage() != null ? item.getMessage() : "");
        }
        etMessageInput.setText(item.getMessage() != null ? item.getMessage() : "");
        etMessageInput.setSelection(etMessageInput.getText().length());
        etMessageInput.requestFocus();

        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etMessageInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void cancelEdit() {
        editingMessage = null;
        if (llEditPreview != null) {
            llEditPreview.setVisibility(View.GONE);
        }
        etMessageInput.setText("");
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

        android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_image_preview);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    android.view.WindowManager.LayoutParams.MATCH_PARENT,
                    android.view.WindowManager.LayoutParams.MATCH_PARENT
            );
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(dialog.getWindow(), false);
            androidx.core.view.WindowInsetsControllerCompat insetsController =
                    new androidx.core.view.WindowInsetsControllerCompat(dialog.getWindow(), dialog.getWindow().getDecorView());
            insetsController.setAppearanceLightStatusBars(true);
            insetsController.setAppearanceLightNavigationBars(true);
        }

        View llTopBar = dialog.findViewById(R.id.llTopBarPreview);
        ImageView ivFull = dialog.findViewById(R.id.ivFullImagePreview);
        ImageView ivClose = dialog.findViewById(R.id.ivCloseImagePreview);
        ProgressBar pb = dialog.findViewById(R.id.pbImagePreview);
        TextView tvTitle = dialog.findViewById(R.id.tvPreviewTitle);
        TextView tvSubtitle = dialog.findViewById(R.id.tvPreviewSubtitle);

        if (llTopBar != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(llTopBar, (v, insets) -> {
                int statusBarHeight = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars()).top;
                int topPadding = statusBarHeight > 0
                        ? statusBarHeight + (int)(8 * getResources().getDisplayMetrics().density)
                        : (int)(36 * getResources().getDisplayMetrics().density);
                int bottomPadding = (int)(12 * getResources().getDisplayMetrics().density);
                v.setPadding(v.getPaddingLeft(), topPadding, v.getPaddingRight(), bottomPadding);
                return insets;
            });
        }

        if (tvTitle != null) {
            if (!TextUtils.isEmpty(fileName)) {
                tvTitle.setText(fileName);
            } else {
                tvTitle.setText("Pratinjau Gambar");
            }
        }
        if (tvSubtitle != null) {
            tvSubtitle.setText("Foto Kedinasan");
        }

        if (ivFull != null && llTopBar != null) {
            ivFull.setOnClickListener(v -> {
                if (llTopBar.getVisibility() == View.VISIBLE) {
                    llTopBar.setVisibility(View.GONE);
                } else {
                    llTopBar.setVisibility(View.VISIBLE);
                }
            });
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

        android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_video_preview);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    android.view.WindowManager.LayoutParams.MATCH_PARENT,
                    android.view.WindowManager.LayoutParams.MATCH_PARENT
            );
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(dialog.getWindow(), false);
            androidx.core.view.WindowInsetsControllerCompat insetsController =
                    new androidx.core.view.WindowInsetsControllerCompat(dialog.getWindow(), dialog.getWindow().getDecorView());
            insetsController.setAppearanceLightStatusBars(true);
            insetsController.setAppearanceLightNavigationBars(true);
        }

        View llTopBarVideo = dialog.findViewById(R.id.llTopBarVideo);
        if (llTopBarVideo != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(llTopBarVideo, (v, insets) -> {
                int statusBarHeight = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars()).top;
                int topPadding = statusBarHeight > 0
                        ? statusBarHeight + (int)(8 * getResources().getDisplayMetrics().density)
                        : (int)(36 * getResources().getDisplayMetrics().density);
                int bottomPadding = (int)(12 * getResources().getDisplayMetrics().density);
                v.setPadding(v.getPaddingLeft(), topPadding, v.getPaddingRight(), bottomPadding);
                return insets;
            });
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

    private void launchCameraCapture() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 101);
            return;
        }

        try {
            File photoFile = new File(getCacheDir(), "camera_" + System.currentTimeMillis() + ".jpg");
            cameraPhotoUri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    "go.pemkott.appsandroidmobiletebingtinggi.fileprovider",
                    photoFile
            );
            cameraLauncher.launch(cameraPhotoUri);
        } catch (Exception e) {
            Toast.makeText(this, "Gagal membuka kamera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAndShowVoiceRecorder() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 102);
            return;
        }
        showVoiceRecorderDialog();
    }

    private void showVoiceRecorderDialog() {
        if (adapter != null) {
            adapter.stopAudio();
        }
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_voice_recorder, null);
        dialog.setContentView(view);

        ImageView ivClose = view.findViewById(R.id.ivCloseVoiceRecorder);
        TextView tvTimer = view.findViewById(R.id.tvRecordingTimer);
        TextView tvInstruction = view.findViewById(R.id.tvRecordingInstruction);
        com.google.android.material.floatingactionbutton.FloatingActionButton fabToggle = view.findViewById(R.id.fabRecordToggle);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancelRecording);
        MaterialButton btnSend = view.findViewById(R.id.btnSendVoiceNote);

        final File[] recordFile = {null};
        final android.media.MediaRecorder[] recorder = {null};
        final boolean[] isRecording = {false};
        final long[] startTime = {0};
        Handler timerHandler = new Handler(Looper.getMainLooper());

        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRecording[0]) {
                    long elapsed = System.currentTimeMillis() - startTime[0];
                    int seconds = (int) (elapsed / 1000);
                    int m = seconds / 60;
                    int s = seconds % 60;
                    tvTimer.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", m, s));
                    timerHandler.postDelayed(this, 500);
                }
            }
        };

        fabToggle.setOnClickListener(v -> {
            if (!isRecording[0]) {
                try {
                    recordFile[0] = new File(getCacheDir(), "voice_note_" + System.currentTimeMillis() + ".m4a");
                    recorder[0] = new android.media.MediaRecorder();
                    recorder[0].setAudioSource(android.media.MediaRecorder.AudioSource.MIC);
                    recorder[0].setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4);
                    recorder[0].setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC);
                    recorder[0].setAudioSamplingRate(44100);
                    recorder[0].setAudioEncodingBitRate(96000);
                    recorder[0].setOutputFile(recordFile[0].getAbsolutePath());
                    recorder[0].prepare();
                    recorder[0].start();

                    isRecording[0] = true;
                    startTime[0] = System.currentTimeMillis();
                    fabToggle.setImageResource(R.drawable.ic_pause);
                    fabToggle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DC2626")));
                    tvInstruction.setText("Merekam... Tekan tombol untuk berhenti");
                    btnSend.setEnabled(false);
                    timerHandler.post(timerRunnable);
                } catch (Exception e) {
                    Toast.makeText(this, "Gagal memulai perekaman: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                try {
                    if (recorder[0] != null) {
                        recorder[0].stop();
                        recorder[0].release();
                        recorder[0] = null;
                    }
                } catch (Exception ignored) {}
                isRecording[0] = false;
                timerHandler.removeCallbacks(timerRunnable);
                fabToggle.setImageResource(R.drawable.ic_mic);
                fabToggle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9333EA")));
                tvInstruction.setText("Perekaman selesai. Tekan 'Kirim' untuk mengirim pesan suara.");
                btnSend.setEnabled(true);
            }
        });

        btnCancel.setOnClickListener(v -> {
            if (isRecording[0] && recorder[0] != null) {
                try {
                    recorder[0].stop();
                    recorder[0].release();
                } catch (Exception ignored) {}
                isRecording[0] = false;
            }
            timerHandler.removeCallbacks(timerRunnable);
            if (recordFile[0] != null && recordFile[0].exists()) {
                recordFile[0].delete();
            }
            dialog.dismiss();
        });

        btnSend.setOnClickListener(v -> {
            if (isRecording[0] && recorder[0] != null) {
                try {
                    recorder[0].stop();
                    recorder[0].release();
                } catch (Exception ignored) {}
                isRecording[0] = false;
            }
            timerHandler.removeCallbacks(timerRunnable);
            if (recordFile[0] != null && recordFile[0].exists()) {
                uploadAndSendRecordedAudio(recordFile[0]);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "File rekaman tidak ditemukan", Toast.LENGTH_SHORT).show();
            }
        });

        if (ivClose != null) {
            ivClose.setOnClickListener(v -> btnCancel.performClick());
        }

        dialog.setOnDismissListener(d -> {
            if (isRecording[0] && recorder[0] != null) {
                try {
                    recorder[0].stop();
                    recorder[0].release();
                } catch (Exception ignored) {}
            }
            timerHandler.removeCallbacks(timerRunnable);
        });

        dialog.show();
    }

    private void uploadAndSendRecordedAudio(File audioFile) {
        if (audioFile == null || !audioFile.exists()) return;

        btnSendMessage.setEnabled(false);
        pbChatRoom.setVisibility(View.VISIBLE);

        RequestBody chatTypeBody = RequestBody.create(MediaType.parse("text/plain"), chatType);
        RequestBody targetIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(targetId));
        RequestBody messageBody = RequestBody.create(MediaType.parse("text/plain"), "Pesan Suara");
        RequestBody msgTypeBody = RequestBody.create(MediaType.parse("text/plain"), "audio");
        RequestBody replyToBody = RequestBody.create(MediaType.parse("text/plain"), activeReplyToId != null ? String.valueOf(activeReplyToId) : "");

        RequestBody fileReqBody = RequestBody.create(MediaType.parse("audio/mp4"), audioFile);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", audioFile.getName(), fileReqBody);

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.sendChatMessageMultipart(chatTypeBody, targetIdBody, messageBody, msgTypeBody, replyToBody, filePart).enqueue(new Callback<SendMessageResponse>() {
            @Override
            public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                btnSendMessage.setEnabled(true);
                pbChatRoom.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    ChatMessageItem newMsgItem = response.body().getData();
                    if (newMsgItem != null) {
                        newMsgItem.setMsgType("audio");
                        chatDb.insertMessage(newMsgItem);
                        adapter.addMessage(newMsgItem);
                        rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                    } else {
                        loadMessages(false);
                    }
                    cancelReply();
                } else {
                    String err = "Gagal mengirim pesan suara";
                    try {
                        if (response.errorBody() != null) {
                            String errBody = response.errorBody().string();
                            org.json.JSONObject obj = new org.json.JSONObject(errBody);
                            if (obj.has("message")) err = obj.getString("message");
                        } else if (response.body() != null && response.body().getMessage() != null) {
                            err = response.body().getMessage();
                        }
                    } catch (Exception ignored) {}
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
    }

    private void showMoreOptionsMenu(View anchor) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_chat_room_menu, null);

        final android.widget.PopupWindow popupWindow = new android.widget.PopupWindow(
                popupView,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(16f);

        View menuSearch = popupView.findViewById(R.id.llMenuSearchMessage);
        View menuOpd = popupView.findViewById(R.id.llMenuOpdProfile);
        View menuClear = popupView.findViewById(R.id.llMenuClearChat);
        View menuBlock = popupView.findViewById(R.id.llMenuBlockContact);

        if ("opd".equals(chatType)) {
            if (menuOpd != null) menuOpd.setVisibility(View.VISIBLE);
            if (menuBlock != null) menuBlock.setVisibility(View.GONE);
        } else {
            if (menuOpd != null) menuOpd.setVisibility(View.GONE);
            if (menuBlock != null) {
                menuBlock.setVisibility(View.VISIBLE);
                TextView tvMenuBlock = popupView.findViewById(R.id.tvMenuBlockContact);
                if (tvMenuBlock != null) {
                    tvMenuBlock.setText(isContactBlocked ? "Buka Blokir Kontak" : "Blokir Kontak");
                }
            }
        }

        if (menuSearch != null) {
            menuSearch.setOnClickListener(v -> {
                popupWindow.dismiss();
                if (llChatSearchBar != null) {
                    llChatSearchBar.setVisibility(View.VISIBLE);
                }
                if (etSearchInChat != null) {
                    etSearchInChat.requestFocus();
                    android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(etSearchInChat, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            });
        }

        if (menuOpd != null) {
            menuOpd.setOnClickListener(v -> {
                popupWindow.dismiss();
                showOpdGroupProfileDialog();
            });
        }

        if (menuClear != null) {
            menuClear.setOnClickListener(v -> {
                popupWindow.dismiss();
                confirmClearConversation();
            });
        }

        if (menuBlock != null) {
            menuBlock.setOnClickListener(v -> {
                popupWindow.dismiss();
                confirmBlockContact();
            });
        }

        // Measure content to place within screen bounds
        popupView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int popupWidth = popupView.getMeasuredWidth();
        if (popupWidth <= 0) {
            popupWidth = (int) (210 * getResources().getDisplayMetrics().density);
        }

        int[] location = new int[2];
        anchor.getLocationOnScreen(location);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int margin = (int) (12 * getResources().getDisplayMetrics().density);

        // Align right edge of popup with right edge of anchor, clamped within screen
        int xPos = location[0] + anchor.getWidth() - popupWidth;
        if (xPos + popupWidth > screenWidth - margin) {
            xPos = screenWidth - popupWidth - margin;
        }
        if (xPos < margin) {
            xPos = margin;
        }

        int yPos = location[1] + anchor.getHeight() + (int) (6 * getResources().getDisplayMetrics().density);

        popupWindow.showAtLocation(anchor, android.view.Gravity.NO_GRAVITY, xPos, yPos);
    }

    private void confirmClearConversation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Bersihkan Obrolan")
                .setMessage("Apakah Anda yakin ingin menghapus seluruh riwayat pesan dalam obrolan ini?")
                .setPositiveButton("Bersihkan", (dialog, which) -> {
                    HttpService api = RetroClient.getInstance(this).getApi2();
                    api.clearConversationHistory(targetId, chatType).enqueue(new Callback<SendMessageResponse>() {
                        @Override
                        public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {}
                        @Override
                        public void onFailure(Call<SendMessageResponse> call, Throwable t) {}
                    });

                    chatDb.clearConversationLocal(chatType, targetId);
                    messageList.clear();
                    adapter.setList(messageList);
                    Toast.makeText(this, "Riwayat obrolan telah dibersihkan", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void confirmBlockContact() {
        String actionTitle = isContactBlocked ? "Buka Blokir Kontak" : "Blokir Kontak";
        String actionMsg = isContactBlocked
                ? "Apakah Anda yakin ingin membuka blokir kontak ini? Anda dan kontak akan dapat saling bertukar pesan kembali."
                : "Apakah Anda yakin ingin memblokir kontak ini? Kontak tidak akan dapat mengirimkan pesan kepada Anda.";
        String positiveBtn = isContactBlocked ? "Buka Blokir" : "Blokir";
        String actionType = isContactBlocked ? "unblock" : "block";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(actionTitle)
                .setMessage(actionMsg)
                .setPositiveButton(positiveBtn, (dialog, which) -> {
                    HttpService api = RetroClient.getInstance(this).getApi2();
                    api.toggleBlockContact(targetId, actionType).enqueue(new Callback<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatPrivacyResponse>() {
                        @Override
                        public void onResponse(Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatPrivacyResponse> call, Response<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatPrivacyResponse> response) {
                            if (response.isSuccessful()) {
                                isContactBlocked = !isContactBlocked;
                                String resultMsg = isContactBlocked ? "Kontak telah diblokir" : "Blokir kontak telah dibuka";
                                go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                                        ChatRoomActivity.this, resultMsg, go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.SUCCESS
                                );
                            } else {
                                Toast.makeText(ChatRoomActivity.this, "Gagal mengubah status blokir", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatPrivacyResponse> call, Throwable t) {
                            Toast.makeText(ChatRoomActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void performInChatSearch(String keyword) {
        searchMatchPositions.clear();
        currentSearchIndex = -1;

        if (TextUtils.isEmpty(keyword)) {
            if (tvSearchMatchCount != null) {
                tvSearchMatchCount.setVisibility(View.GONE);
            }
            adapter.setHighlightedPosition(-1);
            return;
        }

        String query = keyword.toLowerCase().trim();
        for (int i = 0; i < messageList.size(); i++) {
            ChatMessageItem item = messageList.get(i);
            boolean matchMsg = item.getMessage() != null && item.getMessage().toLowerCase().contains(query);
            boolean matchSender = item.getSenderName() != null && item.getSenderName().toLowerCase().contains(query);
            boolean matchFile = item.getFileName() != null && item.getFileName().toLowerCase().contains(query);
            if (matchMsg || matchSender || matchFile) {
                searchMatchPositions.add(i);
            }
        }

        if (tvSearchMatchCount != null) {
            tvSearchMatchCount.setVisibility(View.VISIBLE);
        }

        if (searchMatchPositions.isEmpty()) {
            if (tvSearchMatchCount != null) {
                tvSearchMatchCount.setText("0/0");
            }
            adapter.setHighlightedPosition(-1);
        } else {
            // Telegram: melompat ke pesan cocok yang paling mutakhir (paling bawah/terakhir)
            currentSearchIndex = searchMatchPositions.size() - 1;
            updateSearchDisplay();
        }
    }

    private void navigateSearchPrev() {
        if (searchMatchPositions.isEmpty()) return;
        currentSearchIndex--;
        if (currentSearchIndex < 0) {
            currentSearchIndex = searchMatchPositions.size() - 1;
        }
        updateSearchDisplay();
    }

    private void navigateSearchNext() {
        if (searchMatchPositions.isEmpty()) return;
        currentSearchIndex++;
        if (currentSearchIndex >= searchMatchPositions.size()) {
            currentSearchIndex = 0;
        }
        updateSearchDisplay();
    }

    private void updateSearchDisplay() {
        if (searchMatchPositions.isEmpty() || currentSearchIndex < 0 || currentSearchIndex >= searchMatchPositions.size()) {
            return;
        }

        if (tvSearchMatchCount != null) {
            tvSearchMatchCount.setText((currentSearchIndex + 1) + "/" + searchMatchPositions.size());
        }

        int targetPos = searchMatchPositions.get(currentSearchIndex);
        adapter.setHighlightedPosition(targetPos);
        rvMessages.smoothScrollToPosition(targetPos);
    }

    private void closeChatSearch() {
        if (llChatSearchBar != null) {
            llChatSearchBar.setVisibility(View.GONE);
        }
        if (etSearchInChat != null) {
            etSearchInChat.setText("");
        }
        searchMatchPositions.clear();
        currentSearchIndex = -1;
        if (tvSearchMatchCount != null) {
            tvSearchMatchCount.setVisibility(View.GONE);
        }
        adapter.setHighlightedPosition(-1);

        View view = this.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                launchCameraCapture();
            } else {
                Toast.makeText(this, "Izin kamera diperlukan untuk mengambil foto", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 102) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                showVoiceRecorderDialog();
            } else {
                Toast.makeText(this, "Izin mikrofon diperlukan untuk merekam pesan suara", Toast.LENGTH_SHORT).show();
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
        if (adapter != null) {
            adapter.releaseAudio();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.releaseAudio();
        }
    }
}
