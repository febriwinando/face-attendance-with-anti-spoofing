package go.pemkott.appsandroidmobiletebingtinggi.utils;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

public class MapUtils {

    public static ValueAnimator showRippleEffect(GoogleMap map, LatLng latLng) {
        if (map == null) return null;

        final Circle rippleCircle = map.addCircle(new CircleOptions()
                .center(latLng)
                .radius(0)
                .strokeWidth(0)
                .fillColor(Color.argb(64, 0, 141, 218))); // Biru transparan (#40008DDA)

        ValueAnimator animator = ValueAnimator.ofFloat(0, 80); // radius dalam meter
        animator.setDuration(2500);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            try {
                float value = (float) animation.getAnimatedValue();
                rippleCircle.setRadius(value);
                int alpha = (int) (64 * (1.0f - value / 80f));
                rippleCircle.setFillColor(Color.argb(alpha, 0, 141, 218));
            } catch (Exception e) {
                animation.cancel();
            }
        });
        animator.start();
        return animator;
    }
}
