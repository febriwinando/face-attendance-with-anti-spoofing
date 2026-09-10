/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package go.pemkott.appsandroidmobiletebingtinggi.deteksidir.DeteksiWajah;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Trace;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import go.pemkott.appsandroidmobiletebingtinggi.NewDashboard.DashboardVersiOne;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.dinasluarkantor.perjalanandinas.PerjalananDinasFinalActivity;
import go.pemkott.appsandroidmobiletebingtinggi.dinasluarkantor.tugaslapangan.TugasLapanganFinalActivity;
import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.env.ImageUtils;
import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.env.Logger;
import go.pemkott.appsandroidmobiletebingtinggi.izin.cuti.IzinCutiFinalActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izin.keperluanpribadi.KeperluanPribadiFinalActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izin.sakit.IzinSakitFinalActivity;

import go.pemkott.appsandroidmobiletebingtinggi.izinshift.JadwalIzinShiftActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izinshift.izinshiftcuti.IzinCutiShiftFinalActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izinshift.izinshiftpribadi.KeperluanPribadiShiftFinalActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izinshift.izinshiftsakit.IzinSakitShiftFinalActivity;
import go.pemkott.appsandroidmobiletebingtinggi.kehadiran.AbsensiKehadiranActivity;

import go.pemkott.appsandroidmobiletebingtinggi.kehadiransift.AbsenShiftActivity;
import go.pemkott.appsandroidmobiletebingtinggi.konstanta.AmbilFoto;

public abstract class CameraActivity extends AppCompatActivity
        implements OnImageAvailableListener,
        Camera.PreviewCallback,
        CompoundButton.OnCheckedChangeListener,
        View.OnClickListener {
    int jenisabsensi = DashboardVersiOne.jenisabsensi;
    private static final Logger LOGGER = new Logger();

    private static final int PERMISSIONS_REQUEST = 1;

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    protected int previewWidth = 0;
    protected int previewHeight = 0;
    private boolean debug = false;
    private Handler handler;
    private HandlerThread handlerThread;
    private boolean useCamera2API;
    private boolean isProcessingFrame = false;
    private byte[][] yuvBytes = new byte[3][];
    private int[] rgbBytes = null;
    private int yRowStride;
    private Runnable postInferenceCallback;
    private Runnable imageConverter;

    private LinearLayout bottomSheetLayout;
    private LinearLayout gestureLayout;
    private BottomSheetBehavior<LinearLayout> sheetBehavior;

    protected TextView frameValueTextView, cropValueTextView, inferenceTimeTextView;
    protected ImageView bottomSheetArrowImageView;
    private ImageView plusImageView, minusImageView;
    private SwitchCompat apiSwitchCompat;
    private TextView threadsTextView;


    private static final String KEY_USE_FACING = "use_facing";
    private Integer useFacing = null;
    private String cameraId = null;

    protected Integer getCameraFacing() {
        return useFacing;
    }

    ImageView ivTakeFoto, ivHasilGambar;
    int ambilGambar = 0;
    TextView tvPeringatanWajah;
    ProgressBar pbTakeGambar;
    public static Activity cameraActivity;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        LOGGER.d("onCreate " + this);
        super.onCreate(null);

        Intent intent = getIntent();
        useFacing = intent.getIntExtra(KEY_USE_FACING, CameraCharacteristics.LENS_FACING_FRONT);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.black));
        window.setNavigationBarColor(getResources().getColor(R.color.black));

        setContentView(R.layout.tfe_od_activity_camera);


        cameraActivity = this;

        if (hasPermission()) {
            setFragment();
        } else {
            requestPermission();
        }

        ivTakeFoto = findViewById(R.id.ivTakeFoto);
        tvPeringatanWajah = findViewById(R.id.tvPeringatanWajah);
        threadsTextView = findViewById(R.id.threads);
        plusImageView = findViewById(R.id.plus);
        minusImageView = findViewById(R.id.minus);
        apiSwitchCompat = findViewById(R.id.api_info_switch);
        bottomSheetLayout = findViewById(R.id.bottom_sheet_layout);
        gestureLayout = findViewById(R.id.gesture_layout);
        sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetArrowImageView = findViewById(R.id.bottom_sheet_arrow);
        pbTakeGambar = findViewById(R.id.pbTakeGambar);

        ivTakeFoto.setEnabled(false);

        ViewTreeObserver vto = gestureLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        gestureLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        //                int width = bottomSheetLayout.getMeasuredWidth();
                        int height = gestureLayout.getMeasuredHeight();
                        sheetBehavior.setPeekHeight(0);
                    }
                });
        sheetBehavior.setHideable(false);

        ivTakeFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    ivTakeFoto.setVisibility(View.GONE);
                    pbTakeGambar.setVisibility(View.VISIBLE);
                    saveBitmapToInternalStorage(gambarHasilDeteksi);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        sheetBehavior.setBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        switch (newState) {
                            case BottomSheetBehavior.STATE_HIDDEN:
                                break;
                            case BottomSheetBehavior.STATE_EXPANDED: {
                            }
                            break;
                            case BottomSheetBehavior.STATE_COLLAPSED: {
                            }
                            break;
                            case BottomSheetBehavior.STATE_DRAGGING:
                                break;
                            case BottomSheetBehavior.STATE_SETTLING:
                                break;
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    }
                });

        frameValueTextView = findViewById(R.id.frame_info);
        cropValueTextView = findViewById(R.id.crop_info);
        inferenceTimeTextView = findViewById(R.id.inference_info);

        apiSwitchCompat.setOnCheckedChangeListener(this);

        plusImageView.setOnClickListener(this);
        minusImageView.setOnClickListener(this);

    }

    protected int[] getRgbBytes() {
        imageConverter.run();
        return rgbBytes;
    }

    protected int getLuminanceStride() {
        return yRowStride;
    }

    protected byte[] getLuminance() {
        return yuvBytes[0];
    }

    /**
     * Callback for android.hardware.Camera API
     */
    @Override
    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        if (isProcessingFrame) {
            LOGGER.w("Dropping frame!");
            return;
        }

        try {
            // Initialize the storage bitmaps once when the resolution is known.
            if (rgbBytes == null) {

                Camera.Parameters parameters = camera.getParameters();
                Camera.Size previewSize = parameters.getPreviewSize();
                previewHeight = previewSize.height;
                previewWidth = previewSize.width;
                rgbBytes = new int[previewWidth * previewHeight];
                int rotation = 90;

                if (useFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                    rotation = 270;
                }

                Log.d("TestingPL", previewWidth+" - "+previewHeight);

                onPreviewSizeChosen(new Size(previewWidth, previewHeight), rotation);
            }
        } catch (final Exception e) {
            LOGGER.e(e, "Exception!");
            return;
        }

        isProcessingFrame = true;
        yuvBytes[0] = bytes;
        yRowStride = previewWidth;

        imageConverter =
                new Runnable() {
                    @Override
                    public void run() {
                        ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);
                    }
                };

        postInferenceCallback =
                new Runnable() {
                    @Override
                    public void run() {
                        camera.addCallbackBuffer(bytes);
                        isProcessingFrame = false;
                    }
                };
        processImage();
    }

    /**
     * Callback for Camera2 API
     */
    @Override
    public void onImageAvailable(final ImageReader reader) {
        // We need wait until we have some size from onPreviewSizeChosen
        if (previewWidth == 0 || previewHeight == 0) {
            return;
        }
        if (rgbBytes == null) {
            rgbBytes = new int[previewWidth * previewHeight];
        }
        try {
            final Image image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }

            if (isProcessingFrame) {
                image.close();
                return;
            }
            isProcessingFrame = true;
            Trace.beginSection("imageAvailable");
            final Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);
            yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();
            Log.d("TestingPL Line Detector", previewWidth+" - "+previewHeight);

            imageConverter =
                    new Runnable() {
                        @Override
                        public void run() {
                            ImageUtils.convertYUV420ToARGB8888(
                                    yuvBytes[0],
                                    yuvBytes[1],
                                    yuvBytes[2],
                                    previewWidth,
                                    previewHeight,
                                    yRowStride,
                                    uvRowStride,
                                    uvPixelStride,
                                    rgbBytes);
                        }
                    };

            postInferenceCallback =
                    new Runnable() {
                        @Override
                        public void run() {
                            image.close();
                            isProcessingFrame = false;
                        }
                    };

            processImage();
        } catch (final Exception e) {
            LOGGER.e(e, "Exception!");
            Trace.endSection();
            return;
        }
        Trace.endSection();
    }

    @Override
    public synchronized void onStart() {
        LOGGER.d("onStart " + this);
        super.onStart();
    }

    @Override
    public synchronized void onResume() {
        LOGGER.d("onResume " + this);
        super.onResume();

        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public synchronized void onPause() {
        LOGGER.d("onPause " + this);

        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            LOGGER.e(e, "Exception!");
        }

        super.onPause();
    }

    @Override
    public synchronized void onStop() {
        LOGGER.d("onStop " + this);
        super.onStop();
    }

    @Override
    public synchronized void onDestroy() {
        LOGGER.d("onDestroy " + this);
        super.onDestroy();
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String[] permissions, final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            if (allPermissionsGranted(grantResults)) {
                setFragment();
            } else {
                requestPermission();
            }
        }
    }

    private static boolean allPermissionsGranted(final int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
                Toast.makeText(
                        CameraActivity.this,
                        "Camera permission is required for this demo",
                        Toast.LENGTH_LONG)
                        .show();
            }
            requestPermissions(new String[]{PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
        }
    }

    // Returns true if the device supports the required hardware level, or better.
    private boolean isHardwareLevelSupported(
            CameraCharacteristics characteristics, int requiredLevel) {
        int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            return requiredLevel == deviceLevel;
        }
        // deviceLevel is not LEGACY, can use numerical sort
        return requiredLevel <= deviceLevel;
    }

    private String chooseCamera() {

        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {


            for (final String cameraId : manager.getCameraIdList()) {
                final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);


                final StreamConfigurationMap map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (map == null) {
                    continue;
                }

                // Fallback to camera1 API for internal cameras that don't have full support.
                // This should help with legacy situations where using the camera2 API causes
                // distorted or otherwise broken previews.
                //final int facing =
                //(facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
//        if (!facing.equals(useFacing)) {
//          continue;
//        }

                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (useFacing != null &&
                        facing != null &&
                        !facing.equals(useFacing)
                ) {
                    continue;
                }


                useCamera2API = (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
                        || isHardwareLevelSupported(
                        characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);


                LOGGER.i("Camera API lv2?: %s", useCamera2API);
                return cameraId;
            }
        } catch (CameraAccessException e) {
            LOGGER.e(e, "Not allowed to access camera");
        }

        return null;
    }


    protected void setFragment() {

        this.cameraId = chooseCamera();

        Fragment fragment;
        if (useCamera2API) {
            CameraConnectionFragment camera2Fragment =
                    CameraConnectionFragment.newInstance(
                            new CameraConnectionFragment.ConnectionCallback() {
                                @Override
                                public void onPreviewSizeChosen(final Size size, final int rotation) {
                                    previewHeight = size.getHeight();
                                    previewWidth = size.getWidth();
                                    CameraActivity.this.onPreviewSizeChosen(size, rotation);
                                }
                            },
                            this,
                            getLayoutId(),
                            getDesiredPreviewFrameSize());

            camera2Fragment.setCamera(cameraId);
            fragment = camera2Fragment;

        } else {

            int facing = Camera.CameraInfo.CAMERA_FACING_FRONT;
            LegacyCameraConnectionFragment frag = new LegacyCameraConnectionFragment(this,
                    getLayoutId(),
                    getDesiredPreviewFrameSize(), facing);
            fragment = frag;

        }

        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    public boolean isDebug() {
        return debug;
    }

    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        }
    }

    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setUseNNAPI(isChecked);
        if (isChecked) apiSwitchCompat.setText("NNAPI");
        else apiSwitchCompat.setText("TFLITE");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.plus) {
            String threads = threadsTextView.getText().toString().trim();
            int numThreads = Integer.parseInt(threads);
            if (numThreads >= 9) return;
            numThreads++;
            threadsTextView.setText(String.valueOf(numThreads));
            setNumThreads(numThreads);
        } else if (v.getId() == R.id.minus) {
            String threads = threadsTextView.getText().toString().trim();
            int numThreads = Integer.parseInt(threads);
            if (numThreads == 1) {
                return;
            }
            numThreads--;
            threadsTextView.setText(String.valueOf(numThreads));
            setNumThreads(numThreads);
        }
    }

    protected void showFrameInfo(String frameInfo) {
        frameValueTextView.setText(frameInfo);
    }

    protected void showCropInfo(String cropInfo) {
        cropValueTextView.setText(cropInfo);
    }

    protected void showInference(String inferenceTime) {
        inferenceTimeTextView.setText(inferenceTime);
    }

    protected void showGambar(int ambilGambar) {
        if (ambilGambar == 0){
            tvPeringatanWajah.setVisibility(View.VISIBLE);
            tvPeringatanWajah.setText("Arahkan Kamera Ke Wajah Anda!");
            ivTakeFoto.setEnabled(false);
        }else if (ambilGambar == 2){
            tvPeringatanWajah.setVisibility(View.VISIBLE);
            tvPeringatanWajah.setText("Fokuskan Kamera Ke Wajah Anda");
            ivTakeFoto.setEnabled(false);
        }else {
            tvPeringatanWajah.setVisibility(View.INVISIBLE);
            ivTakeFoto.setEnabled(true);
        }
    }
    Bitmap gambarHasilDeteksi;
    protected void hasilDeteksiGambar(Bitmap bitmap) {
        if (bitmap != null){
            gambarHasilDeteksi = bitmap;
            Log.d("CHECKCAMERA", bitmap.getWidth()+" - "+bitmap.getHeight());
        }else {
            ivTakeFoto.setEnabled(false);
        }
    }
    protected abstract void processImage();

    protected abstract void onPreviewSizeChosen(final Size size, final int rotation);

    protected abstract int getLayoutId();

    protected abstract Size getDesiredPreviewFrameSize();

    protected abstract void setNumThreads(int numThreads);

    protected abstract void setUseNNAPI(boolean isChecked);


    public static byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    AmbilFoto ambilFoto = new AmbilFoto(CameraActivity.this);
    String fileName;
    public void saveBitmapToInternalStorage(Bitmap bitmap) throws IOException {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/eabsensi");
        myDir.mkdirs();

        fileName = getFileName();
        File file = new File(myDir, fileName);


//        if (file.exists()) file.delete();

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        String currentPhotoPath = file.getAbsolutePath();

        Log.d("PathGambar", currentPhotoPath);

        Bitmap selectedBitmap = ambilFoto.fileBitmapCompress(file);
        Bitmap rotationBitmapSurat = Bitmap.createBitmap(selectedBitmap, 0,0, selectedBitmap.getWidth(), selectedBitmap.getHeight(), AmbilFoto.exifInterface(currentPhotoPath, 1), true);
//
        File filebaru = new File(myDir, fileName);
        if (filebaru.exists()) filebaru.delete();

        try {
            FileOutputStream out = new FileOutputStream(filebaru);
            rotationBitmapSurat.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        handlerProgress();


    }


    public void handlerProgress(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if (jenisabsensi == 1){
                Intent intent = new Intent(CameraActivity.this, AbsensiKehadiranActivity.class);
                intent.putExtra("fileName", fileName);
                startActivity(intent);
                finish();
            } else if (jenisabsensi == 2) {
                Intent intent = new Intent(CameraActivity.this, TugasLapanganFinalActivity.class);
                intent.putExtra("fileName", fileName);
                intent.putExtra("title", "Isi Data Tugas Lapangan");
                startActivity(intent);
                finish();
            }else if (jenisabsensi == 3) {
                Intent intent = new Intent(CameraActivity.this, PerjalananDinasFinalActivity.class);
                intent.putExtra("fileName", fileName);
                intent.putExtra("title", "Isi Data Perjalanan Dinas");
                startActivity(intent);
                finish();
            }else if (jenisabsensi == 4) {
                Intent intent = new Intent(CameraActivity.this, IzinCutiFinalActivity.class);
                intent.putExtra("fileName", fileName);
                intent.putExtra("title", "Isi Data Cuti");
                startActivity(intent);
                finish();
            }else if (jenisabsensi == 5) {
                Intent intent = new Intent(CameraActivity.this, KeperluanPribadiFinalActivity.class);
                intent.putExtra("fileName", fileName);
                intent.putExtra("title", "Isi Data Keperluan Pribadi");
                startActivity(intent);
                finish();
            }else if (jenisabsensi == 6) {
                Intent intent = new Intent(CameraActivity.this, IzinSakitFinalActivity.class);
                intent.putExtra("fileName", fileName);
                intent.putExtra("title", "Isi Data Sakit");
                startActivity(intent);
                finish();
            }else if (jenisabsensi == 7) {
                Intent intent = new Intent(CameraActivity.this, AbsenShiftActivity.class);
                intent.putExtra("fileName", fileName);
                intent.putExtra("title", "KEHADIRAN");
                startActivity(intent);
                finish();
            }else if (jenisabsensi == 8) {

                int jenisabsenizin = JadwalIzinShiftActivity.jenisabsensi;

                if (jenisabsenizin == 9){
                    Intent intent = new Intent(CameraActivity.this, KeperluanPribadiShiftFinalActivity.class);
                    intent.putExtra("fileName", fileName);
                    intent.putExtra("title", "Isi Data Keperluan Pribadi");
                    startActivity(intent);
                    finish();
                } else if (jenisabsenizin == 10) {
                    Intent intent = new Intent(CameraActivity.this, IzinCutiShiftFinalActivity.class);
                    intent.putExtra("fileName", fileName);
                    intent.putExtra("title", "Isi Data Cuti");
                    startActivity(intent);
                    finish();
                } else if (jenisabsenizin == 11) {
                    Intent intent = new Intent(CameraActivity.this, IzinSakitShiftFinalActivity.class);
                    intent.putExtra("fileName", fileName);
                    intent.putExtra("title", "Isi Data Sakit");
                    startActivity(intent);
                    finish();
                }

            }
        }else{
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (jenisabsensi == 1){
                    Intent intent = new Intent(CameraActivity.this, AbsensiKehadiranActivity.class);
                    intent.putExtra("fileName", fileName);
                    startActivity(intent);
                    finish();
                } else if (jenisabsensi == 2) {
                    Intent intent = new Intent(CameraActivity.this, TugasLapanganFinalActivity.class);
                    intent.putExtra("fileName", fileName);
                    intent.putExtra("title", "Isi Data Tugas Lapangan");
                    startActivity(intent);
                    finish();
                }else if (jenisabsensi == 3) {
                    Intent intent = new Intent(CameraActivity.this, PerjalananDinasFinalActivity.class);
                    intent.putExtra("fileName", fileName);
                    intent.putExtra("title", "Isi Data Perjalanan Dinas");
                    startActivity(intent);
                    finish();
                }else if (jenisabsensi == 4) {
                    Intent intent = new Intent(CameraActivity.this, IzinCutiFinalActivity.class);
                    intent.putExtra("fileName", fileName);
                    intent.putExtra("title", "Isi Data Cuti");
                    startActivity(intent);
                    finish();
                }else if (jenisabsensi == 5) {
                    Intent intent = new Intent(CameraActivity.this, KeperluanPribadiFinalActivity.class);
                    intent.putExtra("fileName", fileName);
                    intent.putExtra("title", "Isi Data Keperluan Pribadi");
                    startActivity(intent);
                    finish();
                }else if (jenisabsensi == 6) {
                    Intent intent = new Intent(CameraActivity.this, IzinSakitFinalActivity.class);
                    intent.putExtra("fileName", fileName);
                    intent.putExtra("title", "Isi Data Sakit");
                    startActivity(intent);
                    finish();
                }else if (jenisabsensi == 7) {
                    Intent intent = new Intent(CameraActivity.this, AbsenShiftActivity.class);
                    intent.putExtra("fileName", fileName);
                    intent.putExtra("title", "KEHADIRAN");
                    startActivity(intent);
                    finish();
                }else if (jenisabsensi == 8) {

                    int jenisabsenizin = JadwalIzinShiftActivity.jenisabsensi;

                    if (jenisabsenizin == 9){
                        Intent intent = new Intent(CameraActivity.this, KeperluanPribadiShiftFinalActivity.class);
                        intent.putExtra("fileName", fileName);
                        intent.putExtra("title", "Isi Data Keperluan Pribadi");
                        startActivity(intent);
                        finish();
                    } else if (jenisabsenizin == 10) {
                        Intent intent = new Intent(CameraActivity.this, IzinCutiShiftFinalActivity.class);
                        intent.putExtra("fileName", fileName);
                        intent.putExtra("title", "Isi Data Cuti");
                        startActivity(intent);
                        finish();
                    } else if (jenisabsenizin == 11) {
                        Intent intent = new Intent(CameraActivity.this, IzinSakitShiftFinalActivity.class);
                        intent.putExtra("fileName", fileName);
                        intent.putExtra("title", "Isi Data Sakit");
                        startActivity(intent);
                        finish();
                    }

                }

            }, 4000);
        }

    }

    public String getFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return "image_" + timeStamp + ".png";
    }


}
