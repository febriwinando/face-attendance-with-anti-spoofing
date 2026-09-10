package go.pemkott.appsandroidmobiletebingtinggi.camerax;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.TorchState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

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

public class CameraXTanpaDetectionAcitvity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageButton capture;
    private ImageView flipCamera, toggleFlash;
    private TextView txtChallenge;
    private ImageCapture imageCapture;
    private Camera camera;
    private int lensFacing = CameraSelector.LENS_FACING_FRONT;
    private String aktivitas;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
                else Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera_xtanpa_detection_acitvity);

        previewView = findViewById(R.id.cameraPreview);
        capture = findViewById(R.id.capture);
        txtChallenge = findViewById(R.id.txtChallenge);
        flipCamera = findViewById(R.id.flipCamera);
        toggleFlash = findViewById(R.id.toggleFlash);
        findViewById(R.id.ivBackCamera).setOnClickListener(v -> finish());

        aktivitas = getIntent().getStringExtra("aktivitas");

        capture.setOnClickListener(v -> takePicture());
        flipCamera.setOnClickListener(v -> toggleCamera());
        toggleFlash.setOnClickListener(v -> toggleFlashMode());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startCamera();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                provider.unbindAll();
                camera = provider.bindToLifecycle(this,
                        new CameraSelector.Builder().requireLensFacing(lensFacing).build(),
                        preview, imageCapture);

                updateFlashButtonVisibility();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
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
                Toast.makeText(CameraXTanpaDetectionAcitvity.this, "Gagal mengambil foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

            if (toggleFlash != null) {
                toggleFlash.setImageResource(!isTorchOn ? R.drawable.flashof : R.drawable.flash);
            }
        }
    }

    private void updateFlashButtonVisibility() {
        if (toggleFlash != null) {
            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                toggleFlash.setVisibility(View.VISIBLE);
                if (findViewById(R.id.vControlDivider) != null) {
                    findViewById(R.id.vControlDivider).setVisibility(View.VISIBLE);
                }
            } else {
                toggleFlash.setVisibility(View.GONE);
                if (findViewById(R.id.vControlDivider) != null) {
                    findViewById(R.id.vControlDivider).setVisibility(View.GONE);
                }
            }
        }
    }
}
