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

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.camerax.FaceRecognizer;
import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.customview.OverlayView;
import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.customview.OverlayView.DrawCallback;
import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.env.BorderedText;
import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.env.ImageUtils;
import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.env.Logger;
import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.tflite.Classifier;
import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.tflite.TFLiteObjectDetectionAPIModel;
import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.tflite.Utils;
import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.tracking.MultiBoxTracker;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();

  private static final int TF_OD_API_INPUT_SIZE = 224;
  private static final boolean TF_OD_API_IS_QUANTIZED = false;
  private static final String TF_OD_API_MODEL_FILE = "mask_detector.tflite";
  private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/mobile_label.txt";

  private enum CaptureStage {
    ANTI_SPOOFING_INIT, // Tahap 1: Verifikasi Keaslian Wajah
    VERIFY_IDENTITY,    // Tahap 2: Face Recognition
    CAPTURE,            // Tahap 3: Otomatis Capture
    DONE
  }
  private CaptureStage currentStage = CaptureStage.ANTI_SPOOFING_INIT;
  private int nyataStreak = 0;
  private boolean isAutoCapturing = false;
  private static final int REQUIRED_NYATA_STREAK = 2;
  
  private FaceRecognizer faceRecognizer;
  private float[] referenceEmbedding;
  private float lastSimilarityScore = 0f;
  private boolean isMatched = false;
  
  enum Challenge { BLINK, SMILE, TURN_LEFT, TURN_RIGHT, LOOK_UP }
  private final List<Challenge> challengeQueue = new ArrayList<>();
  private int challengeIndex = 0;
  private float currentChallengeProgress = 0f;
  
  private String aktivitas;

  private static final DetectorMode MODE = DetectorMode.TF_OD_API;
  // Minimum detection confidence to track a detection.
  private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
  private static final boolean MAINTAIN_ASPECT = false;



  private static final boolean SAVE_PREVIEW_BITMAP = false;
  private static final float TEXT_SIZE_DIP = 10;
  OverlayView trackingOverlay;
  private Integer sensorOrientation;

  private Classifier detector;

  private long lastProcessingTimeMs;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingDetection = false;

  private long timestamp = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;

  private MultiBoxTracker tracker;

  private BorderedText borderedText;

  // Face detector
  private FaceDetector faceDetector;

  // here the preview image is drawn in portrait way
  private Bitmap portraitBmp = null;
  // here the face is cropped and drawn
  private Bitmap faceBmp = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);


    // Real-time contour detection of multiple faces
    FaceDetectorOptions options =
            new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                    .build();


    FaceDetector detector = FaceDetection.getClient(options);

    faceDetector = detector;
    aktivitas = getIntent().getStringExtra("aktivitas");
    generateChallengeQueue();

    // Initialize Face Recognition
    faceRecognizer = new FaceRecognizer(this);
    loadReferenceEmbedding();

    try{
      System.out.println("TEST***********************************************************************");
      System.out.println(Utils.assetFilePath(this, "DeepPixBis_121_v5.ptl"));
      Module model = LiteModuleLoader.load("/data/user/0/go.pemkott.appsandroidmobiletebingtinggi/files/DeepPixBis_121_v5.ptl");
      System.out.println("Loaded model *****************************************************************");
    }  catch (Exception e) {
      System.out.println(e + "************************************************************************************s");
    }
  }

  private void loadReferenceEmbedding() {
    runInBackground(() -> {
      try {
        Bitmap refBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.contohfile);
        if (refBitmap != null) {
          // Use a dummy full-rect bounding box if we don't have face detection for the reference yet,
          // but better to run face detection on it once.
          InputImage image = InputImage.fromBitmap(refBitmap, 0);
          faceDetector.process(image).addOnSuccessListener(faces -> {
            if (!faces.isEmpty()) {
              Rect bounds = faces.get(0).getBoundingBox();
              referenceEmbedding = faceRecognizer.getEmbedding(refBitmap, bounds);
              LOGGER.i("Reference embedding loaded successfully");
            } else {
              // Fallback: use the whole image if no face found in reference (not ideal)
              referenceEmbedding = faceRecognizer.getEmbedding(refBitmap, new Rect(0, 0, refBitmap.getWidth(), refBitmap.getHeight()));
              LOGGER.w("No face found in reference image, using full image.");
            }
          });
        } else {
          LOGGER.e("Could not load reference image contohfile.jpeg");
        }
      } catch (Exception e) {
        LOGGER.e(e, "Error loading reference embedding");
      }
    });
  }

  private void generateChallengeQueue() {
    Challenge[] pool = Challenge.values();
    Random r = new Random();
    challengeQueue.clear();
    challengeQueue.add(pool[r.nextInt(pool.length)]);
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

  ProgressDialog progressDialog;

  public void handlerProgressDialog(){
    Handler handler = new Handler();
    handler.postDelayed(() -> {
      //your code here
      progressDialog.dismiss();}, 1500);

  }
  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
            TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    tracker = new MultiBoxTracker(this);


    try {
      detector =
              TFLiteObjectDetectionAPIModel.create(
                      this,
                      getAssets(),
                      TF_OD_API_MODEL_FILE,
                      TF_OD_API_LABELS_FILE,
                      TF_OD_API_INPUT_SIZE,
                      TF_OD_API_IS_QUANTIZED);
      //cropSize = TF_OD_API_INPUT_SIZE;
    } catch (final IOException e) {
      e.printStackTrace();
      LOGGER.e(e, "Exception initializing classifier!");
      Toast toast =
              Toast.makeText(
                      getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
      toast.show();
      finish();
    }

//    previewWidth = size.getWidth();
//    previewHeight = size.getHeight();

    int screenOrientation = getScreenOrientation();
    sensorOrientation = rotation - screenOrientation;
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);


    int targetW, targetH;

    if (sensorOrientation == 90 || sensorOrientation == 270) {
      targetH = previewWidth;
      targetW = previewHeight;
    }

    else {
      targetW = previewWidth;
      targetH = previewHeight;
    }

    int cropW = (int) (targetW / 2.0);
    int cropH = (int) (targetH / 2.0);

    croppedBitmap = Bitmap.createBitmap(cropW, cropH, Config.ARGB_8888);

    portraitBmp = Bitmap.createBitmap(targetW, targetH, Config.ARGB_8888);
    faceBmp = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Config.ARGB_8888);

    frameToCropTransform =
            ImageUtils.getTransformationMatrix(
                    previewWidth, previewHeight,
                    cropW, cropH,
                    sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
            new DrawCallback() {
              @Override
              public void drawCallback(final Canvas canvas) {
                tracker.draw(canvas);

                if (isDebug()) {
                  tracker.drawDebug(canvas);
                }
              }
            });

    tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
  }


  @Override
  protected void processImage() {
    ++timestamp;
    final long currTimestamp = timestamp;
    trackingOverlay.postInvalidate();

    // No mutex needed as this method is not reentrant.
    if (computingDetection) {
      readyForNextImage();
      return;
    }
    computingDetection = true;
    LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

    readyForNextImage();

    // Transform rgbFrameBitmap into upright portraitBmp
    int sourceW = rgbFrameBitmap.getWidth();
    int sourceH = rgbFrameBitmap.getHeight();
    int targetW = portraitBmp.getWidth();
    int targetH = portraitBmp.getHeight();
    Matrix transform = createTransform(
            sourceW,
            sourceH,
            targetW,
            targetH,
            sensorOrientation);
    final Canvas cv = new Canvas(portraitBmp);
    cv.drawBitmap(rgbFrameBitmap, transform, null);

    InputImage image = InputImage.fromBitmap(portraitBmp, 0);
    faceDetector
            .process(image)
            .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
              @Override
              public void onSuccess(List<Face> faces) {
                if (faces == null || faces.size() == 0) {
                  nyataStreak = 0;
                  updateResults(currTimestamp, new LinkedList<>(), 0, null);
                  return;
                }

                runInBackground(
                        new Runnable() {
                          @Override
                          public void run() {
                            onFacesDetected(currTimestamp, faces);
                          }
                        });


              }

            })
            .addOnFailureListener(e -> {
              LOGGER.e(e, "Face detection failed");
              nyataStreak = 0;
              updateResults(currTimestamp, new LinkedList<>(), 0, null);
            });


  }

  @Override
  protected int getLayoutId() {
    return R.layout.tfe_od_camera_connection_fragment_tracking;
  }
  private Size chooseOptimalSize(Size[] choices, int width, int height) {
    for (Size option : choices) {
      if (option.getHeight() == option.getWidth() * height / width &&
              option.getWidth() >= width && option.getHeight() >= height) {
        return option;
      }
    }
    // Jika tidak ada ukuran yang cocok, kembalikan ukuran terbesar
    return choices[0];
  }
  @Override
  protected Size getDesiredPreviewFrameSize() {

//    CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
//    try {
//      String cameraId = manager.getCameraIdList()[0]; // Mendapatkan ID kamera pertama
//
//      CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
//
//      // Mendapatkan ukuran preview terbaik yang cocok dengan proporsional aspek tertentu
//      Size[] outputSizes = Objects.requireNonNull(characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP))
//              .getOutputSizes(ImageFormat.JPEG);
//      Size largestSize = Collections.max(Arrays.asList(outputSizes), new Comparator<Size>() {
//        @Override
//        public int compare(Size lhs, Size rhs) {
//          // Membandingkan berdasarkan total piksel
//          return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
//                  (long) rhs.getWidth() * rhs.getHeight());
//        }
//      });
//
//      // Mendapatkan ukuran terbaik dengan proporsional tertentu, contoh 4:3
//      Size bestSize = chooseOptimalSize(outputSizes, largestSize.getWidth(), largestSize.getHeight());
//
//      // Sekarang bestSize berisi ukuran kamera yang dapat Anda gunakan
//      int width = bestSize.getWidth();
//      int height = bestSize.getHeight();
//      previewWidth = width;
//      previewHeight = height;
//
//
//      return new Size(previewWidth, previewHeight);
//
//    } catch (Exception e) {
//      e.printStackTrace();
//
//      Display display = getWindowManager().getDefaultDisplay();
//      Point size = new Point();
//      display.getSize(size);
//      int targetWidth = size.x;
//      int targetHeight = size.y;
//      return new Size(targetWidth, targetHeight);
//    }
    Camera camera;
    int index = getCameraId();
    camera = Camera.open(index);
    Camera.Parameters parameters = camera.getParameters();
    Camera.Size previewSize = parameters.getPreviewSize();
    previewHeight = previewSize.height;
    previewWidth = previewSize.width;

    Log.d("TestingPL Detector", previewWidth+" - "+previewHeight);

    return new Size(previewWidth, previewHeight);

  }
  private int facing = Camera.CameraInfo.CAMERA_FACING_FRONT;

  private int getCameraId() {
    Camera.CameraInfo ci = new Camera.CameraInfo();
    for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
      Camera.getCameraInfo(i, ci);
      if (ci.facing == this.facing) return i;
    }
    return -1; // No camera found
  }

  // Which detection model to use: by default uses Tensorflow Object Detection API frozen
  // checkpoints.
  private enum DetectorMode {
    TF_OD_API;
  }

  @Override
  protected void setUseNNAPI(final boolean isChecked) {
    runInBackground(() -> detector.setUseNNAPI(isChecked));
  }

  @Override
  protected void setNumThreads(final int numThreads) {
    runInBackground(() -> detector.setNumThreads(numThreads));
  }


  // Face Mask Processing
  private Matrix createTransform(
          final int srcWidth,
          final int srcHeight,
          final int dstWidth,
          final int dstHeight,
          final int applyRotation) {

    Matrix matrix = new Matrix();
    if (applyRotation != 0) {
      if (applyRotation % 90 != 0) {
//        LOGGER.w("Rotation of %d % 90 != 0", applyRotation);
      }

      // Translate so center of image is at origin.
      matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);

      // Rotate around origin.
      matrix.postRotate(applyRotation);
    }

//        // Account for the already applied rotation, if any, and then determine how
//        // much scaling is needed for each axis.
//        final boolean transpose = (Math.abs(applyRotation) + 90) % 180 == 0;
//
//        final int inWidth = transpose ? srcHeight : srcWidth;
//        final int inHeight = transpose ? srcWidth : srcHeight;

    if (applyRotation != 0) {

      // Translate back from origin centered reference to destination frame.
      matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
    }

    return matrix;

  }

  private void updateResults(long currTimestamp, final List<Classifier.Recognition> mappedRecognitions, int ambilGambar, Bitmap hasilDeteksi) {

    tracker.trackResults(mappedRecognitions, currTimestamp);
    trackingOverlay.postInvalidate();
    computingDetection = false;


    runOnUiThread(
            new Runnable() {
              @Override
              public void run() {
                showFrameInfo(previewWidth + "x" + previewHeight);
                showCropInfo(croppedBitmap.getWidth() + "x" + croppedBitmap.getHeight());
                showInference(lastProcessingTimeMs + "ms");
                showGambar(ambilGambar);
                if (hasilDeteksi != null){
                  hasilDeteksiGambar(hasilDeteksi);
                }
              }
            });

  }

  private void onFacesDetected(long currTimestamp, List<Face> faces) {

    cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
    final List<Classifier.Recognition> mappedRecognitions = new LinkedList<Classifier.Recognition>();

    int sourceW = rgbFrameBitmap.getWidth();
    int sourceH = rgbFrameBitmap.getHeight();
    int targetW = portraitBmp.getWidth();
    int targetH = portraitBmp.getHeight();
    Matrix transform = createTransform(
            sourceW,
            sourceH,
            targetW,
            targetH,
            sensorOrientation);
    final Canvas cv = new Canvas(portraitBmp);

    // draws the original image in portrait mode.
    cv.drawBitmap(rgbFrameBitmap, transform, null);

    Matrix transformInverted = new Matrix();
    transform.invert(transformInverted);

    for (Face face : faces) {

      LOGGER.i("FACE" + face.toString());

      Rect faceRect = face.getBoundingBox();

      int left = Math.max(0, faceRect.left);
      int top = Math.max(0, faceRect.top);
      int right = Math.min(portraitBmp.getWidth(), faceRect.right);
      int bottom = Math.min(portraitBmp.getHeight(), faceRect.bottom);

      int faceW = right - left;
      int faceH = bottom - top;

      if (faceW <= 10 || faceH <= 10) continue;

      // Clean, un-distorted face cropping
      Bitmap croppedFace = Bitmap.createBitmap(portraitBmp, left, top, faceW, faceH);
      Bitmap scaledFaceBmp = Bitmap.createScaledBitmap(croppedFace, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, true);

      String label = "";
      float confidence = -1f;

      final long startTime = SystemClock.uptimeMillis();

      boolean isSharp = isImageSharp(scaledFaceBmp);
      boolean hasMoire = detectMoirePattern(scaledFaceBmp);

      final List<Classifier.Recognition> resultsAux = detector.recognizeImage(scaledFaceBmp);
      lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

      if (resultsAux != null && resultsAux.size() > 0) {
        Classifier.Recognition result = resultsAux.get(0);
        label = result.getTitle();
        confidence = result.getConfidence();
      }

      final float finalConfidence = confidence;
      final boolean aiNyata = "Nyata".equalsIgnoreCase(label) || finalConfidence >= 0.50f;
      final boolean securityPassed = isSharp && !hasMoire && aiNyata;

      // Update Streak
      if (securityPassed) {
        nyataStreak++;
      } else {
        nyataStreak = 0;
      }

      // --- UI FEEDBACK & AUTO CAPTURE LOGIC ---
      runOnUiThread(() -> {
        if (isAutoCapturing) return;

        if (securityPassed && ivTakeFoto != null) {
          ivTakeFoto.setEnabled(true);
        }

        if (nyataStreak < REQUIRED_NYATA_STREAK) {
          String status;
          if (!isSharp) status = "⚠️ Gambar Kabur, Hadap Kamera dengan Jelas";
          else if (hasMoire) status = "❌ PALSU (Layar Digital)";
          else if (!aiNyata) status = "❌ PALSU (" + String.format("%.0f%%", finalConfidence * 100) + ")";
          else status = "✅ WAJAH NYATA TERDETEKSI (" + nyataStreak + "/" + REQUIRED_NYATA_STREAK + ")";

          if (tvPeringatanWajah != null) {
            tvPeringatanWajah.setVisibility(View.VISIBLE);
            tvPeringatanWajah.setText(status);
          }
        } else {
          // AUTO CAPTURE TRIGGERED
          isAutoCapturing = true;
          if (tvPeringatanWajah != null) {
            tvPeringatanWajah.setVisibility(View.VISIBLE);
            tvPeringatanWajah.setText("✅ WAJAH TERVERIFIKASI NYATA!\nMengambil Foto...");
          }
          if (ivTakeFoto != null) ivTakeFoto.setEnabled(false);

          Bitmap captureBitmap = Bitmap.createBitmap(portraitBmp);

          runInBackground(() -> {
            try {
              saveBitmapToInternalStorage(captureBitmap);
            } catch (Exception e) {
              LOGGER.e(e, "Gagal auto-capture foto");
              runOnUiThread(() -> {
                isAutoCapturing = false;
                nyataStreak = 0;
                if (tvPeringatanWajah != null) {
                  tvPeringatanWajah.setText("Gagal mengambil foto, silakan coba lagi");
                }
              });
            }
          });
        }
      });

      final Integer color = securityPassed ? Color.GREEN : Color.RED;

      // Map faceRect from portraitBmp coordinates back to raw frame (previewWidth x previewHeight)
      RectF rawFrameRect = new RectF(left, top, right, bottom);
      transformInverted.mapRect(rawFrameRect);

      if (getCameraFacing() == CameraCharacteristics.LENS_FACING_FRONT) {
        Matrix flip = new Matrix();
        if (sensorOrientation == 90 || sensorOrientation == 270) {
          flip.postScale(1, -1, previewWidth / 2.0f, previewHeight / 2.0f);
        } else {
          flip.postScale(-1, 1, previewWidth / 2.0f, previewHeight / 2.0f);
        }
        flip.mapRect(rawFrameRect);
      }

      final Classifier.Recognition result = new Classifier.Recognition(
              "0", label, confidence, rawFrameRect);

      result.setColor(color);
      result.setLocation(rawFrameRect);
      mappedRecognitions.add(result);

    }

    updateResults(currTimestamp, mappedRecognitions, 1, portraitBmp);

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
      case TURN_LEFT:
        passed = eulerY < -1;
        break;
      case TURN_RIGHT:
        passed = eulerY > 1;
        break;
      case LOOK_UP:
        // ML Kit Euler X is positive when looking up
        passed = eulerX > 10;
        break;
    }

    if (passed) {
      challengeIndex++;
    }
  }

  private boolean isImageSharp(Bitmap bitmap) {
    if (bitmap == null) return true;
    try {
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
      float avgLaplacian = (float) laplacianSum / (width * height);
      return avgLaplacian > 1.0f;
    } catch (Exception e) {
      return true;
    }
  }

  private boolean detectMoirePattern(Bitmap bitmap) {
    if (bitmap == null) return false;
    try {
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
          if (Math.abs(p1 - p2) > 65 && Math.abs(p1 - p3) > 65) moireScore++;
        }
      }
      return moireScore > (width * height * 0.20);
    } catch (Exception e) {
      return false;
    }
  }

}
