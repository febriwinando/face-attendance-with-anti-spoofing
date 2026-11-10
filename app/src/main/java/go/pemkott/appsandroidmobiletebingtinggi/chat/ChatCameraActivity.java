package go.pemkott.appsandroidmobiletebingtinggi.chat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FallbackStrategy;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.PendingRecording;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Locale;

import go.pemkott.appsandroidmobiletebingtinggi.R;

public class ChatCameraActivity extends AppCompatActivity {

    public static final String EXTRA_CAPTURED_PATH = "extra_captured_path";
    public static final String EXTRA_CAPTURED_TYPE = "extra_captured_type"; // "image" or "video"

    private static final int PERMISSION_REQ_CODE = 2001;

    // ViewFinder & Overlays
    private PreviewView viewFinder;
    private View viewShutterFlash;
    private FrameLayout flReviewContainer;
    private ImageView ivPhotoReview;
    private VideoView vvVideoReview;
    private ImageView ivVideoReviewPlay;

    // Top Controls
    private View flCameraClose;
    private View flCameraFlash;
    private ImageView ivCameraFlash;
    private LinearLayout llRecordingIndicator;
    private TextView tvRecordingTimer;

    // Bottom Controls & Mode
    private LinearLayout llModeSelector;
    private TextView tvModePhoto;
    private TextView tvModeVideo;
    private RelativeLayout rlLiveCameraControls;
    private View flBtnFlipCamera;
    private FrameLayout flShutterPhoto;
    private FrameLayout flRecordVideo;
    private View viewRecordInner;

    // Review Actions
    private LinearLayout llReviewActions;
    private LinearLayout llBtnRetake;
    private LinearLayout llBtnConfirmSend;

    // CameraX State
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording activeRecording;

    // Modes & States
    private boolean isPhotoMode = true;
    private boolean isRecordingVideo = false;
    private int flashMode = ImageCapture.FLASH_MODE_OFF; // OFF -> AUTO -> ON

    // Timer handler for video recording
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private long recordingStartTime = 0;
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecordingVideo) {
                long elapsedMillis = SystemClock.elapsedRealtime() - recordingStartTime;
                int seconds = (int) (elapsedMillis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                if (tvRecordingTimer != null) {
                    tvRecordingTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                }
                timerHandler.postDelayed(this, 500);
            }
        }
    };

    // Captured Result for Review
    private File capturedFile = null;
    private String capturedType = null; // "image" or "video"

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_camera);

        initViews();
        checkAndRequestPermissions();
    }

    private void initViews() {
        viewFinder = findViewById(R.id.viewFinder);
        viewShutterFlash = findViewById(R.id.viewShutterFlash);
        flReviewContainer = findViewById(R.id.flReviewContainer);
        ivPhotoReview = findViewById(R.id.ivPhotoReview);
        vvVideoReview = findViewById(R.id.vvVideoReview);
        ivVideoReviewPlay = findViewById(R.id.ivVideoReviewPlay);

        flCameraClose = findViewById(R.id.flCameraClose);
        flCameraFlash = findViewById(R.id.flCameraFlash);
        ivCameraFlash = findViewById(R.id.ivCameraFlash);
        llRecordingIndicator = findViewById(R.id.llRecordingIndicator);
        tvRecordingTimer = findViewById(R.id.tvRecordingTimer);

        llModeSelector = findViewById(R.id.llModeSelector);
        tvModePhoto = findViewById(R.id.tvModePhoto);
        tvModeVideo = findViewById(R.id.tvModeVideo);
        rlLiveCameraControls = findViewById(R.id.rlLiveCameraControls);
        flBtnFlipCamera = findViewById(R.id.flBtnFlipCamera);
        flShutterPhoto = findViewById(R.id.flShutterPhoto);
        flRecordVideo = findViewById(R.id.flRecordVideo);
        viewRecordInner = findViewById(R.id.viewRecordInner);

        llReviewActions = findViewById(R.id.llReviewActions);
        llBtnRetake = findViewById(R.id.llBtnRetake);
        llBtnConfirmSend = findViewById(R.id.llBtnConfirmSend);

        // Click listeners
        flCameraClose.setOnClickListener(v -> finish());

        flCameraFlash.setOnClickListener(v -> toggleFlash());

        flBtnFlipCamera.setOnClickListener(v -> flipCamera());

        tvModePhoto.setOnClickListener(v -> setMode(true));
        tvModeVideo.setOnClickListener(v -> setMode(false));

        flShutterPhoto.setOnClickListener(v -> takePhoto());

        flRecordVideo.setOnClickListener(v -> {
            if (isRecordingVideo) {
                stopVideoRecording();
            } else {
                startVideoRecording();
            }
        });

        llBtnRetake.setOnClickListener(v -> resetToLiveCamera());

        llBtnConfirmSend.setOnClickListener(v -> confirmAndSend());

        ivVideoReviewPlay.setOnClickListener(v -> {
            if (vvVideoReview != null) {
                if (vvVideoReview.isPlaying()) {
                    vvVideoReview.pause();
                    ivVideoReviewPlay.setVisibility(View.VISIBLE);
                } else {
                    vvVideoReview.start();
                    ivVideoReviewPlay.setVisibility(View.GONE);
                }
            }
        });

        vvVideoReview.setOnClickListener(v -> {
            if (vvVideoReview.isPlaying()) {
                vvVideoReview.pause();
                ivVideoReviewPlay.setVisibility(View.VISIBLE);
            } else {
                vvVideoReview.start();
                ivVideoReviewPlay.setVisibility(View.GONE);
            }
        });
    }

    private void checkAndRequestPermissions() {
        boolean hasCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean hasAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        if (hasCamera && hasAudio) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQ_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_CODE) {
            boolean cameraGranted = false;
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.CAMERA.equals(permissions[i]) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    cameraGranted = true;
                }
            }
            if (cameraGranted) {
                startCamera();
            } else {
                Toast.makeText(this, "Izin kamera diperlukan untuk mengambil foto dan video", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                bindCameraUseCases();
            } catch (Exception e) {
                Toast.makeText(this, "Gagal memulai kamera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null) return;

        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(flashMode)
                .build();

        Recorder recorder = new Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD, FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)))
                .build();
        videoCapture = VideoCapture.withOutput(recorder);

        try {
            // Bind preview, imageCapture and videoCapture to lifecycle
            camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture,
                    videoCapture
            );
            applyFlashToCamera();
        } catch (Exception e) {
            // Fallback: If device doesn't support concurrent Image+Video capture, bind mode-specific use case
            try {
                if (isPhotoMode) {
                    camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                } else {
                    camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);
                }
                applyFlashToCamera();
            } catch (Exception ex) {
                Toast.makeText(this, "Gagal menginisialisasi kamera: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setMode(boolean photoMode) {
        if (isRecordingVideo) return; // Prevent mode change during active recording

        this.isPhotoMode = photoMode;
        if (photoMode) {
            tvModePhoto.setTextColor(Color.parseColor("#38BDF8"));
            tvModeVideo.setTextColor(Color.parseColor("#94A3B8"));
            flShutterPhoto.setVisibility(View.VISIBLE);
            flRecordVideo.setVisibility(View.GONE);
        } else {
            tvModePhoto.setTextColor(Color.parseColor("#94A3B8"));
            tvModeVideo.setTextColor(Color.parseColor("#EF4444"));
            flShutterPhoto.setVisibility(View.GONE);
            flRecordVideo.setVisibility(View.VISIBLE);
        }
    }

    private void flipCamera() {
        if (isRecordingVideo) return;

        lensFacing = (lensFacing == CameraSelector.LENS_FACING_BACK)
                ? CameraSelector.LENS_FACING_FRONT
                : CameraSelector.LENS_FACING_BACK;
        bindCameraUseCases();
    }

    private void toggleFlash() {
        if (camera == null) return;

        if (isPhotoMode) {
            if (flashMode == ImageCapture.FLASH_MODE_OFF) {
                flashMode = ImageCapture.FLASH_MODE_AUTO;
                ivCameraFlash.setImageResource(R.drawable.flash);
                ivCameraFlash.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FBBF24"))); // Yellow auto
                Toast.makeText(this, "Flash: Otomatis", Toast.LENGTH_SHORT).show();
            } else if (flashMode == ImageCapture.FLASH_MODE_AUTO) {
                flashMode = ImageCapture.FLASH_MODE_ON;
                ivCameraFlash.setImageResource(R.drawable.flash);
                ivCameraFlash.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                Toast.makeText(this, "Flash: Aktif", Toast.LENGTH_SHORT).show();
            } else {
                flashMode = ImageCapture.FLASH_MODE_OFF;
                ivCameraFlash.setImageResource(R.drawable.flashof);
                ivCameraFlash.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                Toast.makeText(this, "Flash: Nonaktif", Toast.LENGTH_SHORT).show();
            }
            if (imageCapture != null) {
                imageCapture.setFlashMode(flashMode);
            }
        } else {
            // Video Torch toggle
            boolean hasTorch = camera.getCameraInfo().hasFlashUnit();
            if (!hasTorch) {
                Toast.makeText(this, "Flash tidak tersedia pada kamera ini", Toast.LENGTH_SHORT).show();
                return;
            }
            Integer torchState = camera.getCameraInfo().getTorchState().getValue();
            boolean enable = (torchState == null || torchState == 0);
            camera.getCameraControl().enableTorch(enable);
            if (enable) {
                ivCameraFlash.setImageResource(R.drawable.flash);
                ivCameraFlash.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FBBF24")));
            } else {
                ivCameraFlash.setImageResource(R.drawable.flashof);
                ivCameraFlash.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            }
        }
    }

    private void applyFlashToCamera() {
        if (imageCapture != null) {
            imageCapture.setFlashMode(flashMode);
        }
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        // Visual Shutter Flash
        viewShutterFlash.setVisibility(View.VISIBLE);
        viewShutterFlash.setAlpha(0.85f);
        viewShutterFlash.animate().alpha(0f).setDuration(250).withEndAction(() -> viewShutterFlash.setVisibility(View.GONE)).start();

        File photoFile = new File(getCacheDir(), "SAHUT_CAM_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        capturedFile = photoFile;
                        capturedType = "image";
                        showPhotoReview(photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(ChatCameraActivity.this, "Gagal mengambil foto: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void startVideoRecording() {
        if (videoCapture == null) return;

        File videoFile = new File(getCacheDir(), "SAHUT_VID_" + System.currentTimeMillis() + ".mp4");
        FileOutputOptions options = new FileOutputOptions.Builder(videoFile).build();

        PendingRecording pending = videoCapture.getOutput().prepareRecording(this, options);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            pending.withAudioEnabled();
        }

        activeRecording = pending.start(ContextCompat.getMainExecutor(this), recordEvent -> {
            if (recordEvent instanceof VideoRecordEvent.Start) {
                isRecordingVideo = true;
                recordingStartTime = SystemClock.elapsedRealtime();
                llRecordingIndicator.setVisibility(View.VISIBLE);
                timerHandler.post(timerRunnable);

                // UI adjustments during recording
                viewRecordInner.setBackgroundResource(R.drawable.bg_shutter_inner_red_recording);
                llModeSelector.setVisibility(View.INVISIBLE);
                flBtnFlipCamera.setVisibility(View.INVISIBLE);
            } else if (recordEvent instanceof VideoRecordEvent.Finalize) {
                VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) recordEvent;
                isRecordingVideo = false;
                timerHandler.removeCallbacks(timerRunnable);
                llRecordingIndicator.setVisibility(View.GONE);
                viewRecordInner.setBackgroundResource(R.drawable.bg_shutter_inner_red);

                if (!finalizeEvent.hasError()) {
                    capturedFile = videoFile;
                    capturedType = "video";
                    showVideoReview(videoFile);
                } else {
                    activeRecording = null;
                    Toast.makeText(ChatCameraActivity.this, "Gagal merekam video", Toast.LENGTH_SHORT).show();
                    resetToLiveCamera();
                }
            }
        });
    }

    private void stopVideoRecording() {
        if (activeRecording != null) {
            activeRecording.stop();
            activeRecording = null;
        }
    }

    private void showPhotoReview(File photoFile) {
        rlLiveCameraControls.setVisibility(View.GONE);
        llModeSelector.setVisibility(View.GONE);
        flReviewContainer.setVisibility(View.VISIBLE);
        ivPhotoReview.setVisibility(View.VISIBLE);
        vvVideoReview.setVisibility(View.GONE);
        ivVideoReviewPlay.setVisibility(View.GONE);

        Glide.with(this).load(photoFile).into(ivPhotoReview);

        llReviewActions.setVisibility(View.VISIBLE);
    }

    private void showVideoReview(File videoFile) {
        rlLiveCameraControls.setVisibility(View.GONE);
        llModeSelector.setVisibility(View.GONE);
        flReviewContainer.setVisibility(View.VISIBLE);
        ivPhotoReview.setVisibility(View.GONE);
        vvVideoReview.setVisibility(View.VISIBLE);
        ivVideoReviewPlay.setVisibility(View.GONE);

        vvVideoReview.setVideoURI(Uri.fromFile(videoFile));
        vvVideoReview.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            vvVideoReview.start();
        });
        vvVideoReview.setOnCompletionListener(mp -> {
            ivVideoReviewPlay.setVisibility(View.VISIBLE);
        });

        llReviewActions.setVisibility(View.VISIBLE);
    }

    private void resetToLiveCamera() {
        if (vvVideoReview != null && vvVideoReview.isPlaying()) {
            vvVideoReview.stopPlayback();
        }

        if (capturedFile != null && capturedFile.exists()) {
            // Delete temp file if not confirmed
            try {
                capturedFile.delete();
            } catch (Exception ignored) {}
        }
        capturedFile = null;
        capturedType = null;

        flReviewContainer.setVisibility(View.GONE);
        ivPhotoReview.setVisibility(View.GONE);
        vvVideoReview.setVisibility(View.GONE);
        ivVideoReviewPlay.setVisibility(View.GONE);

        llReviewActions.setVisibility(View.GONE);
        rlLiveCameraControls.setVisibility(View.VISIBLE);
        llModeSelector.setVisibility(View.VISIBLE);
        flBtnFlipCamera.setVisibility(View.VISIBLE);

        setMode(isPhotoMode);
    }

    private void confirmAndSend() {
        if (vvVideoReview != null && vvVideoReview.isPlaying()) {
            vvVideoReview.stopPlayback();
        }

        if (capturedFile == null || !capturedFile.exists()) {
            Toast.makeText(this, "Berkas hasil rekaman tidak ditemukan", Toast.LENGTH_SHORT).show();
            resetToLiveCamera();
            return;
        }

        Intent data = new Intent();
        data.putExtra(EXTRA_CAPTURED_PATH, capturedFile.getAbsolutePath());
        data.putExtra(EXTRA_CAPTURED_TYPE, capturedType);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
        if (vvVideoReview != null) {
            try {
                vvVideoReview.stopPlayback();
            } catch (Exception ignored) {}
        }
        if (activeRecording != null) {
            activeRecording.stop();
            activeRecording = null;
        }
    }
}

