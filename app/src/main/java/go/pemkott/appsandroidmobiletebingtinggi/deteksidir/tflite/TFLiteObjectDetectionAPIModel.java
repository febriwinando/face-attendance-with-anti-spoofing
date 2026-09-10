/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package go.pemkott.appsandroidmobiletebingtinggi.deteksidir.tflite;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Trace;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

//import org.pytorch.LiteModuleLoader;

/**
 * Wrapper for frozen detection models trained using the Tensorflow Object Detection API:
 * - https://github.com/tensorflow/models/tree/master/research/object_detection
 * where you can find the training code.
 *
 * To use pretrained models in the API or convert to TF Lite models, please see docs for details:
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/detection_model_zoo.md
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/running_on_mobile_tensorflowlite.md#running-our-model-on-android
 */
public class TFLiteObjectDetectionAPIModel implements Classifier {
  private static final Logger LOGGER = new Logger();
  private static float[] TORCHVISION_NORM_MEAN_RGB = new float[] {0.5f, 0.5f, 0.5f};
  private static float[] TORCHVISION_NORM_STD_RGB = new float[] {0.5f, 0.5f, 0.5f};

  // Only return this many results.
  private static final int NUM_DETECTIONS = 10;
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
  // outputLocations: array of shape [Batchsize, NUM_DETECTIONS,4]
  // contains the location of detected boxes
  private float[][][] outputLocations;
  // outputClasses: array of shape [Batchsize, NUM_DETECTIONS]
  // contains the classes of detected boxes
  private float[][] outputClasses;
  // outputScores: array of shape [Batchsize, NUM_DETECTIONS]
  // contains the scores of detected boxes
  private float[][] outputScores;
  // numDetections: array of shape [Batchsize]
  // contains the number of detected boxes
  private float[] numDetections;

  private ByteBuffer imgData;

  private Interpreter tfLite;
  private Module model;

// Face Mask Detector Output
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

  /**
   * Initializes a native TensorFlow session for classifying images.
   *
   * @param assetManager The asset manager to be used to load assets.
   * @param modelFilename The filepath of the model GraphDef protocol buffer.
   * @param labelFilename The filepath of label file for classes.
   * @param inputSize The size of image input
   * @param isQuantized Boolean representing model is quantized or not
   */

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

    try{
      System.out.println("TEST***********************************************************************");
      d.model = LiteModuleLoader.load("/data/user/0/go.pemkott.appsandroidmobiletebingtinggi/files/gpumodel_2.ptl");
      System.out.println("Loaded model *****************************************************************");
    }  catch (Exception e) {
      System.out.println(e + "************************************************************************************s");
    }



    try {
      d.tfLite = new Interpreter(loadModelFile(assetManager, modelFilename));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    d.isModelQuantized = isQuantized;
    // Pre-allocate buffers.
    int numBytesPerChannel;
    if (isQuantized) {
      numBytesPerChannel = 1; // Quantized
    } else {
      numBytesPerChannel = 4; // Floating point
    }
    d.imgData = ByteBuffer.allocateDirect(1 * d.inputSize * d.inputSize * 3 * numBytesPerChannel);
    d.imgData.order(ByteOrder.nativeOrder());
    d.intValues = new int[d.inputSize * d.inputSize];
    Interpreter.Options options = new Interpreter.Options();
    options.setNumThreads(NUM_THREADS);
    d.tfLite = new Interpreter(loadModelFile(assetManager2, modelFilename2), options);
//    d.tfLite.setNumThreads(NUM_THREADS);
    d.outputLocations = new float[1][NUM_DETECTIONS][4];
    d.outputClasses = new float[1][NUM_DETECTIONS];
    d.outputScores = new float[1][NUM_DETECTIONS];
    d.numDetections = new float[1];
    return d;
  }

  @Override
  public List<Recognition> recognizeImage(final Bitmap bitmap){
    outputLocations = new float[1][NUM_DETECTIONS][4];
    outputClasses = new float[1][NUM_DETECTIONS];
    outputScores = new float[1][NUM_DETECTIONS];
    numDetections = new float[1];
    String[] labels = {"Nyata", "Palsu"};
  // Log this method so that it can be analyzed with systrace.
    Trace.beginSection("recognizeImage");

    Trace.beginSection("preprocessBitmap");
    // Preprocess the image data from 0-255 int to normalized float based
    // on the provided parameters.

    Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
            TORCHVISION_NORM_MEAN_RGB, TORCHVISION_NORM_STD_RGB);

    IValue[] outputTuple = model.forward(IValue.from(inputTensor)).toTuple();
    Tensor outputTensor = outputTuple[1].toTensor();

    float[] scores = outputTensor.getDataAsFloatArray();

    int maxScoreIdx = 0;
    String className = "";
    float maxScore = 0;
    System.out.println(scores[0]);
    if (scores[0] > 0.9){
      maxScoreIdx = 0;
      className = labels[0];
      maxScore = scores[0];

    } else{
      maxScoreIdx = 1;
      className = labels[1];
      maxScore = scores[0];
    }


    int numDetectionsOutput = Math.min(NUM_DETECTIONS, (int) numDetections[0]);

    final ArrayList<Recognition> recognitions = new ArrayList<>(numDetectionsOutput);
    recognitions.add(
            new Recognition(
                    "" + maxScoreIdx,
                    className,
                    maxScore,
                    new RectF()));

    return recognitions;
  }

//  @Override
  public List<Recognition> recognizeImage_2(final Bitmap bitmap) {
    // Log this method so that it can be analyzed with systrace.
    Trace.beginSection("recognizeImage");

    Trace.beginSection("preprocessBitmap");
    // Preprocess the image data from 0-255 int to normalized float based
    // on the provided parameters.
    bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

    imgData.rewind();
    for (int i = 0; i < inputSize; ++i) {
      for (int j = 0; j < inputSize; ++j) {
        int pixelValue = intValues[i * inputSize + j];
        if (isModelQuantized) {
          // Quantized model
          imgData.put((byte) ((pixelValue >> 16) & 0xFF));
          imgData.put((byte) ((pixelValue >> 8) & 0xFF));
          imgData.put((byte) (pixelValue & 0xFF));
        } else { // Float model
          imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
          imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
          imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        }
      }
    }
    Trace.endSection(); // preprocessBitmap

    // Copy the input data into TensorFlow.
    Trace.beginSection("feed");
    outputLocations = new float[1][NUM_DETECTIONS][4];
    outputClasses = new float[1][NUM_DETECTIONS];
    outputScores = new float[1][NUM_DETECTIONS];
    numDetections = new float[1];

    Object[] inputArray = {imgData};
    //Map<Integer, Object> outputMap = new HashMap<>();
    //outputMap.put(0, outputLocations);
    //outputMap.put(1, outputClasses);
    //outputMap.put(2, outputScores);
    //outputMap.put(3, numDetections);
    Trace.endSection();

// Here outputMap is changed to fit the Face Mask detector
    Map<Integer, Object> outputMap = new HashMap<>();


    output = new float[1][2];
    outputMap.put(0, output);

    // Run the inference call.
    Trace.beginSection("run");
    //tfLite.runForMultipleInputsOutputs(inputArray, outputMapBack);
    tfLite.runForMultipleInputsOutputs(inputArray, outputMap);
    Trace.endSection();

    float mask = output[0][0];
    float no_mask = output[0][1];
    float confidence;
    String id;
    String label;
    if (mask > no_mask) {
      label = "mask";
      confidence = mask;
      id = "0";
    }
    else {
       label = "no mask";
       confidence = no_mask;
       id = "1";
    }



    LOGGER.i("prediction: " + mask + ", " + no_mask);
    // Show the best detections.
    // after scaling them back to the input size.
      
    // You need to use the number of detections from the output and not the NUM_DETECTONS variable declared on top
      // because on some models, they don't always output the same total number of detections
      // For example, your model's NUM_DETECTIONS = 20, but sometimes it only outputs 16 predictions
      // If you don't use the output's numDetections, you'll get nonsensical data
    int numDetectionsOutput = Math.min(NUM_DETECTIONS, (int) numDetections[0]); // cast from float to integer, use min for safety
      
    final ArrayList<Recognition> recognitions = new ArrayList<>(numDetectionsOutput);
    recognitions.add(
          new Recognition(
              id,
              label,
              confidence,
              new RectF()));

//    for (int i = 0; i < numDetectionsOutput; ++i) {
//      final RectF detection =
//          new RectF(
//              outputLocations[0][i][1] * inputSize,
//              outputLocations[0][i][0] * inputSize,
//              outputLocations[0][i][3] * inputSize,
//              outputLocations[0][i][2] * inputSize);
//      // SSD Mobilenet V1 Model assumes class 0 is background class
//      // in label file and class labels start from 1 to number_of_classes+1,
//      // while outputClasses correspond to class index from 0 to number_of_classes
//      int labelOffset = 1;
//      recognitions.add(
//          new Recognition(
//              "" + i,
//              labels.get((int) outputClasses[0][i] + labelOffset),
//              outputScores[0][i],
//              detection));
//    }
    Trace.endSection(); // "recognizeImage"
    return recognitions;
  }

  @Override
  public void enableStatLogging(final boolean logStats) {}

  @Override
  public String getStatString() {
    return "";
  }

  @Override
  public void close() {}

  public void setNumThreads(int num_threads) {
    if (tfLite != null) {
      // Membebaskan resource dari tfLite interpreter sebelumnya
      tfLite.close();
    }

    // Buat opsi Interpreter baru dengan jumlah thread yang diinginkan
    Interpreter.Options options = new Interpreter.Options();
    options.setNumThreads(num_threads);

    // Muat ulang model dengan opsi yang diperbarui
    try {
      tfLite = new Interpreter(loadModelFile(assetManager2, modelFilename2), options);
    } catch (IOException e) {
      e.printStackTrace();
    }
//    if (tfLite != null) tfLite.setNumThreads(num_threads);
  }

  @Override
  public void setUseNNAPI(boolean isChecked) {
    if (tfLite != null) {
      // Tutup interpreter yang sudah ada
      tfLite.close();
    }

    // Buat opsi baru untuk Interpreter dan atur penggunaan NNAPI
    Interpreter.Options options = new Interpreter.Options();
    options.setUseNNAPI(isChecked); // Atur NNAPI sesuai parameter

    // Muat ulang model dengan opsi baru
    try {
      tfLite = new Interpreter(loadModelFile(assetManager2, modelFilename2), options);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
