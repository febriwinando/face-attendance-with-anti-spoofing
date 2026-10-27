package go.pemkott.appsandroidmobiletebingtinggi.camerax;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Size;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.TorchState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.env.ImageUtils;
import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.tflite.Classifier;
import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.tflite.TFLiteObjectDetectionAPIModel;
import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.tflite.Utils;
import go.pemkott.appsandroidmobiletebingtinggi.dinasluarkantor.perjalanandinas.PerjalananDinasFinalActivity;
import go.pemkott.appsandroidmobiletebingtinggi.dinasluarkantor.tugaslapangan.TugasLapanganFinalActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izin.cuti.IzinCutiFinalActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izin.keperluanpribadi.KeperluanPribadiFinalActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izin.sakit.IzinSakitFinalActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izinshift.izinshiftcuti.IzinCutiShiftFinalActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izinshift.izinshiftpribadi.KeperluanPribadiShiftFinalActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izinshift.izinshiftsakit.IzinSakitShiftFinalActivity;
import go.pemkott.appsandroidmobiletebingtinggi.kehadiran.AbsensiKehadiranActivity;
import go.pemkott.appsandroidmobiletebingtinggi.kehadiransift.AbsenShiftActivity;

public class CameraXDetectionOnlyActivity extends AppCompatActivity {

    // ================= UI =================
    private PreviewView previewView;
    private FaceOverlayView faceOverlay;
    private ImageButton capture;
    ImageView flipCamera, toggleFlash;
    private TextView txtChallenge;

    // ================= CAMERA =================
    private ImageCapture imageCapture;
    private Camera camera;
    private int lensFacing = CameraSelector.LENS_FACING_FRONT;

    // ================= FACE =================
    private FaceDetector faceDetector;
    private boolean faceInsideFrame = false;

    // ================= CAPTURE STAGES =================
    private enum CaptureStage {
        ANTI_SPOOFING_INIT, // Tahap 1
        CHALLENGES,         // Tahap 2
        ANTI_SPOOFING_FINAL, // Tahap 3
        CAPTURE,            // Tahap 4
        DONE
    }
    private CaptureStage currentStage = CaptureStage.ANTI_SPOOFING_INIT;

    // ================= CHALLENGE =================
    enum Challenge {
        BLINK, SMILE, TURN_LEFT, TURN_RIGHT, LOOK_UP
    }

    private final List<Challenge> challengeQueue = new ArrayList<>();
    private int challengeIndex = 0;

    // ================= PERMISSION =================
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) startCamera();
                        else Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show();
                    }
            );

    private String aktivitas;
    private boolean isCapturing = false;

    // Buffers for optimized anti-spoofing processing (DetectorActivity style)
    private Bitmap rgbFrameBitmap = null;
    private Bitmap faceBmp = null;
    private int[] rgbBytes = null;
    private byte[] yBuffer = null;
    private byte[] uBuffer = null;
    private byte[] vBuffer = null;

    // Antispofing TFLite (Metode DetectorActivity)
    private Classifier antispofingDetector;
    private static final int TF_OD_API_INPUT_SIZE = 224;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "mask_detector.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/mobile_label.txt";
    private boolean isProcessingAntispofing = false;
    private boolean isAnalyzing = false;
    private int nyataStreak = 0;
    private ExecutorService analysisExecutor;


    // ================= LIFECYCLE =================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_xdetection_only);

        previewView = findViewById(R.id.cameraPreview);
        faceOverlay = findViewById(R.id.faceOverlay);
        capture = findViewById(R.id.capture);
        txtChallenge = findViewById(R.id.txtChallenge);
        flipCamera = findViewById(R.id.flipCamera);
        toggleFlash = findViewById(R.id.toggleFlash);
        findViewById(R.id.ivBackCamera).setOnClickListener(v -> finish());

        aktivitas = getIntent().getStringExtra("aktivitas");

        capture.setEnabled(false);
        capture.setAlpha(0.5f);

        flipCamera.setOnClickListener(v -> toggleCamera());
        toggleFlash.setOnClickListener(v -> toggleFlashMode());

        initFaceDetector();
        generateChallengeQueue();

        try {
            antispofingDetector = TFLiteObjectDetectionAPIModel.create(
                    getAssets(),
                    TF_OD_API_MODEL_FILE,
                    TF_OD_API_LABELS_FILE,
                    TF_OD_API_INPUT_SIZE,
                    TF_OD_API_IS_QUANTIZED);
        } catch (Exception e) {
            e.printStackTrace();
        }

        capture.setOnClickListener(v -> {
            if (!faceInsideFrame) {
                Toast.makeText(this, "Posisikan wajah di dalam frame", Toast.LENGTH_SHORT).show();
                return;
            }

            if (challengeIndex < challengeQueue.size()) {
                Toast.makeText(this, "Selesaikan challenge terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            takePicture();
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startCamera();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();   // atau aksi lain
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetState();
    }

    @Override
    protected void onDestroy() {
        if (analysisExecutor != null) {
            analysisExecutor.shutdown();
        }
        if (rgbFrameBitmap != null) {
            rgbFrameBitmap.recycle();
            rgbFrameBitmap = null;
        }
        if (faceBmp != null) {
            faceBmp.recycle();
            faceBmp = null;
        }
        super.onDestroy();
    }

    // ================= FACE DETECTOR =================
    private void initFaceDetector() {
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .setMinFaceSize(0.15f)
                        .build();

        faceDetector = FaceDetection.getClient(options);
    }

    // ================= CHALLENGE =================
    private void generateChallengeQueue() {
        Challenge[] pool = Challenge.values();
        List<Challenge> temp = new ArrayList<>();
        Random r = new Random();

        while (temp.size() < 2) { // 2 tantangan random
            Challenge c = pool[r.nextInt(pool.length)];
            if (!temp.contains(c)) temp.add(c);
        }

        challengeQueue.clear();
        challengeQueue.addAll(temp);
        Collections.shuffle(challengeQueue);

        challengeIndex = 0;
        showCurrentChallenge();
    }

    private void showCurrentChallenge() {
        runOnUiThread(() -> {
            if (challengeIndex < challengeQueue.size()) {
                Challenge c = challengeQueue.get(challengeIndex);
                txtChallenge.setText("Tahap 2: Liveness Check\n" + getChallengeText(c));
            }
        });
    }

    private String getChallengeText(Challenge c) {
        switch (c) {
            case BLINK: return "Kedipkan mata";
            case SMILE: return "Senyum";
            case TURN_LEFT: return "Hadap ke kiri";
            case TURN_RIGHT: return "Hadap ke kanan";
            case LOOK_UP: return "Angkat dagu";
            default: return "";
        }
    }

    // ================= CAMERA =================
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                analysisExecutor = Executors.newSingleThreadExecutor();

                ImageAnalysis analysis =
                        new ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .setTargetResolution(new Size(640, 480))
                                .build();

                analysis.setAnalyzer(
                        analysisExecutor,
                        this::analyzeFrame
                );

                provider.unbindAll();
                camera = provider.bindToLifecycle(
                        this,
                        new CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build(),
                        preview,
                        imageCapture,
                        analysis
                );

                updateFlashButtonVisibility();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // ================= ANALYSIS =================
    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyzeFrame(ImageProxy proxy) {
        if (isFinishing() || isDestroyed() || isAnalyzing) {
            proxy.close();
            return;
        }

        Image img = proxy.getImage();
        if (img == null) {
            proxy.close();
            return;
        }

        isAnalyzing = true;
        InputImage image = InputImage.fromMediaImage(
                img,
                proxy.getImageInfo().getRotationDegrees()
        );

        faceDetector.process(image)
                .addOnSuccessListener(analysisExecutor, faces -> handleFaces(faces, image, proxy))
                .addOnFailureListener(analysisExecutor, e -> proxy.close())
                .addOnCompleteListener(analysisExecutor, t -> {
                    isAnalyzing = false;
                });
    }

    private void handleFaces(List<Face> faces, InputImage image, ImageProxy proxy) {

        if (faces.isEmpty()) {
            proxy.close();
            // Jangan resetStage sepenuhnya agar tahap tidak kembali ke awal
            runOnUiThread(() -> {
                faceOverlay.setFaceInside(false);
                faceOverlay.setProgress(0f);
            });
            return;
        }

        Face face = faces.get(0);
        Rect rect = face.getBoundingBox();
        float l = rect.left / (float) image.getWidth();
        float t = rect.top / (float) image.getHeight();
        float r = rect.right / (float) image.getWidth();
        float b = rect.bottom / (float) image.getHeight();

        RectF projectedFace = new RectF();
        int rotation = proxy.getImageInfo().getRotationDegrees();

        // Proyeksi koordinat Landscape Buffer ke Portrait UI
        if (rotation == 270) {
            projectedFace.left = t;
            projectedFace.top = 1f - r;
            projectedFace.right = b;
            projectedFace.bottom = 1f - l;
        } else if (rotation == 90) {
            projectedFace.left = 1f - b;
            projectedFace.top = l;
            projectedFace.right = 1f - t;
            projectedFace.bottom = r;
        } else {
            projectedFace.set(l, t, r, b);
        }

        // Mirroring untuk kamera depan
        if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            float left = 1f - projectedFace.right;
            float right = 1f - projectedFace.left;
            projectedFace.left = left;
            projectedFace.right = right;
        }

        faceInsideFrame = faceOverlay.getFrameNormalized().contains(projectedFace.centerX(), projectedFace.centerY());
        runOnUiThread(() -> {
            faceOverlay.setFaceInside(faceInsideFrame);
            // Tampilkan kotak AI yang sudah sinkron dengan posisi wajah di layar
            faceOverlay.setFaceData(faceInsideFrame, projectedFace, "");
        });

        if (!faceInsideFrame) {
            proxy.close();
            runOnUiThread(() -> {
                txtChallenge.setText("⚠️ Posisikan wajah di dalam oval");
                faceOverlay.setProgress(0f);
            });
            return;
        }

        // Anti-Spoofing Langsung Bekerja secara berkelanjutan
        if (antispofingDetector != null && !isProcessingAntispofing) {
            isProcessingAntispofing = true;
            analysisExecutor.execute(() -> {
                try {
                    // 1. Proses Gambar Wajah dengan Koordinat yang Benar (SINKRON)
                    Bitmap faceBitmap = prepareFaceBitmap(proxy, face);
                    if (faceBitmap == null) return;

                    // 2. Cek Anti-Spoofing (AI + Sharpness + Moire)
                    boolean isSharp = isImageSharp(faceBitmap);
                    boolean hasMoire = detectMoirePattern(faceBitmap);
                    List<Classifier.Recognition> results = antispofingDetector.recognizeImage(faceBitmap);
                    float confidence = (!results.isEmpty()) ? results.get(0).getConfidence() : 0f;
                    boolean aiNyata = confidence >= 0.95f;

                    // 3. Update Streak Keaslian
                    if (aiNyata && isSharp && !hasMoire) {
                        nyataStreak++;
                    } else {
                        nyataStreak = 0;
                    }

                    // 4. Logika Stage & UI Update
                    runOnUiThread(() -> {
                        if (nyataStreak < 3) {
                             // Jika belum yakin wajah asli, tahan di Tahap 1/3 (Verifikasi)
                             String prefix = (currentStage.ordinal() < CaptureStage.CHALLENGES.ordinal()) 
                                     ? "Tahap 1: Verifikasi Awal" : "Tahap 3: Verifikasi Akhir";

                             String statusText;
                             if (!isSharp) {
                                 statusText = "⚠️ Gambar Kabur";
                             } else if (hasMoire) {
                                 statusText = "❌ PALSU (Layar Digital)";
                             } else if (!aiNyata) {
                                 statusText = "❌ PALSU (" + String.format("%.0f%%", confidence*100) + ")";
                             } else {
                                 statusText = "✅ NYATA (Memproses...)";
                             }

                             txtChallenge.setText(prefix + "\n" + statusText);
                        } else {
                             // Wajah Asli Terverifikasi (Streak >= 3), jalankan logika stage
                             processStageLogic(face, projectedFace);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    isProcessingAntispofing = false;
                    proxy.close();
                }
            });
        } else {
            proxy.close();
        }
    }

    private void processStageLogic(Face face, RectF faceNorm) {
        switch (currentStage) {
            case ANTI_SPOOFING_INIT:
                currentStage = CaptureStage.CHALLENGES;
                runOnUiThread(this::showCurrentChallenge);
                break;

            case CHALLENGES:
                if (challengeIndex >= challengeQueue.size()) {
                    currentStage = CaptureStage.ANTI_SPOOFING_FINAL;
                    nyataStreak = 0;
                } else {
                    detectChallenge(face, faceNorm);
                }
                break;

            case ANTI_SPOOFING_FINAL:
                currentStage = CaptureStage.CAPTURE;
                break;

            case CAPTURE:
                if (!isCapturing) {
                    isCapturing = true;
                    txtChallenge.setText("Tahap 4: Mengambil Foto...");
                    takePicture();
                }
                break;

            case DONE:
                break;
        }
    }

    private interface OnAntiSpoofingPassListener {
        void onPass();
    }


    private synchronized Bitmap prepareFaceBitmap(ImageProxy proxy, Face face) {
        int width = proxy.getWidth();
        int height = proxy.getHeight();
        int rotation = proxy.getImageInfo().getRotationDegrees();

        // Lazy allocation of buffers
        if (rgbBytes == null || rgbBytes.length != width * height) {
            rgbBytes = new int[width * height];
            rgbFrameBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            faceBmp = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Bitmap.Config.ARGB_8888);

            yBuffer = new byte[width * height];
            uBuffer = new byte[(width / 2) * (height / 2) * 2]; // Max possible size
            vBuffer = new byte[(width / 2) * (height / 2) * 2];
        }

        ImageProxy.PlaneProxy[] planes = proxy.getPlanes();
        fillBuffer(planes[0].getBuffer(), yBuffer);
        fillBuffer(planes[1].getBuffer(), uBuffer);
        fillBuffer(planes[2].getBuffer(), vBuffer);

        ImageUtils.convertYUV420ToARGB8888(
                yBuffer, uBuffer, vBuffer,
                width, height,
                planes[0].getRowStride(),
                planes[1].getRowStride(),
                planes[1].getPixelStride(),
                rgbBytes);

        rgbFrameBitmap.setPixels(rgbBytes, 0, width, 0, 0, width, height);

        // --- SINKRONISASI PRESISI KOORDINAT ---
        Canvas canvas = new Canvas(faceBmp);
        Rect rect = face.getBoundingBox(); // Koordinat Portrait dari ML Kit

        Matrix matrix = new Matrix();

        // 1. Geser ke origin (tengah wajah di portrait space)
        matrix.postTranslate(-rect.centerX(), -rect.centerY());

        // 2. Putar buffer Landscape ke Portrait (agar searah dengan bounding box)
        matrix.postRotate(rotation);

        // 3. Mirroring untuk kamera depan
        if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            matrix.postScale(-1, 1);
        }

        // 4. Tarik (Stretch) ke ukuran input AI 224x224
        float sx = (float) TF_OD_API_INPUT_SIZE / rect.width();
        float sy = (float) TF_OD_API_INPUT_SIZE / rect.height();
        matrix.postScale(sx, sy);

        // 5. Pindahkan hasil ke tengah bitmap 224x224
        matrix.postTranslate(TF_OD_API_INPUT_SIZE / 2f, TF_OD_API_INPUT_SIZE / 2f);

        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(rgbFrameBitmap, matrix, new Paint(Paint.FILTER_BITMAP_FLAG));

        return faceBmp;
    }

    private void fillBuffer(ByteBuffer buffer, byte[] array) {
        buffer.rewind();
        int remaining = buffer.remaining();
        if (array.length < remaining) return;
        buffer.get(array, 0, remaining);
    }

    private boolean isImageSharp(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        long laplacianSum = 0;
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int center = pixels[y * width + x] & 0xff;
                int left = pixels[y * width + (x - 1)] & 0xff;
                int right = pixels[y * width + (x + 1)] & 0xff;
                int top = pixels[(y - 1) * width + x] & 0xff;
                int bottom = pixels[(y + 1) * width + x] & 0xff;

                int laplacian = Math.abs(4 * center - left - right - top - bottom);
                laplacianSum += laplacian;
            }
        }
        float variance = (float) laplacianSum / (width * height);
        return variance > 5.0f;
    }

    private boolean detectMoirePattern(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int moireScore = 0;
        for (int y = 0; y < height - 1; y++) {
            for (int x = 0; x < width - 1; x++) {
                int p1 = pixels[y * width + x] & 0xFF;
                int p2 = pixels[y * width + (x + 1)] & 0xFF;
                int p3 = pixels[(y + 1) * width + x] & 0xFF;

                int diffH = Math.abs(p1 - p2);
                int diffV = Math.abs(p1 - p3);

                if (diffH > 40 || diffV > 40) {
                    moireScore++;
                }
            }
        }
        return moireScore > (width * height * 0.08);
    }

    private void detectChallenge(Face face, RectF faceNorm) {

        Challenge c = challengeQueue.get(challengeIndex);
        boolean passed = false;
        float currentProgress = 0f;

        float eulerY = face.getHeadEulerAngleY(); // kiri-kanan
        float eulerX = face.getHeadEulerAngleX(); // atas-bawah

        switch (c) {
            case BLINK:
                Float l = face.getLeftEyeOpenProbability();
                Float r = face.getRightEyeOpenProbability();
                passed = l != null && r != null && l < 0.3f && r < 0.3f;
                if (l != null && r != null) currentProgress = 1f - ((l + r) / 2f);
                break;

            case SMILE:
                Float s = face.getSmilingProbability();
                passed = s != null && s > 0.6f;
                if (s != null) currentProgress = s / 0.6f;
                break;

            case TURN_LEFT:
                passed = eulerY < -1;
                currentProgress = passed ? 1f : Math.abs(eulerY) / 1f;
                break;

            case TURN_RIGHT:
                passed = eulerY > 1;
                currentProgress = passed ? 1f : Math.abs(eulerY) / 1f;
                break;

            case LOOK_UP:
                passed = eulerX < -1;
                currentProgress = passed ? 1f : Math.abs(eulerX) / 1f;
                break;
        }

        final float finalProgress = Math.min(1f, Math.max(0f, currentProgress));
        runOnUiThread(() -> faceOverlay.setProgress(finalProgress));

        if (passed) advanceChallenge();
    }


    private void advanceChallenge() {
        challengeIndex++;
        runOnUiThread(() -> faceOverlay.setProgress(0f));

        if (challengeIndex >= challengeQueue.size()) {
            currentStage = CaptureStage.ANTI_SPOOFING_FINAL;
            runOnUiThread(() -> txtChallenge.setText("Tahap 3: Verifikasi Akhir\n(Fokuskan wajah ke kamera)"));
            return;
        }

        runOnUiThread(this::showCurrentChallenge);
    }

    private void resetState() {
        currentStage = CaptureStage.ANTI_SPOOFING_INIT;
        challengeIndex = 0;
        isCapturing = false;
        nyataStreak = 0;
        if (camera != null) camera.getCameraControl().enableTorch(false);
        generateChallengeQueue();
        runOnUiThread(() -> {
            faceOverlay.setFaceData(false, null, "");
            txtChallenge.setText("Arahkan wajah ke frame");
        });
    }


    private void toggleCamera() {
        lensFacing = (lensFacing == CameraSelector.LENS_FACING_FRONT)
                ? CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT;
        startCamera();
    }

    private void toggleFlashMode() {
        if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
            Integer torchState = camera.getCameraInfo().getTorchState().getValue();
            boolean isTorchOn = torchState != null && torchState == TorchState.ON;
            camera.getCameraControl().enableTorch(!isTorchOn);
            toggleFlash.setImageResource(!isTorchOn ? R.drawable.flashof : R.drawable.flash);
        }
    }

    private void updateFlashButtonVisibility() {
        if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
            toggleFlash.setVisibility(View.VISIBLE);
        } else {
            toggleFlash.setVisibility(View.GONE);
        }
    }


    // ================= CAPTURE =================
    private void takePicture() {
        // Anti-Spoofing Visual: Gunakan Flash untuk mendeteksi pantulan layar
        if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
            camera.getCameraControl().enableTorch(true);
        }

        Dialog dialogproses = new Dialog(CameraXDetectionOnlyActivity.this, R.style.DialogStyle);
        dialogproses.setContentView(R.layout.view_proses);
        dialogproses.setCancelable(false);


        String fileName = System.currentTimeMillis() + ".jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/eabsensi");

        ImageCapture.OutputFileOptions options =
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                ).build();

        imageCapture.takePicture(
                options,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        if (camera != null) camera.getCameraControl().enableTorch(false);
                        dialogproses.dismiss();
                        if (output.getSavedUri() != null) {
                            kirimHasil(output.getSavedUri().toString());
                        } else {
                            kirimHasil(fileName);
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        isCapturing = false;
                        Toast.makeText(CameraXDetectionOnlyActivity.this,
                                e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        dialogproses.show();
    }

    private void kirimHasil(String fileName) {
        Intent i;

        if ("kehadiran".equals(aktivitas)) {
            i = new Intent(this, AbsensiKehadiranActivity.class);
        } else if ("tugaslapangan".equals(aktivitas)) {
            i = new Intent(this, TugasLapanganFinalActivity.class);
        } else if ("perjalanandinas".equals(aktivitas)) {
            i = new Intent(this, PerjalananDinasFinalActivity.class);
        } else if ("izincuti".equals(aktivitas)) {
            i = new Intent(this, IzinCutiFinalActivity.class);
        } else if ("izinkp".equals(aktivitas)) {
            i = new Intent(this, KeperluanPribadiFinalActivity.class);
        } else if ("izinsakit".equals(aktivitas)) {
            i = new Intent(this, IzinSakitFinalActivity.class);
        } else if ("kehadiransift".equals(aktivitas)) {
            i = new Intent(this, AbsenShiftActivity.class);
        } else if ("shiftizinsakit".equals(aktivitas)) {
            i = new Intent(this, IzinSakitShiftFinalActivity.class);
        } else if ("shiftizinkp".equals(aktivitas)) {
            i = new Intent(this, KeperluanPribadiShiftFinalActivity.class);
        } else if ("shiftizincuti".equals(aktivitas)) {
            i = new Intent(this, IzinCutiShiftFinalActivity.class);
        } else {
            i = new Intent();
            i.putExtra("namafile", fileName);
            setResult(RESULT_OK, i);
            finish();
            return;
        }

        if (getIntent().getExtras() != null) i.putExtras(getIntent().getExtras());
        i.putExtra("namafile", fileName);
        startActivity(i);
        finish();
    }
}

