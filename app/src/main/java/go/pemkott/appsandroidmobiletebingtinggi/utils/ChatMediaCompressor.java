package go.pemkott.appsandroidmobiletebingtinggi.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ChatMediaCompressor {

    private static final String TAG = "ChatMediaCompressor";
    private static final int MAX_IMAGE_DIMENSION = 1280; // 1280px max width/height
    private static final int COMPRESS_QUALITY = 75;      // 75% JPEG quality

    /**
     * Kompres Gambar di Perangkat HP sebelum diunggah ke server
     */
    public static File compressImage(Context context, Uri imageUri) {
        try {
            // 1. Read image dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream is1 = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.decodeStream(is1, null, options);
            if (is1 != null) is1.close();

            int actualWidth = options.outWidth;
            int actualHeight = options.outHeight;

            if (actualWidth <= 0 || actualHeight <= 0) {
                return null;
            }

            // 2. Calculate sample size
            int sampleSize = 1;
            while ((actualWidth / sampleSize) > MAX_IMAGE_DIMENSION || (actualHeight / sampleSize) > MAX_IMAGE_DIMENSION) {
                sampleSize *= 2;
            }

            // 3. Decode downsampled bitmap
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            InputStream is2 = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(is2, null, options);
            if (is2 != null) is2.close();

            if (bitmap == null) {
                return null;
            }

            // 4. Fix orientation from EXIF
            bitmap = rotateBitmapIfRequired(context, imageUri, bitmap);

            // 5. Compress to JPEG file in cache directory
            File outputFile = new File(context.getCacheDir(), "compressed_img_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, fos);
            fos.flush();
            fos.close();
            bitmap.recycle();

            Log.d(TAG, "Foto berhasil dikompres: " + outputFile.length() / 1024 + " KB");
            return outputFile;

        } catch (Exception e) {
            Log.e(TAG, "Error compressing image: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Kompres File Video di Perangkat HP sebelum diunggah ke server
     */
    public static File compressVideo(Context context, Uri videoUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(videoUri);
            if (inputStream == null) return null;

            File outputFile = new File(context.getCacheDir(), "compressed_vid_" + System.currentTimeMillis() + ".mp4");
            FileOutputStream outputStream = new FileOutputStream(outputFile);

            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            Log.d(TAG, "Video berhasil dikompres/diproses: " + outputFile.length() / 1024 + " KB");
            return outputFile;

        } catch (Exception e) {
            Log.e(TAG, "Error compressing video: " + e.getMessage(), e);
            return null;
        }
    }

    private static Bitmap rotateBitmapIfRequired(Context context, Uri uri, Bitmap bitmap) {
        try {
            InputStream input = context.getContentResolver().openInputStream(uri);
            ExifInterface ei;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N && input != null) {
                ei = new ExifInterface(input);
            } else {
                return bitmap;
            }

            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (input != null) input.close();

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(bitmap, 270);
                default:
                    return bitmap;
            }
        } catch (Exception e) {
            return bitmap;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedBitmap = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedBitmap;
    }
}
