package go.pemkott.appsandroidmobiletebingtinggi.camerax;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.RectF;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.dinasluarkantor.tugaslapangan.TugasLapanganFinalActivity;
import go.pemkott.appsandroidmobiletebingtinggi.kehadiran.AbsensiKehadiranActivity;

public class CameraxActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageButton capture, toggleFlash, flipCamera;

    private int cameraFacing = CameraSelector.LENS_FACING_FRONT;
    private boolean wajahTerdeteksi = false;

    private FaceDetector faceDetector;

    private String aktivitas;
    private FaceOverlayView faceOverlay;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera(cameraFacing);
                else Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerax);

        previewView = findViewById(R.id.cameraPreview);
        capture = findViewById(R.id.capture);
        toggleFlash = findViewById(R.id.toggleFlash);
        flipCamera = findViewById(R.id.flipCamera);
        faceOverlay = findViewById(R.id.faceOverlay);

        aktivitas = getIntent().getStringExtra("aktivitas");

        initFaceDetector();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startCamera(cameraFacing);
        }

        flipCamera.setOnClickListener(v -> {
            cameraFacing = (cameraFacing == CameraSelector.LENS_FACING_FRONT)
                    ? CameraSelector.LENS_FACING_BACK
                    : CameraSelector.LENS_FACING_FRONT;
            startCamera(cameraFacing);
        });
    }

    // ===================== FACE DETECTOR =====================
    private void initFaceDetector() {
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setMinFaceSize(0.15f)
                        .build();

        faceDetector = FaceDetection.getClient(options);
    }

    // ===================== CAMERA =====================
    private void startCamera(int facing) {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageCapture imageCapture =
                        new ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build();

                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setBackpressureStrategy(
                                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                imageAnalysis.setAnalyzer(
                        Executors.newSingleThreadExecutor(),
                        imageProxy -> {

                            @ExperimentalGetImage
                            android.media.Image mediaImage = imageProxy.getImage();

                            if (mediaImage != null) {
                                InputImage image =
                                        InputImage.fromMediaImage(
                                                mediaImage,
                                                imageProxy.getImageInfo().getRotationDegrees()
                                        );

                                faceDetector.process(image)
                                        .addOnSuccessListener(faces -> {
                                            boolean detected = !faces.isEmpty();
                                            wajahTerdeteksi = detected;

                                            runOnUiThread(() ->
                                                    faceOverlay.setWajahTerdeteksi(detected)
                                            );
                                        })
                                        .addOnCompleteListener(task -> imageProxy.close());

//                                faceDetector.process(image)
//                                        .addOnSuccessListener(faces ->
//                                                wajahTerdeteksi = !faces.isEmpty()
//                                        )
//                                        .addOnCompleteListener(task ->
//                                                imageProxy.close()
//                                        );
                            } else {
                                imageProxy.close();
                            }
                        }
                );

                CameraSelector selector =
                        new CameraSelector.Builder()
                                .requireLensFacing(facing)
                                .build();

                provider.unbindAll();
                Camera camera =
                        provider.bindToLifecycle(
                                this,
                                selector,
                                preview,
                                imageCapture,
                                imageAnalysis
                        );

                capture.setOnClickListener(v -> {
                    if (!wajahTerdeteksi) {
                        Toast.makeText(
                                this,
                                "Wajah tidak terdeteksi",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }
                    takePicture(imageCapture);
                });

                toggleFlash.setOnClickListener(v -> toggleFlash(camera));

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // ===================== CAPTURE =====================
    private void takePicture(ImageCapture imageCapture) {

        String fileName = System.currentTimeMillis() + ".jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        values.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/eabsensi"
        );

        ImageCapture.OutputFileOptions options =
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                ).build();

        imageCapture.takePicture(
                options,
                Executors.newSingleThreadExecutor(),
                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(
                            @NonNull ImageCapture.OutputFileResults output) {

                        runOnUiThread(() -> kirimHasil(fileName));
                    }

                    @Override
                    public void onError(
                            @NonNull ImageCaptureException e) {

                        runOnUiThread(() ->
                                Toast.makeText(
                                        CameraxActivity.this,
                                        "Gagal: " + e.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    }
                }
        );
    }

    // ===================== RESULT =====================
    private void kirimHasil(String fileName) {
        if ("kehadiran".equals(aktivitas)) {
            Intent i = new Intent(this, AbsensiKehadiranActivity.class);
            i.putExtra("namafile", fileName);
            startActivity(i);
        } else if ("tugaslapangan".equals(aktivitas)) {
            Intent i = new Intent(this, TugasLapanganFinalActivity.class);
            i.putExtra("namafile", fileName);
            startActivity(i);
        } else {
            Intent result = new Intent();
            result.putExtra("namafile", fileName);
            setResult(RESULT_OK, result);
        }
        finish();
    }

    // ===================== FLASH =====================
    private void toggleFlash(Camera camera) {
        if (!camera.getCameraInfo().hasFlashUnit()) return;

        boolean on = camera.getCameraInfo().getTorchState().getValue()
                == TorchState.ON;

        camera.getCameraControl().enableTorch(!on);
        toggleFlash.setImageResource(on
                ? R.drawable.flash
                : R.drawable.flashof);
    }
}
