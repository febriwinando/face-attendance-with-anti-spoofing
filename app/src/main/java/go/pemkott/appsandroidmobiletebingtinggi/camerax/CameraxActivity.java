package go.pemkott.appsandroidmobiletebingtinggi.camerax;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.Trace;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
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
    private boolean isSystemReady = false;
    private boolean isAnalyzing = false;
    private int nyataStreak = 0;

    // ================= CAPTURE STAGES =================
    private enum CaptureStage {
        ANTI_SPOOFING_INIT, // Tahap 1
        CHALLENGES,         // Tahap 2
        ANTI_SPOOFING_FINAL, // Tahap 3
        RECOGNITION,        // Tahap 4
        CAPTURE,            // Tahap 5
        DONE
    }
    private CaptureStage currentStage = CaptureStage.ANTI_SPOOFING_INIT;

    // Antispofing TFLite (Metode DetectorActivity)
    private Classifier antispofingDetector;
    private static final int TF_OD_API_INPUT_SIZE = 224;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "mask_detector.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/mobile_label.txt";
    private boolean isProcessingAntispofing = false;

    // Buffers for optimized anti-spoofing processing (DetectorActivity style)
    private Bitmap rgbFrameBitmap = null;
    private Bitmap faceBmp = null;
    private int[] rgbBytes = null;
    private byte[] yBuffer = null;
    private byte[] uBuffer = null;
    private byte[] vBuffer = null;

    enum Challenge { BLINK, SMILE, TURN_LEFT, TURN_RIGHT, LOOK_UP }
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

        // Cukup 1 tantangan saja agar proses cepat
        Challenge c = pool[r.nextInt(pool.length)];
        temp.add(c);
        
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
                            .setTargetResolution(new Size(640, 480))
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
        InputImage image = InputImage.fromMediaImage(img, proxy.getImageInfo().getRotationDegrees());
        faceDetector.process(image)
                .addOnSuccessListener(analysisExecutor, faces -> {
                    handleFaces(faces, image, proxy);
                })
                .addOnFailureListener(analysisExecutor, e -> {
                    proxy.close();
                })
                .addOnCompleteListener(analysisExecutor, t -> {
                    isAnalyzing = false;
                });
    }

    private void handleFaces(List<Face> faces, InputImage image, ImageProxy proxy) {
        if (faces.isEmpty()) {
            proxy.close();
            runOnUiThread(() -> {
                faceOverlay.setFaceInside(false);
                faceOverlay.setProgress(0f);
            });
            return;
        }

        Face face = faces.get(0);
        Rect rect = face.getBoundingBox();
        
        // ML Kit sudah menangani rotasi secara internal melalui InputImage.
        // Kita hanya perlu melakukan normalisasi sederhana 0-1.
        RectF projectedFace = new RectF(
                rect.left / (float) image.getWidth(),
                rect.top / (float) image.getHeight(),
                rect.right / (float) image.getWidth(),
                rect.bottom / (float) image.getHeight()
        );

        // Mirroring tetap diperlukan untuk kamera depan
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
                                 statusText = "❌ PALSU (" + String.format(Locale.US, "%.0f%%", confidence*100) + ")";
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
        // State Machine Tahapan Capture (Sekuensial)
        switch (currentStage) {
            case ANTI_SPOOFING_INIT:
                // Sudah lewat streak di handleFaces, lanjut ke tantangan
                currentStage = CaptureStage.CHALLENGES;
                break;

            case CHALLENGES:
                if (challengeIndex >= challengeQueue.size()) {
                    currentStage = CaptureStage.ANTI_SPOOFING_FINAL;
                    nyataStreak = 0; // Reset streak untuk verifikasi akhir
                } else {
                    String instruction = getChallengeText(challengeQueue.get(challengeIndex));
                    txtChallenge.setText("Tahap 2: Liveness Check\n" + instruction);
                    detectChallenge(face, faceNorm);
                }
                break;

            case ANTI_SPOOFING_FINAL:
                // Sudah lewat streak di handleFaces, lanjut ke Recognition
                currentStage = CaptureStage.RECOGNITION;
                break;

            case RECOGNITION:
                // Gunakan bitmap wajah yang sudah stabil untuk recognition
                processRecognition(face, faceNorm);
                break;

            case CAPTURE:
                if (!isCapturing) {
                    isCapturing = true;
                    txtChallenge.setText("Tahap 5: Mengambil Foto...");
                    takePicture();
                }
                break;

            case DONE:
                break;
        }
    }

    private interface OnCaptureStepPassListener {
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

        canvas.drawColor(Color.BLACK); // Clear background
        canvas.drawBitmap(rgbFrameBitmap, matrix, new Paint(Paint.FILTER_BITMAP_FLAG));

        return faceBmp;
    }

    private void fillBuffer(ByteBuffer buffer, byte[] array) {
        buffer.rewind();
        int remaining = buffer.remaining();
        if (array.length < remaining) {
            // Should not happen if pre-allocated correctly, but for safety:
            return;
        }
        buffer.get(array, 0, remaining);
    }

    private boolean isImageSharp(Bitmap bitmap) {
        // Simple variance of Laplacian estimate (edge detection)
        // Helps filter out blurry photos or screen re-captures
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
        return variance > 5.0f; // Threshold for sharpness
    }

    private boolean detectMoirePattern(Bitmap bitmap) {
        // Moiré patterns create high-frequency repeating stripes
        // We detect this by looking for abnormal periodic contrast changes
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

                // High local contrast difference in tiny areas (pixel grid noise)
                int diffH = Math.abs(p1 - p2);
                int diffV = Math.abs(p1 - p3);

                if (diffH > 40 || diffV > 40) { // Repetitive sharp edges of pixels
                    moireScore++;
                }
            }
        }
        // If more than 8% of the face has screen-like pixel noise:
        return moireScore > (width * height * 0.08);
    }

    private void processRecognition(Face face, RectF faceNorm) {
        // AI Recognition menggunakan faceBmp yang sudah disiapkan oleh prepareFaceBitmap
        // Kita tidak perlu proxy lagi di sini karena faceBmp adalah buffer class level
        if (faceBmp == null) return;

        analysisExecutor.execute(() -> {
            float[] currentEmbedding = faceRecognizer.getEmbedding(faceBmp, face.getBoundingBox());
            if (currentEmbedding == null) return;

            float score = faceRecognizer.getSimilarityScore(currentEmbedding, referenceEmbedding);
            boolean isRecognized = score > 0.7f;
            String percentageText = String.format(Locale.US, "%.0f%%", score * 100);

            runOnUiThread(() -> {
                faceOverlay.setFaceData(true, faceNorm, percentageText);
                if (isRecognized) {
                    currentStage = CaptureStage.CAPTURE;
                    txtChallenge.setText("✔ Wajah Sesuai! (" + percentageText + ")");
                    // Recognition sukses, lanjut ke Capture otomatis di frame berikutnya
                } else {
                    txtChallenge.setText("Tahap 4: Verifikasi Wajah\n⚠️ Wajah Tidak Dikenali (" + percentageText + ")");
                }
            });
        });
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

        if (passed) {
            challengeIndex++;
            runOnUiThread(() -> faceOverlay.setProgress(0f));
            if (challengeIndex >= challengeQueue.size()) {
                currentStage = CaptureStage.ANTI_SPOOFING_FINAL;
                runOnUiThread(() -> txtChallenge.setText("Tahap 3: Verifikasi Akhir\n(Fokuskan wajah ke kamera)"));
            }
        }
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
                
                String finalFileName = (output.getSavedUri() != null) ? output.getSavedUri().toString() : fileName;
                runOnUiThread(() -> {
                    currentStage = CaptureStage.DONE;
                    kirimHasil(finalFileName);
                });
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
