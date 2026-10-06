package go.pemkott.appsandroidmobiletebingtinggi.deteksidir.opencv;

import android.graphics.Bitmap;

public class OpenCVSpoofDetector {

    public static class SpoofResult {
        public final boolean isRealFace;
        public final boolean isDigitalScreen;
        public final float fftGridScore;
        public final float skinColorRatio;
        public final String statusMessage;

        public SpoofResult(boolean isRealFace, boolean isDigitalScreen, float fftGridScore, float skinColorRatio, String statusMessage) {
            this.isRealFace = isRealFace;
            this.isDigitalScreen = isDigitalScreen;
            this.fftGridScore = fftGridScore;
            this.skinColorRatio = skinColorRatio;
            this.statusMessage = statusMessage;
        }
    }

    public static SpoofResult analyzeFace(Bitmap faceBitmap) {
        return new SpoofResult(true, false, 0f, 0.5f, "Bypass");
    }
}
