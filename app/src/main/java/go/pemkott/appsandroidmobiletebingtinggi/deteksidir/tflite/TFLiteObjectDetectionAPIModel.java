package go.pemkott.appsandroidmobiletebingtinggi.deteksidir.tflite;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Trace;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import go.pemkott.appsandroidmobiletebingtinggi.deteksidir.env.Logger;

public class TFLiteObjectDetectionAPIModel implements Classifier {
  private static final Logger LOGGER = new Logger();

  // Float model
  private static final float IMAGE_MEAN = 128.0f;
  private static final float IMAGE_STD = 128.0f;
  // Number of threads in the java app
  private static final int NUM_THREADS = 4;
  private boolean isModelQuantized;
  // Config values.
  private int inputSize;
  // Pre-allocated buffers.
  private Vector<String> labels = new Vector<String>();
  private int[] intValues;

  private ByteBuffer imgData;
  private Interpreter tfLite;

  // Face Mask / Anti-Spoofing Detector Output
  private float[][] output;

  private TFLiteObjectDetectionAPIModel() {
  }

  /** Memory-map the model file in Assets. */
  private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)
      throws IOException {
    AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
    FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
    FileChannel fileChannel = inputStream.getChannel();
    long startOffset = fileDescriptor.getStartOffset();
    long declaredLength = fileDescriptor.getDeclaredLength();
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
  }

  public static AssetManager assetManager2;
  public static String modelFilename2;

  public static Classifier create(
          final AssetManager assetManager,
          final String modelFilename,
          final String labelFilename,
          final int inputSize,
          final boolean isQuantized)
      throws IOException {
    final TFLiteObjectDetectionAPIModel d = new TFLiteObjectDetectionAPIModel();

    assetManager2 = assetManager;
    modelFilename2 = modelFilename;
    String actualFilename = labelFilename.split("file:///android_asset/")[1];
    InputStream labelsInput = assetManager.open(actualFilename);
    BufferedReader br = new BufferedReader(new InputStreamReader(labelsInput));
    String line;
    while ((line = br.readLine()) != null) {
      LOGGER.w(line);
      d.labels.add(line);
    }
    br.close();

    d.inputSize = inputSize;

    d.isModelQuantized = isQuantized;
    int numBytesPerChannel = isQuantized ? 1 : 4;
    d.imgData = ByteBuffer.allocateDirect(1 * d.inputSize * d.inputSize * 3 * numBytesPerChannel);
    d.imgData.order(ByteOrder.nativeOrder());
    d.intValues = new int[d.inputSize * d.inputSize];

    Interpreter.Options options = new Interpreter.Options();
    options.setNumThreads(NUM_THREADS);
    d.tfLite = new Interpreter(loadModelFile(assetManager2, modelFilename2), options);

    return d;
  }

  @Override
  public List<Recognition> recognizeImage(final Bitmap bitmap) {
    Trace.beginSection("recognizeImage");
    Trace.beginSection("preprocessBitmap");

    bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

    imgData.rewind();
    for (int i = 0; i < inputSize; ++i) {
      for (int j = 0; j < inputSize; ++j) {
        int pixelValue = intValues[i * inputSize + j];
        if (isModelQuantized) {
          imgData.put((byte) ((pixelValue >> 16) & 0xFF));
          imgData.put((byte) ((pixelValue >> 8) & 0xFF));
          imgData.put((byte) (pixelValue & 0xFF));
        } else {
          imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
          imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
          imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        }
      }
    }
    Trace.endSection();

    Object[] inputArray = {imgData};
    Map<Integer, Object> outputMap = new HashMap<>();

    output = new float[1][2];
    outputMap.put(0, output);

    Trace.beginSection("run");
    if (tfLite != null) {
      tfLite.runForMultipleInputsOutputs(inputArray, outputMap);
    }
    Trace.endSection();

    float mask = output[0][0];
    float noMask = output[0][1];
    float confidence;
    String id;
    String label;
    if (mask > noMask) {
      label = "Nyata";
      confidence = mask;
      id = "0";
    } else {
      label = "Tidak Nyata";
      confidence = noMask;
      id = "1";
    }

    LOGGER.i("prediction: " + mask + ", " + noMask);

    final ArrayList<Recognition> recognitions = new ArrayList<>(1);
    recognitions.add(
          new Recognition(
              id,
              label,
              confidence,
              new RectF()));

    Trace.endSection();
    return recognitions;
  }

  @Override
  public void enableStatLogging(final boolean logStats) {}

  @Override
  public String getStatString() {
    return "";
  }

  @Override
  public void close() {
    if (tfLite != null) {
      tfLite.close();
      tfLite = null;
    }
  }

  public void setNumThreads(int num_threads) {
    if (tfLite != null) {
      tfLite.close();
    }
    Interpreter.Options options = new Interpreter.Options();
    options.setNumThreads(num_threads);
    try {
      tfLite = new Interpreter(loadModelFile(assetManager2, modelFilename2), options);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void setUseNNAPI(boolean isChecked) {
    if (tfLite != null) {
      tfLite.close();
    }
    Interpreter.Options options = new Interpreter.Options();
    if (isChecked) {
      try {
        Class<?> nnApiDelegateClass = Class.forName("org.tensorflow.lite.nnapi.NnApiDelegate");
        Object delegate = nnApiDelegateClass.getDeclaredConstructor().newInstance();
        for (Method method : options.getClass().getMethods()) {
          if (method.getName().equals("addDelegate") && method.getParameterTypes().length == 1) {
            method.invoke(options, delegate);
            break;
          }
        }
      } catch (Throwable e) {
        LOGGER.e(e, "NNAPI Delegate is not available: " + e.getMessage());
      }
    }

    try {
      tfLite = new Interpreter(loadModelFile(assetManager2, modelFilename2), options);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
