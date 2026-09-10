package go.pemkott.appsandroidmobiletebingtinggi.camerax;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
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
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
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

import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.tflite.Classifier;
import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.tflite.TFLiteObjectDetectionAPIModel;
import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.tflite.Utils;

public class CameraxActivity extends AppCompatActivity {

    private PreviewView previewView;
    private FaceOverlayView faceOverlay;
    private TextView txtChallenge;

    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private Camera camera;
    private int lensFacing = CameraSelector.LENS_FACING_FRONT;
    private FaceDetector faceDetector;
    private FaceRecognizer faceRecognizer;
    private ExecutorService analysisExecutor;
    
    private float[] referenceEmbedding;
    private boolean faceInsideFrame = false;
    private boolean isCapturing = false;
    private boolean livenessPassed = false;
    private boolean isSystemReady = false;

    // Antispofing TFLite (Metode DetectorActivity)
    private Classifier antispofingDetector;
    private static final int TF_OD_API_INPUT_SIZE = 224;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "mask_detector.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/mobile_label.txt";
    private boolean isProcessingAntispofing = false;

    // Liveness Security
    private final List<RectF> positionHistory = new ArrayList<>();
    private static final int HISTORY_SIZE = 15;
    private boolean isRealFace = false;
    private float initialFaceWidth = 0;

    enum Challenge { BLINK, SMILE, TURN_LEFT, TURN_RIGHT, LOOK_UP, ZOOM_IN }
    private final List<Challenge> challengeQueue = new ArrayList<>();
    private int challengeIndex = 0;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
                else Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show();
            });

    private String aktivitas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camerax);

        previewView = findViewById(R.id.cameraPreview);
        faceOverlay = findViewById(R.id.faceOverlay);
        txtChallenge = findViewById(R.id.txtChallenge);
        findViewById(R.id.ivBackCamera).setOnClickListener(v -> finish());
        findViewById(R.id.flipCamera).setOnClickListener(v -> toggleCamera());
        findViewById(R.id.toggleFlash).setOnClickListener(v -> toggleFlashMode());
        
        aktivitas = getIntent().getStringExtra("aktivitas");

        faceRecognizer = new FaceRecognizer(this);
        initFaceDetector();
        loadReferenceFace();

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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetState();
        
        // Small delay to ensure previous activity has released camera hardware
        previewView.postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA);
            }
        }, 500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCamera();
    }

    private void stopCamera() {
        try {
            if (cameraProvider != null) {
                cameraProvider.unbindAll();
            }
        } catch (Exception e) {
            Log.e("CameraxActivity", "Error unbinding camera", e);
        }
        if (analysisExecutor != null) {
            analysisExecutor.shutdown(); // Non-aggressive shutdown
            analysisExecutor = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (faceDetector != null) faceDetector.close();
        if (faceRecognizer != null) faceRecognizer.close();
        super.onDestroy();
    }

    private void initFaceDetector() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.15f)
                .build();
        faceDetector = FaceDetection.getClient(options);
    }

    private void loadReferenceFace() {
        isSystemReady = false;
        runOnUiThread(() -> txtChallenge.setText("Menyiapkan Sistem..."));
        
        Bitmap refBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.contohfile);
        if (refBitmap == null) {
            isSystemReady = true; // Fallback agar preview tetap jalan
            runOnUiThread(() -> txtChallenge.setText("⚠️ Mode Tanpa Referensi (File Tidak Ada)"));
            return;
        }

        InputImage image = InputImage.fromBitmap(refBitmap, 0);
        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    if (!faces.isEmpty()) {
                        referenceEmbedding = faceRecognizer.getEmbedding(refBitmap, faces.get(0).getBoundingBox());
                        if (referenceEmbedding != null) {
                            isSystemReady = true;
                            runOnUiThread(() -> txtChallenge.setText("Sistem Siap\nSilahkan posisikan wajah"));
                        } else {
                            isSystemReady = true; // Fallback
                            runOnUiThread(() -> txtChallenge.setText("⚠️ Mode Liveness Saja (AI Error)"));
                        }
                    } else {
                        isSystemReady = true; // Fallback
                        runOnUiThread(() -> txtChallenge.setText("⚠️ Mode Liveness Saja (Wajah Tidak Terdeteksi)"));
                    }
                })
                .addOnFailureListener(e -> {
                    isSystemReady = true; // Fallback
                    Log.e("CameraxActivity", "Face detection failed on reference", e);
                });
    }

    private void generateChallengeQueue() {
        Challenge[] pool = Challenge.values();
        List<Challenge> temp = new ArrayList<>();
        Random r = new Random();

        // Selalu wajibkan BLINK dan ZOOM_IN
        temp.add(Challenge.BLINK);
        temp.add(Challenge.ZOOM_IN);

        while (temp.size() < 3) {
            Challenge c = pool[r.nextInt(pool.length)];
            if (!temp.contains(c)) temp.add(c);
        }
        challengeQueue.clear();
        challengeQueue.addAll(temp);
        challengeIndex = 0;
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

    private void startCamera() {
        if (isFinishing() || isDestroyed()) return;

        // Ensure we wait for previewView to be measured
        previewView.post(() -> {
            ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
            future.addListener(() -> {
                try {
                    if (isFinishing() || isDestroyed()) return;

                    cameraProvider = future.get();
                    cameraProvider.unbindAll();

                    Preview preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());

                    imageCapture = new ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build();

                    analysisExecutor = Executors.newSingleThreadExecutor();

                    int rotation = Surface.ROTATION_0;
                    if (previewView.getDisplay() != null) {
                        rotation = previewView.getDisplay().getRotation();
                    }

                    ImageAnalysis analysis = new ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setTargetRotation(rotation)
                            .build();

                    analysis.setAnalyzer(analysisExecutor, this::analyzeFrame);

                    camera = cameraProvider.bindToLifecycle(this,
                            new CameraSelector.Builder().requireLensFacing(lensFacing).build(),
                            preview, imageCapture, analysis);

                    updateFlashButtonVisibility();

                    Log.d("CameraxActivity", "Camera successfully bound to lifecycle");

                } catch (Exception e) {
                    Log.e("CameraxActivity", "Camera start failed", e);
                }
            }, ContextCompat.getMainExecutor(this));
        });
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyzeFrame(ImageProxy proxy) {
        if (isFinishing() || isDestroyed()) {
            proxy.close();
            return;
        }

        Image img = proxy.getImage();
        if (img == null) {
            proxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(img, proxy.getImageInfo().getRotationDegrees());
        faceDetector.process(image)
                .addOnSuccessListener(analysisExecutor, faces -> {
                    // Reuse handleFaces but avoid double toBitmap conversion
                    handleFaces(faces, image, proxy);
                })
                .addOnFailureListener(analysisExecutor, e -> proxy.close())
                .addOnCompleteListener(analysisExecutor, t -> {
                    // proxy closed in handleFaces or onFailure
                });
    }

    private void handleFaces(List<Face> faces, InputImage image, ImageProxy proxy) {
        if (faces.isEmpty()) {
            proxy.close();
            resetChallengeOnly();
            return;
        }

        Face face = faces.get(0);
        Rect rect = face.getBoundingBox();
        RectF faceNorm = new RectF(rect.left / (float) image.getWidth(), rect.top / (float) image.getHeight(),
                rect.right / (float) image.getWidth(), rect.bottom / (float) image.getHeight());

        if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            // mirror kamera depan
            float left = 1f - faceNorm.right;
            float right = 1f - faceNorm.left;
            faceNorm.left = left;
            faceNorm.right = right;
        }

        if (initialFaceWidth == 0) {
            initialFaceWidth = faceNorm.width();
        }
        
        faceInsideFrame = faceOverlay.getFrameNormalized().contains(faceNorm.centerX(), faceNorm.centerY());

        if (!faceInsideFrame) {
            proxy.close();
            runOnUiThread(() -> {
                txtChallenge.setText("⚠️ Posisikan wajah di dalam oval");
                faceOverlay.setFaceData(false, faceNorm, "");
            });
            resetChallengeOnly();
            return;
        }

        // Anti-Spoofing 1: Check for natural micro-movements
        checkFaceStillness(faceNorm);
        if (!isRealFace && positionHistory.size() >= HISTORY_SIZE) {
            proxy.close();
            runOnUiThread(() -> txtChallenge.setText("⚠️ Pastikan Wajah Asli\n(Gerakkan wajah sedikit)"));
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
                                faceOverlay.setFaceData(true, faceNorm, "Palsu");
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

        if (!livenessPassed) {
            if (challengeIndex >= challengeQueue.size()) {
                livenessPassed = true;
            } else {
                proxy.close();
                runOnUiThread(() -> {
                    String instruction = getChallengeText(challengeQueue.get(challengeIndex));
                    txtChallenge.setText("Tahap 1: Liveness Check\n" + instruction);
                    faceOverlay.setFaceData(true, faceNorm, "Mendeteksi Gerakan...");
                });
                detectChallenge(face, faceNorm);
                return;
            }
        }

        // Verification Stage: Need mirrored bitmap for Face Recognition matching
        Bitmap mirroredFaceBmp = getFaceBitmap(proxy, face, true);
        proxy.close();

        if (mirroredFaceBmp == null) return;
        
        float[] currentEmbedding = faceRecognizer.getEmbedding(mirroredFaceBmp, null); // passing null boundingBox as it's already cropped
        if (currentEmbedding == null) return;

        float score = faceRecognizer.getSimilarityScore(currentEmbedding, referenceEmbedding);
        boolean isRecognized = score > 0.7f;
        String percentageText = String.format(Locale.US, "%.0f%%", score * 100);

        runOnUiThread(() -> faceOverlay.setFaceData(true, faceNorm, percentageText));

        if (isRecognized && !isCapturing) {
            // Anti-Spoofing 3: Skin Authenticity Check (Optional)
            if (!validateSkinIntegrity(mirroredFaceBmp, null)) {
                runOnUiThread(() -> txtChallenge.setText("Tahap 2: Verifikasi Wajah\n⚠️ Gunakan Wajah Nyata (Bukan Foto/Layar)"));
                return;
            }

            isCapturing = true;
            runOnUiThread(() -> {
                txtChallenge.setText("✔ Wajah Sesuai! (" + percentageText + ")\nMohon tunggu, mengambil foto...");
                takePicture();
            });
        }
    }

    private void detectChallenge(Face face, RectF faceNorm) {
        if (challengeIndex >= challengeQueue.size()) return;
        Challenge c = challengeQueue.get(challengeIndex);
        boolean passed = false;
        float currentProgress = 0f;
        float eulerY = face.getHeadEulerAngleY();
        float eulerX = face.getHeadEulerAngleX();

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
                passed = eulerX > 12;
                currentProgress = eulerX / 12f;
                break;
            case ZOOM_IN:
                float growth = faceNorm.width() / initialFaceWidth;
                passed = growth > 1.25f;
                currentProgress = Math.min(1f, (growth - 1f) / 0.25f);
                break;
        }

        final float finalProgress = Math.min(1f, Math.max(0f, currentProgress));
        runOnUiThread(() -> faceOverlay.setProgress(finalProgress));

        if (passed) {
            challengeIndex++;
            runOnUiThread(() -> faceOverlay.setProgress(0f));
            if (challengeIndex < challengeQueue.size()) {
                runOnUiThread(() -> txtChallenge.setText("Tahap 1: Liveness Check\n" + getChallengeText(challengeQueue.get(challengeIndex))));
            }
        }
    }

    private boolean validate3DDepth(Face face, String direction) {
        FaceLandmark nose = face.getLandmark(FaceLandmark.NOSE_BASE);
        FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
        FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);

        if (nose == null || leftEye == null || rightEye == null) return false;

        float distLeft = Math.abs(nose.getPosition().x - leftEye.getPosition().x);
        float distRight = Math.abs(nose.getPosition().x - rightEye.getPosition().x);
        
        // Anti-Spoofing Monitor: Strict Geometric Integrity
        // Layar 2D saat dimiringkan akan mempertahankan rasio linier
        // Wajah 3D akan mengalami distorsi kedalaman yang non-linier
        float totalDist = distLeft + distRight;
        float asymmetryRatio = Math.max(distLeft, distRight) / Math.min(distLeft, distRight);

        if (direction.equals("left")) return distRight > distLeft * 2.0f && asymmetryRatio > 2.0f;
        if (direction.equals("right")) return distLeft > distRight * 2.0f && asymmetryRatio > 2.0f;
        
        return true;
    }

    private boolean validateSkinIntegrity(Bitmap fullBitmap, Rect faceRect) {
        int centerX;
        int centerY;
        
        if (faceRect == null) {
            centerX = fullBitmap.getWidth() / 2;
            centerY = fullBitmap.getHeight() / 2;
        } else {
            centerX = faceRect.centerX();
            centerY = faceRect.centerY();
        }
        
        int sampleSize = 20;

        int skinPixels = 0;
        int totalPixels = 0;

        for (int y = centerY - sampleSize; y < centerY + sampleSize; y++) {
            for (int x = centerX - sampleSize; x < centerX + sampleSize; x++) {
                if (x < 0 || y < 0 || x >= fullBitmap.getWidth() || y >= fullBitmap.getHeight()) continue;

                int pixel = fullBitmap.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                double cb = 128 + (-0.168736 * r - 0.331264 * g + 0.5 * b);
                // Convert to YCbCr
                double cbVal = 128 + (-0.168736 * r - 0.331264 * g + 0.5 * b);
                double crVal = 128 + (0.5 * r - 0.418688 * g - 0.081312 * b);

                if (cbVal >= 77 && cbVal <= 127 && crVal >= 133 && crVal <= 173) {
                    // Cek spektrum biru (Anti-Monitor)
                    // Kulit manusia asli tidak memantulkan cahaya biru lebih tinggi dari merah/hijau
                    if (!(b > r + 20 || b > g + 20)) {
                        skinPixels++;
                    }
                }
                
                totalPixels++;
            }
        }

        if (totalPixels == 0) return false;
        float skinRatio = (float) skinPixels / totalPixels;
        
        // Anti-Glow Layar HP
        int averageBrightness = 0;
        for (int y = centerY - 5; y < centerY + 5; y++) {
            for (int x = centerX - 5; x < centerX + 5; x++) {
                int p = fullBitmap.getPixel(x, y);
                averageBrightness += (Color.red(p) + Color.green(p) + Color.blue(p)) / 3;
            }
        }
        averageBrightness /= 100;
        
        // Monitor/LCD memancarkan cahaya (self-emissive) yang merusak konsistensi warna kulit
        // Kita perketat skinRatio menjadi 85% untuk wajah asli
        return skinRatio > 0.85f && averageBrightness < 235;
    }

    private void resetState() {
        challengeIndex = 0;
        livenessPassed = false;
        isCapturing = false;
        positionHistory.clear();
        isRealFace = false;
        initialFaceWidth = 0;
        generateChallengeQueue();
        runOnUiThread(() -> {
            faceOverlay.setFaceData(false, null, "");
            txtChallenge.setText("Arahkan wajah ke frame");
        });
    }

    private void checkFaceStillness(RectF currentPos) {
        positionHistory.add(new RectF(currentPos));
        if (positionHistory.size() > HISTORY_SIZE) positionHistory.remove(0);

        if (positionHistory.size() == HISTORY_SIZE) {
            float varX = 0, varY = 0;
            RectF first = positionHistory.get(0);
            for (RectF p : positionHistory) {
                varX += Math.abs(p.centerX() - first.centerX());
                varY += Math.abs(p.centerY() - first.centerY());
            }
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

    private void resetChallengeOnly() {
        livenessPassed = false;
        challengeIndex = 0;
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
            
            ImageButton flashBtn = findViewById(R.id.toggleFlash);
            if (flashBtn != null) {
                flashBtn.setImageResource(!isTorchOn ? R.drawable.flashof : R.drawable.flash);
            }
        }
    }

    private void updateFlashButtonVisibility() {
        View flashBtn = findViewById(R.id.toggleFlash);
        if (flashBtn != null) {
            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                flashBtn.setVisibility(View.VISIBLE);
            } else {
                flashBtn.setVisibility(View.GONE);
            }
        }
    }

    private void takePicture() {
        if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
            camera.getCameraControl().enableTorch(true);
        }

        Dialog dialogproses = new Dialog(this, R.style.DialogStyle);
        dialogproses.setContentView(R.layout.view_proses);
        dialogproses.setCancelable(false);
        dialogproses.show();

        String fileName = System.currentTimeMillis() + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/eabsensi");

        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values).build();

        imageCapture.takePicture(options, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                if (camera != null) camera.getCameraControl().enableTorch(false);
                dialogproses.dismiss();
                if (output.getSavedUri() != null) kirimHasil(output.getSavedUri().toString());
                else kirimHasil(fileName);
            }
            @Override
            public void onError(@NonNull ImageCaptureException e) {
                dialogproses.dismiss();
                Toast.makeText(CameraxActivity.this, "Gagal mengambil foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                isCapturing = false;
            }
        });
    }

    private void kirimHasil(String fileName) {
        Intent i;
        if ("kehadiran".equals(aktivitas)) i = new Intent(this, AbsensiKehadiranActivity.class);
        else if ("tugaslapangan".equals(aktivitas)) i = new Intent(this, TugasLapanganFinalActivity.class);
        else if ("perjalanandinas".equals(aktivitas)) i = new Intent(this, PerjalananDinasFinalActivity.class);
        else if ("izincuti".equals(aktivitas)) i = new Intent(this, IzinCutiFinalActivity.class);
        else if ("izinkp".equals(aktivitas)) i = new Intent(this, KeperluanPribadiFinalActivity.class);
        else if ("izinsakit".equals(aktivitas)) i = new Intent(this, IzinSakitFinalActivity.class);
        else if ("kehadiransift".equals(aktivitas)) i = new Intent(this, AbsenShiftActivity.class);
        else if ("shiftizinsakit".equals(aktivitas)) i = new Intent(this, IzinSakitShiftFinalActivity.class);
        else if ("shiftizinkp".equals(aktivitas)) i = new Intent(this, KeperluanPribadiShiftFinalActivity.class);
        else if ("shiftizincuti".equals(aktivitas)) i = new Intent(this, IzinCutiShiftFinalActivity.class);
        else {
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
