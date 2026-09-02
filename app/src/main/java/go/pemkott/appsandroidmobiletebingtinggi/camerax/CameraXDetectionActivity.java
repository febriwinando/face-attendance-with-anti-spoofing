package go.pemkott.appsandroidmobiletebingtinggi.camerax;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
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
import go.pemkott.appsandroidmobiletebingtinggi.izinsift.izinsiftcuti.IzinCutiSiftFinalActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izinsift.izinsiftpribadi.KeperluanPribadiSiftActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izinsift.izinsiftsakit.IzinSakitSiftFinalActivity;
import go.pemkott.appsandroidmobiletebingtinggi.kehadiran.AbsensiKehadiranActivity;
import go.pemkott.appsandroidmobiletebingtinggi.kehadiransift.AbsenSiftActivity;

public class CameraXDetectionActivity extends AppCompatActivity {

    // ================= UI =================
    private PreviewView previewView;
    private FaceOverlayView faceOverlay;
    private ImageButton capture;
    private TextView txtChallenge;

    // ================= CAMERA =================
    private ImageCapture imageCapture;
    private static final int CAMERA_FACING = CameraSelector.LENS_FACING_FRONT;

    // ================= FACE =================
    private FaceDetector faceDetector;
    private FaceRecognizer faceRecognizer;
    private float[] referenceEmbedding;
    private boolean faceInsideFrame = false;
    private boolean isCapturing = false;
    private boolean livenessPassed = false;

    // ================= CHALLENGE =================
    enum Challenge {
        BLINK, SMILE, TURN_LEFT, TURN_RIGHT, LOOK_UP, LOOK_DOWN
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

    // ================= LIFECYCLE =================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerax);

        previewView = findViewById(R.id.cameraPreview);
        faceOverlay = findViewById(R.id.faceOverlay);
        faceOverlay.setVisibility(View.VISIBLE);
        capture = findViewById(R.id.capture);
        txtChallenge = findViewById(R.id.txtChallenge);
        txtChallenge.setVisibility(View.VISIBLE);

        aktivitas = getIntent().getStringExtra("aktivitas");

        faceRecognizer = new FaceRecognizer(this);
        initFaceDetector();
        loadReferenceFace();

        capture.setEnabled(false);
        generateChallengeQueue();

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

    // ================= FACE DETECTOR =================
    private void initFaceDetector() {
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .setMinFaceSize(0.15f)
                        .build();

        faceDetector = FaceDetection.getClient(options);
    }

    private void loadReferenceFace() {
        if (faceRecognizer == null) return;

        Bitmap refBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.contohfile);
        if (refBitmap == null) {
            runOnUiThread(() -> Toast.makeText(this, "FATAL: File drawable/contohfile tidak ditemukan!", Toast.LENGTH_LONG).show());
            return;
        }

        InputImage image = InputImage.fromBitmap(refBitmap, 0);
        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    if (!faces.isEmpty()) {
                        referenceEmbedding = faceRecognizer.getEmbedding(refBitmap, faces.get(0).getBoundingBox());
                        if (referenceEmbedding != null) {
                            Log.d("CameraXDetection", "Reference face loaded successfully");
                        } else {
                            runOnUiThread(() -> Toast.makeText(this, "Gagal mengekstrak ciri wajah referensi. Pastikan model AI ada di assets.", Toast.LENGTH_LONG).show());
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Wajah tidak terdeteksi pada file referensi (contohfile.jpeg)", Toast.LENGTH_LONG).show());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CameraXDetection", "Face detection failed on reference", e);
                });
    }

    // ================= CHALLENGE =================
    private void generateChallengeQueue() {

        Challenge[] pool = Challenge.values();
        List<Challenge> temp = new ArrayList<>();

        Random r = new Random();

        while (temp.isEmpty()) {
            Challenge c = pool[r.nextInt(pool.length)];
            if (!temp.contains(c)) temp.add(c);
        }

        challengeQueue.clear();

        // masing-masing 1 kali
        for (Challenge c : temp) {
            challengeQueue.add(c);
        }

        Collections.shuffle(challengeQueue);

        challengeIndex = 0;
        showCurrentChallenge();
    }

    private void showCurrentChallenge() {
        if (challengeQueue.isEmpty()) {
            txtChallenge.setText("Menyiapkan pemeriksaan...");
            return;
        }
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
            case LOOK_DOWN: return "Tundukkan kepala";
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

                ImageAnalysis analysis =
                        new ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                analysis.setAnalyzer(
                        Executors.newSingleThreadExecutor(),
                        this::analyzeFrame
                );

                provider.unbindAll();
                provider.bindToLifecycle(
                        this,
                        new CameraSelector.Builder()
                                .requireLensFacing(CAMERA_FACING)
                                .build(),
                        preview,
                        imageCapture,
                        analysis
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // ================= ANALYSIS =================
    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyzeFrame(ImageProxy proxy) {
        Bitmap frameBitmap = faceRecognizer.toBitmap(proxy);
        if (frameBitmap == null) {
            proxy.close();
            return;
        }

        InputImage image = InputImage.fromBitmap(frameBitmap, 0);

        faceDetector.process(image)
                .addOnSuccessListener(faces -> handleFaces(faces, image, frameBitmap))
                .addOnCompleteListener(t -> proxy.close());
    }

    private void handleFaces(List<Face> faces, InputImage image, Bitmap frameBitmap) {

        if (faces.isEmpty()) {
            resetState();
            return;
        }

        Face face = faces.get(0);
        RectF faceNorm = normalize(face.getBoundingBox(), image);

        faceInsideFrame =
                faceOverlay.getFrameNormalized()
                        .contains(faceNorm.centerX(), faceNorm.centerY());

        if (!faceInsideFrame) {
            runOnUiThread(() -> {
                txtChallenge.setText("⚠️ Posisikan wajah di dalam oval");
                faceOverlay.setFaceData(false, faceNorm, "");
            });
            resetChallengeOnly();
            return;
        }

        // TAHAP 1: Validasi Gerakan (Liveness)
        if (!livenessPassed) {
            if (challengeIndex >= challengeQueue.size()) {
                livenessPassed = true;
            } else {
                runOnUiThread(() -> {
                    if (challengeIndex < challengeQueue.size()) {
                        String instruction = getChallengeText(challengeQueue.get(challengeIndex));
                        txtChallenge.setText("Tahap 1: Liveness Check\n" + instruction);
                        faceOverlay.setFaceData(true, faceNorm, "Mendeteksi Gerakan...");
                    }
                });
                detectChallenge(face);
                return;
            }
        }

        // TAHAP 2: Deteksi Kesesuaian Wajah (Recognition)
        float[] currentEmbedding = faceRecognizer.getEmbedding(frameBitmap, face.getBoundingBox());
        float score = 0f;
        boolean isRecognized = false;

        if (referenceEmbedding != null && currentEmbedding != null) {
            score = faceRecognizer.getSimilarityScore(currentEmbedding, referenceEmbedding);
            isRecognized = score > 0.7f;
        } else if (referenceEmbedding == null) {
            isRecognized = true; 
        }

        final float finalScore = score;
        String percentageText = String.format(java.util.Locale.US, "%.0f%%", score * 100);

        runOnUiThread(() -> faceOverlay.setFaceData(true, faceNorm, percentageText));

        if (!isRecognized) {
            runOnUiThread(() -> txtChallenge.setText("Tahap 2: Verifikasi Wajah\n❌ Wajah tidak cocok (" + percentageText + ")"));
            return;
        }

        // TAHAP 3: Capture Otomatis
        if (!isCapturing) {
            isCapturing = true;
            runOnUiThread(() -> {
                txtChallenge.setText("✔ Wajah Sesuai! (" + percentageText + ")\nMohon tunggu, mengambil foto...");
                takePicture();
            });
        }
    }

    private void detectChallenge(Face face) {

        Challenge c = challengeQueue.get(challengeIndex);
        boolean passed = false;

        float eulerY = face.getHeadEulerAngleY(); // kiri-kanan
        float eulerX = face.getHeadEulerAngleX(); // atas-bawah

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

            case TURN_LEFT:
                passed = eulerY < -15;
                break;

            case TURN_RIGHT:
                passed = eulerY > 15;
                break;

            case LOOK_UP:
                passed = eulerX > 10;
                break;

            case LOOK_DOWN:
                passed = eulerX < -10;
                break;
        }

        if (passed) advanceChallenge();
    }

    private void advanceChallenge() {
        challengeIndex++;

        if (challengeIndex >= challengeQueue.size()) {
            // Auto-capture handled in handleFaces
            return;
        }

        runOnUiThread(this::showCurrentChallenge);
    }

    private void resetState() {
        challengeIndex = 0;
        livenessPassed = false;
        isCapturing = false;
        runOnUiThread(() -> {
            capture.setEnabled(false);
            capture.setAlpha(0.5f);
            faceOverlay.setFaceData(false, null, "");
            txtChallenge.setText("Arahkan wajah ke frame");
        });
    }

    private void resetChallengeOnly() {
        challengeIndex = 0;
        runOnUiThread(() -> {
            capture.setEnabled(false);
            capture.setAlpha(0.5f);
        });
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

        Dialog dialogproses = new Dialog(CameraXDetectionActivity.this, R.style.DialogStyle);
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
                        dialogproses.dismiss();
                        kirimHasil(fileName);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        Toast.makeText(CameraXDetectionActivity.this,
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
            i = new Intent(this, AbsenSiftActivity.class);
        } else if ("shiftizinsakit".equals(aktivitas)) {
            i = new Intent(this, IzinSakitSiftFinalActivity.class);
        } else if ("shiftizinkp".equals(aktivitas)) {
            i = new Intent(this, KeperluanPribadiSiftActivity.class);
        } else if ("shiftizincuti".equals(aktivitas)) {
            i = new Intent(this, IzinCutiSiftFinalActivity.class);
        } else {
            i = new Intent();
            i.putExtra("namafile", fileName);
            setResult(RESULT_OK, i);
            finish();
            return;
        }

        i.putExtra("namafile", fileName);
        startActivity(i);
        finish();
    }
}
