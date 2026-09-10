package go.pemkott.appsandroidmobiletebingtinggi.deteksidir.DeteksiWajah;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableBitmap implements Parcelable {
    private Bitmap bitmap;

    public ParcelableBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    protected ParcelableBitmap(Parcel in) {
        bitmap = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<ParcelableBitmap> CREATOR = new Creator<ParcelableBitmap>() {
        @Override
        public ParcelableBitmap createFromParcel(Parcel in) {
            return new ParcelableBitmap(in);
        }

        @Override
        public ParcelableBitmap[] newArray(int size) {
            return new ParcelableBitmap[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(bitmap, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
