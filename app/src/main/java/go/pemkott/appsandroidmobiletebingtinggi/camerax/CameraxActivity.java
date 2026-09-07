package go.pemkott.appsandroidmobiletebingtinggi.camerax;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.WindowManager;
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

public class CameraxActivity extends AppCompatActivity {

    private PreviewView previewView;
    private FaceOverlayView faceOverlay;
    private TextView txtChallenge;

    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private FaceDetector faceDetector;
    private FaceRecognizer faceRecognizer;
    private ExecutorService analysisExecutor;
    
    private float[] referenceEmbedding;
    private boolean faceInsideFrame = false;
    private boolean isCapturing = false;
    private boolean livenessPassed = false;
    private boolean isSystemReady = false;

    enum Challenge { BLINK, SMILE, TURN_LEFT, TURN_RIGHT, LOOK_UP, LOOK_DOWN }
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
        
        aktivitas = getIntent().getStringExtra("aktivitas");

        faceRecognizer = new FaceRecognizer(this);
        initFaceDetector();
        loadReferenceFace();

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
        while (temp.size() < 1) {
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
            case LOOK_DOWN: return "Tundukkan kepala";
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

                    int rotation = android.view.Surface.ROTATION_0;
                    if (previewView.getDisplay() != null) {
                        rotation = previewView.getDisplay().getRotation();
                    }

                    ImageAnalysis analysis = new ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setTargetRotation(rotation)
                            .build();

                    analysis.setAnalyzer(analysisExecutor, this::analyzeFrame);

                    cameraProvider.bindToLifecycle(this,
                            new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build(),
                            preview, imageCapture, analysis);

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
                .addOnSuccessListener(faces -> {
                    // Tahap 1 (Gerakan) tidak butuh frameBitmap, jadi hemat memori
                    if (livenessPassed && isSystemReady && referenceEmbedding != null) {
                        Bitmap frameBitmap = faceRecognizer.toBitmap(proxy);
                        handleFaces(faces, image, frameBitmap);
                    } else {
                        handleFaces(faces, image, null);
                    }
                })
                .addOnCompleteListener(t -> proxy.close());
    }

    private void handleFaces(List<Face> faces, InputImage image, Bitmap frameBitmap) {
        if (faces.isEmpty()) {
            resetChallengeOnly();
            return;
        }

        Face face = faces.get(0);
        android.graphics.Rect rect = face.getBoundingBox();
        RectF faceNorm = new RectF(rect.left / (float) image.getWidth(), rect.top / (float) image.getHeight(),
                rect.right / (float) image.getWidth(), rect.bottom / (float) image.getHeight());
        
        faceInsideFrame = faceOverlay.getFrameNormalized().contains(faceNorm.centerX(), faceNorm.centerY());

        if (!faceInsideFrame) {
            runOnUiThread(() -> {
                txtChallenge.setText("⚠️ Posisikan wajah di dalam oval");
                faceOverlay.setFaceData(false, faceNorm, "");
            });
            resetChallengeOnly();
            return;
        }

        if (!livenessPassed) {
            if (challengeIndex >= challengeQueue.size()) {
                livenessPassed = true;
            } else {
                runOnUiThread(() -> {
                    String instruction = getChallengeText(challengeQueue.get(challengeIndex));
                    txtChallenge.setText("Tahap 1: Liveness Check\n" + instruction);
                    faceOverlay.setFaceData(true, faceNorm, "Mendeteksi Gerakan...");
                });
                detectChallenge(face);
                return;
            }
        }

        if (frameBitmap == null) return;
        
        float[] currentEmbedding = faceRecognizer.getEmbedding(frameBitmap, face.getBoundingBox());
        if (currentEmbedding == null) return;

        float score = faceRecognizer.getSimilarityScore(currentEmbedding, referenceEmbedding);
        boolean isRecognized = score > 0.7f;
        String percentageText = String.format(java.util.Locale.US, "%.0f%%", score * 100);

        runOnUiThread(() -> faceOverlay.setFaceData(true, faceNorm, percentageText));

        if (!isRecognized) {
            runOnUiThread(() -> txtChallenge.setText("Tahap 2: Verifikasi Wajah\n❌ Wajah tidak cocok (" + percentageText + ")"));
            return;
        }

        if (!isCapturing) {
            isCapturing = true;
            runOnUiThread(() -> {
                txtChallenge.setText("✔ Wajah Sesuai! (" + percentageText + ")\nMohon tunggu, mengambil foto...");
                takePicture();
            });
        }
    }

    private void detectChallenge(Face face) {
        if (challengeIndex >= challengeQueue.size()) return;
        Challenge c = challengeQueue.get(challengeIndex);
        boolean passed = false;
        float eulerY = face.getHeadEulerAngleY();
        float eulerX = face.getHeadEulerAngleX();

        switch (c) {
            case BLINK:
                Float l = face.getLeftEyeOpenProbability();
                Float r = face.getRightEyeOpenProbability();
                passed = l != null && r != null && l < 0.3f && r < 0.3f;
                break;
            case SMILE:
                Float s = face.getSmilingProbability();
                passed = s != null && s > 0.6f;
                break;
            case TURN_LEFT: passed = eulerY < -15; break;
            case TURN_RIGHT: passed = eulerY > 15; break;
            case LOOK_UP: passed = eulerX > 10; break;
            case LOOK_DOWN: passed = eulerX < -10; break;
        }
        if (passed) {
            challengeIndex++;
            if (challengeIndex < challengeQueue.size()) {
                runOnUiThread(() -> txtChallenge.setText("Tahap 1: Liveness Check\n" + getChallengeText(challengeQueue.get(challengeIndex))));
            }
        }
    }

    private void resetState() {
        challengeIndex = 0;
        livenessPassed = false;
        isCapturing = false;
        generateChallengeQueue();
        runOnUiThread(() -> {
            faceOverlay.setFaceData(false, null, "");
            txtChallenge.setText("Arahkan wajah ke frame");
        });
    }

    private void resetChallengeOnly() {
        livenessPassed = false;
        challengeIndex = 0;
    }

    private void takePicture() {
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
