package go.pemkott.appsandroidmobiletebingtinggi.camerax;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.util.Log;

import androidx.camera.core.ImageProxy;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FaceRecognizer {
    private static final String TAG = "FaceRecognizer";
    private static final String MODEL_FILE = "mobile_facenet.tflite";
    private static final int INPUT_IMAGE_SIZE = 112; // MobileFaceNet standard
    private static final float THRESHOLD = 0.7f; // Cosine similarity threshold (Lowered for better matching)

    private Interpreter interpreter;

    public FaceRecognizer(Context context) {
        try {
            MappedByteBuffer modelBuffer = loadModelFile(context);
            if (modelBuffer != null) {
                interpreter = new Interpreter(modelBuffer);
                int[] shape = interpreter.getOutputTensor(0).shape();
                Log.d(TAG, "Model loaded. Output shape: " + java.util.Arrays.toString(shape));
            } else {
                Log.e(TAG, "FATAL: Model file missing in assets!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing interpreter: " + e.getMessage());
        }
    }

    private MappedByteBuffer loadModelFile(Context context) {
        try (AssetFileDescriptor fileDescriptor = context.getAssets().openFd(MODEL_FILE);
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } catch (IOException e) {
            Log.e(TAG, "Model file not found in assets: " + MODEL_FILE);
            return null;
        }
    }

    public float[] getEmbedding(Bitmap bitmap, Rect boundingBox) {
        if (interpreter == null || bitmap == null || boundingBox == null) return null;

        try {
            // Crop face from bitmap
            Bitmap faceBitmap = cropFace(bitmap, boundingBox);
            
            // Preprocess image
            TensorImage tensorImage = new TensorImage(interpreter.getInputTensor(0).dataType());
            ImageProcessor imageProcessor = new ImageProcessor.Builder()
                    .add(new ResizeOp(INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                    .add(new NormalizeOp(127.5f, 127.5f)) 
                    .build();
            
            tensorImage.load(faceBitmap);
            tensorImage = imageProcessor.process(tensorImage);

            // Run inference
            int outputSize = interpreter.getOutputTensor(0).shape()[1];
            float[][] output = new float[1][outputSize];
            interpreter.run(tensorImage.getBuffer(), output);
            
            return output[0];
        } catch (Exception e) {
            Log.e(TAG, "Embedding error: " + e.getMessage());
            return null;
        }
    }

    private Bitmap cropFace(Bitmap bitmap, Rect boundingBox) {
        // Add 10% padding to face crop for better recognition context
        int paddingW = (int) (boundingBox.width() * 0.10f);
        int paddingH = (int) (boundingBox.height() * 0.10f);
        
        int left = Math.max(boundingBox.left - paddingW, 0);
        int top = Math.max(boundingBox.top - paddingH, 0);
        int right = Math.min(boundingBox.right + paddingW, bitmap.getWidth());
        int bottom = Math.min(boundingBox.bottom + paddingH, bitmap.getHeight());
        
        int width = right - left;
        int height = bottom - top;
        
        return Bitmap.createBitmap(bitmap, left, top, width, height);
    }

    public boolean compare(float[] emb1, float[] emb2) {
        if (emb1 == null || emb2 == null) return false;
        
        float similarity = cosineSimilarity(emb1, emb2);
        return similarity > THRESHOLD;
    }

    public float getSimilarityScore(float[] emb1, float[] emb2) {
        if (emb1 == null || emb2 == null) return 0f;
        return cosineSimilarity(emb1, emb2);
    }

    private float cosineSimilarity(float[] emb1, float[] emb2) {
        float dotProduct = 0.0f;
        float norm1 = 0.0f;
        float norm2 = 0.0f;
        for (int i = 0; i < emb1.length; i++) {
            dotProduct += emb1[i] * emb2[i];
            norm1 += emb1[i] * emb1[i];
            norm2 += emb2[i] * emb2[i];
        }
        return dotProduct / (float) (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }

    @androidx.annotation.OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
    public Bitmap toBitmap(ImageProxy image) {
        Image img = image.getImage();
        if (img == null) return null;

        // More efficient YUV to RGB conversion could be done here, 
        // but keeping it simple and only calling it when needed (Tahap 2)
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 90, out);

        byte[] imageBytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        Matrix matrix = new Matrix();
        matrix.postRotate(image.getImageInfo().getRotationDegrees());
        matrix.postScale(-1, 1, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
        
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
