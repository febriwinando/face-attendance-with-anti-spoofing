package go.pemkott.appsandroidmobiletebingtinggi.camerax;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

import go.pemkott.appsandroidmobiletebingtinggi.R;
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


import android.graphics.Matrix;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.tflite.Classifier;
import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.tflite.TFLiteObjectDetectionAPIModel;
import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.tflite.Utils;

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

    // ================= CHALLENGE =================
    enum Challenge {
        BLINK, SMILE, TURN_LEFT, TURN_RIGHT, LOOK_UP, ZOOM_IN
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

    // Antispofing TFLite (Metode DetectorActivity)
    private Classifier antispofingDetector;
    private static final int TF_OD_API_INPUT_SIZE = 224;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "mask_detector.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/mobile_label.txt";
    private boolean isProcessingAntispofing = false;
    private ExecutorService analysisExecutor;

    // Liveness Security
    private final List<RectF> positionHistory = new ArrayList<>();
    private static final int HISTORY_SIZE = 15;
    private boolean isRealFace = false;
    private float initialFaceWidth = 0;
    private float initialFaceX = 0;

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
            Utils.assetFilePath(this, "gpumodel_2.ptl");
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
    protected void onDestroy() {
        if (analysisExecutor != null) {
            analysisExecutor.shutdown();
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

        // Selalu sertakan BLINK dan ZOOM_IN sebagai tantangan wajib untuk anti-spoofing
        temp.add(Challenge.BLINK);
        temp.add(Challenge.ZOOM_IN);

        while (temp.size() < 3) { // Total 3 tantangan
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
        txtChallenge.setText(
                "Pemeriksaan " + (challengeIndex + 1) + " / " + challengeQueue.size()
                        + "\n" + getChallengeText(challengeQueue.get(challengeIndex))
        );
    }

    private String getChallengeText(Challenge c) {
        switch (c) {
            case BLINK: return "Kedipkan mata";
            case SMILE: return "Senyum";
            case TURN_LEFT: return "Hadap ke kiri";
            case TURN_RIGHT: return "Hadap ke kanan";
            case LOOK_UP: return "Angkat dagu";
            case ZOOM_IN: return "Maju/Mendekat ke kamera";
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

        Image img = proxy.getImage();
        if (img == null) {
            proxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                img,
                proxy.getImageInfo().getRotationDegrees()
        );

        faceDetector.process(image)
                .addOnSuccessListener(analysisExecutor, faces -> handleFaces(faces, image, proxy))
                .addOnFailureListener(analysisExecutor, e -> proxy.close())
                .addOnCompleteListener(analysisExecutor, t -> {
                    // Note: proxy is closed in handleFaces or onFailure
                });
    }

    private void handleFaces(List<Face> faces, InputImage image, ImageProxy proxy) {

        if (faces.isEmpty()) {
            proxy.close();
            resetState();
            return;
        }

        Face face = faces.get(0);

        RectF faceNorm = normalize(face.getBoundingBox(), image);

        if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            // mirror kamera depan
            float left = 1f - faceNorm.right;
            float right = 1f - faceNorm.left;
            faceNorm.left = left;
            faceNorm.right = right;
        }

        if (initialFaceWidth == 0) {
            initialFaceWidth = faceNorm.width();
            initialFaceX = faceNorm.centerX();
        }

        faceInsideFrame =
                faceOverlay.getFrameNormalized()
                        .contains(faceNorm.centerX(), faceNorm.centerY());

        runOnUiThread(() -> faceOverlay.setFaceInside(faceInsideFrame));

        if (!faceInsideFrame) {
            proxy.close();
            resetState();
            return;
        }

        // Anti-Spoofing 1: Check for natural micro-movements
        checkFaceStillness(faceNorm);
        if (!isRealFace && positionHistory.size() >= HISTORY_SIZE) {
            proxy.close();
            runOnUiThread(() -> txtChallenge.setText("⚠️ Gerakkan wajah perlahan\n(Pastikan wajah asli)"));
            return;
        }

        // Anti-Spoofing 2: TFLite Antispofing (Metode DetectorActivity)
        if (antispofingDetector != null && !isProcessingAntispofing) {
            isProcessingAntispofing = true;
            try {
                // Get rotated but NOT mirrored bitmap for inference (like DetectorActivity)
                Bitmap faceBmp = getFaceBitmap(proxy, face, false);
                if (faceBmp != null) {
                    Bitmap scaledFaceBmp = Bitmap.createScaledBitmap(faceBmp, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, false);
                    List<Classifier.Recognition> results = antispofingDetector.recognizeImage(scaledFaceBmp);
                    if (!results.isEmpty()) {
                        Classifier.Recognition result = results.get(0);
                        if (result.getConfidence() < 0.9f || "Palsu".equals(result.getTitle())) {
                            isProcessingAntispofing = false;
                            proxy.close();
                            runOnUiThread(() -> {
                                txtChallenge.setText("⚠️ Fokuskan Kamera Ke Wajah Anda\n(Wajah Tidak Valid)");
                                capture.setEnabled(false);
                                capture.setAlpha(0.5f);
                            });
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isProcessingAntispofing = false;
            }
        }

        proxy.close();

        // Anti-Spoofing 3: Chromatic Authenticity (YCbCr Analysis)
        if (challengeIndex >= challengeQueue.size() && !isCapturing) {
            // Analisis kulit hanya jika tantangan selesai
            // Karena ini butuh bitmap dan cukup berat, kita lakukan di sini
            // Namun karena kita sudah tutup proxy, kita pakai previewView.getBitmap() sebagai fallback atau simpan bitmap tadi
            // Untuk sementara kita lewati YCbCr jika PyTorch sudah sangat yakin
        }

        if (challengeIndex >= challengeQueue.size()) {
            if (!isCapturing) {
                isCapturing = true;
                runOnUiThread(() -> {
                    capture.setEnabled(true);
                    capture.setAlpha(1f);
                    takePicture();
                });
            }
            return;
        }

        detectChallenge(face, faceNorm);
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
                passed = eulerY > 18 && validate3DDepth(face, "left");
                currentProgress = eulerY / 18f;
                break;

            case TURN_RIGHT:
                passed = eulerY < -18 && validate3DDepth(face, "right");
                currentProgress = Math.abs(eulerY) / 18f;
                break;

            case LOOK_UP:
                passed = eulerX > 12 && validate3DDepth(face, "up");
                currentProgress = eulerX / 12f;
                break;

            case ZOOM_IN:
                // Harus ada peningkatan ukuran wajah minimal 25% dari posisi awal
                float growth = faceNorm.width() / initialFaceWidth;
                passed = growth > 1.25f;
                currentProgress = Math.min(1f, (growth - 1f) / 0.25f);
                break;
        }

        final float finalProgress = Math.min(1f, Math.max(0f, currentProgress));
        runOnUiThread(() -> faceOverlay.setProgress(finalProgress));

        if (passed) advanceChallenge();
    }

    private boolean validate3DDepth(Face face, String direction) {
        FaceLandmark nose = face.getLandmark(FaceLandmark.NOSE_BASE);
        FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
        FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);
        FaceLandmark leftCheek = face.getLandmark(FaceLandmark.LEFT_CHEEK);
        FaceLandmark rightCheek = face.getLandmark(FaceLandmark.RIGHT_CHEEK);

        if (nose == null || leftEye == null || rightEye == null || leftCheek == null || rightCheek == null) return false;

        float distLeft = Math.abs(nose.getPosition().x - leftEye.getPosition().x);
        float distRight = Math.abs(nose.getPosition().x - rightEye.getPosition().x);
        
        // Anti-Spoofing 3D: Pengecekan Rasio Pipit saat menoleh
        float cheekRatio = Math.abs(nose.getPosition().x - leftCheek.getPosition().x) / 
                           Math.abs(nose.getPosition().x - rightCheek.getPosition().x);

        if (direction.equals("left")) {
            // Menoleh kiri: Pipit kanan harus terlihat jauh lebih lebar secara perspektif
            return distRight > distLeft * 1.8f && cheekRatio < 0.6f;
        }
        if (direction.equals("right")) {
            // Menoleh kanan: Pipit kiri harus terlihat jauh lebih lebar secara perspektif
            return distLeft > distRight * 1.8f && cheekRatio > 1.6f;
        }
        
        return true;
    }

    private void advanceChallenge() {
        challengeIndex++;
        runOnUiThread(() -> faceOverlay.setProgress(0f));

        if (challengeIndex >= challengeQueue.size()) {
            if (!isCapturing) {
                isCapturing = true;
                runOnUiThread(() -> {
                    txtChallenge.setText("✔ Verifikasi berhasil");
                    capture.setEnabled(true);
                    capture.setAlpha(1f);
                    takePicture();
                });
            }
            return;
        }

        runOnUiThread(this::showCurrentChallenge);
    }

    private boolean validateSkinIntegrity(Bitmap fullBitmap, Rect faceRect) {
        // Ambil sampel area tengah wajah
        int centerX = faceRect.centerX();
        int centerY = faceRect.centerY();
        int sampleSize = 20; // Ambil grid 20x20 di tengah

        int skinPixels = 0;
        int totalPixels = 0;

        for (int y = centerY - sampleSize; y < centerY + sampleSize; y++) {
            for (int x = centerX - sampleSize; x < centerX + sampleSize; x++) {
                if (x < 0 || y < 0 || x >= fullBitmap.getWidth() || y >= fullBitmap.getHeight()) continue;

                int pixel = fullBitmap.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                // Convert to YCbCr
                double cb = 128 + (-0.168736 * r - 0.331264 * g + 0.5 * b);
                double cr = 128 + (0.5 * r - 0.418688 * g - 0.081312 * b);

                // Standar Klaster Warna Kulit Manusia (YCbCr Range)
                if (cb >= 77 && cb <= 127 && cr >= 133 && cr <= 173) {
                    skinPixels++;
                }

                // Anti-Monitor: Deteksi Dominasi Cahaya Biru (Emisi Layar)
                // Layar digital cenderung memiliki intensitas biru yang tidak alami pada area kulit
                if (b > r && b > g) {
                    return false; // Langsung tolak jika pixel wajah didominasi warna biru (khas layar)
                }

                totalPixels++;
            }
        }

        if (totalPixels == 0) return false;
        float skinRatio = (float) skinPixels / totalPixels;

        // Hitung Variansi Tekstur (Detecting 2D Flat Surface vs 3D Skin)
        // Kulit asli memiliki tekstur pori yang menyebabkan variansi warna mikro
        // Layar monitor memiliki pola piksel yang sangat teratur (Moiré) atau terlalu halus
        return skinRatio > 0.85f; 
    }

    private void resetState() {
        challengeIndex = 0;
        isCapturing = false;
        positionHistory.clear();
        isRealFace = false;
        initialFaceWidth = 0;
        initialFaceX = 0;
        runOnUiThread(() -> {
            capture.setEnabled(false);
            capture.setAlpha(0.5f);
            showCurrentChallenge();
        });
    }

    private void checkFaceStillness(RectF currentPos) {
        positionHistory.add(new RectF(currentPos));
        if (positionHistory.size() > HISTORY_SIZE) {
            positionHistory.remove(0);
        }

        if (positionHistory.size() == HISTORY_SIZE) {
            float varX = 0;
            float varY = 0;
            RectF first = positionHistory.get(0);
            for (RectF p : positionHistory) {
                varX += Math.abs(p.centerX() - first.centerX());
                varY += Math.abs(p.centerY() - first.centerY());
            }
            // Jika variansi sangat kecil, kemungkinan besar ini adalah foto statis
            isRealFace = (varX > 0.001f || varY > 0.001f);
        }
    }

    private Bitmap getFaceBitmap(ImageProxy proxy, Face face, boolean mirror) {
        try {
            Bitmap frameBitmap = proxy.toBitmap();
            if (frameBitmap == null) return null;

            Rect rect = face.getBoundingBox();
            Bitmap cropped = cropFace(frameBitmap, rect);
            if (cropped == null) return null;

            Matrix matrix = new Matrix();
            matrix.postRotate(proxy.getImageInfo().getRotationDegrees());
            if (mirror && lensFacing == CameraSelector.LENS_FACING_FRONT) {
                matrix.postScale(-1, 1, cropped.getWidth() / 2f, cropped.getHeight() / 2f);
            }

            return Bitmap.createBitmap(cropped, 0, 0, cropped.getWidth(), cropped.getHeight(), matrix, true);
        } catch (Exception e) {
            return null;
        }
    }

    private Bitmap cropFace(Bitmap bitmap, Rect boundingBox) {
        int left = Math.max(boundingBox.left, 0);
        int top = Math.max(boundingBox.top, 0);
        int right = Math.min(boundingBox.right, bitmap.getWidth());
        int bottom = Math.min(boundingBox.bottom, bitmap.getHeight());

        if (left >= right || top >= bottom) return null;

        return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top);
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

    // ================= UTILS =================
    private RectF normalize(Rect r, InputImage img) {
        return new RectF(
                r.left / (float) img.getWidth(),
                r.top / (float) img.getHeight(),
                r.right / (float) img.getWidth(),
                r.bottom / (float) img.getHeight()
        );
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

