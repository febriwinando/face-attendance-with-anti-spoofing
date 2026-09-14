package go.pemkott.appsandroidmobiletebingtinggi.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class CompressFile {

    Context context;

    public CompressFile(Context context) {
        this.context = context;
    }
    public static File getCompressedImageFile(File file, Context mContext) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            if (getFileExt(file.getName()).equals("png") || getFileExt(file.getName()).equals("PNG")) {
                o.inSampleSize = 6;
            } else {
                o.inSampleSize = 6;
            }

            FileInputStream inputStream = new FileInputStream(file);
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE = 400;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inPreferredConfig = Bitmap.Config.RGB_565;

            inputStream = new FileInputStream(file);
            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);

            ExifInterface ei = new ExifInterface(file.getAbsolutePath());
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    selectedBitmap = rotateImage(selectedBitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    selectedBitmap = rotateImage(selectedBitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    selectedBitmap = rotateImage(selectedBitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:

                default:
                    break;
            }
            inputStream.close();


            // here i override the original image file
            File folder = new File(Environment.getExternalStorageDirectory() + "/FolderName");
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdir();
            }
            if (success) {
                File newFile = new File(new File(folder.getAbsolutePath()), file.getName());
                if (newFile.exists()) {
                    newFile.delete();
                }
                FileOutputStream outputStream = new FileOutputStream(newFile);

                if (getFileExt(file.getName()).equals("png") || getFileExt(file.getName()).equals("PNG")) {
                    selectedBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
                } else {
                    selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                }

                return newFile;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
}
