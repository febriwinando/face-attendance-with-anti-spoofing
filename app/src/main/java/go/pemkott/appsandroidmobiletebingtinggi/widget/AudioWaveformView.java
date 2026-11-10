package go.pemkott.appsandroidmobiletebingtinggi.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class AudioWaveformView extends View {

    private final Paint activePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint inactivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float progress = 0.0f; // 0.0f to 1.0f
    private int activeColor = Color.parseColor("#FFFFFF");
    private int inactiveColor = Color.argb(160, 255, 255, 255); // Crisp semi-transparent white
    private boolean isPlaying = false;

    // Harmonic voice peaks for the central speech burst (envelope distribution)
    private static final float[] BURST_PEAKS = new float[] {
            0.35f, 0.55f, 0.85f, 0.65f, 1.00f, 0.80f, 0.95f, 0.60f, 0.82f, 0.50f, 0.30f
    };

    public interface OnSeekListener {
        void onSeek(float progress);
    }
    private OnSeekListener seekListener;

    public AudioWaveformView(Context context) {
        super(context);
        init();
    }

    public AudioWaveformView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AudioWaveformView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        float barWidth = 2.6f * density;

        activePaint.setColor(activeColor);
        activePaint.setStrokeWidth(barWidth);
        activePaint.setStrokeCap(Paint.Cap.ROUND);
        activePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        inactivePaint.setColor(inactiveColor);
        inactivePaint.setStrokeWidth(barWidth);
        inactivePaint.setStrokeCap(Paint.Cap.ROUND);
        inactivePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public void setWaveColors(int active, int inactive) {
        this.activeColor = active;
        this.inactiveColor = inactive;
        activePaint.setColor(active);
        inactivePaint.setColor(inactive);
        invalidate();
    }

    public void setProgress(float progress) {
        if (progress < 0f) progress = 0f;
        if (progress > 1f) progress = 1f;
        this.progress = progress;
        invalidate();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        if (this.isPlaying != playing) {
            this.isPlaying = playing;
            if (playing) {
                postInvalidateOnAnimation();
            } else {
                invalidate();
            }
        }
    }

    public float getProgress() {
        return progress;
    }

    public void setOnSeekListener(OnSeekListener listener) {
        this.seekListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        float density = getResources().getDisplayMetrics().density;
        float barWidth = 2.6f * density;
        float dotRadius = 1.35f * density;
        float barGap = 2.4f * density;
        float step = barWidth + barGap;

        int totalBars = (int) (width / step);
        if (totalBars <= 0) return;

        float startX = (width - (totalBars * step - barGap)) / 2f + barWidth / 2f;
        float centerY = height / 2f;
        float maxWaveHeight = height * 0.88f;

        int activeBarCount = (int) (progress * totalBars);
        long now = System.currentTimeMillis();

        // Exact pattern as in XChat / voice notes:
        // Baseline of small dots on left and right, with speech burst centered in the middle
        float centerIndex = totalBars * 0.52f;
        float voiceSpread = totalBars * 0.16f; // Center burst covering ~12-14 bars

        for (int i = 0; i < totalBars; i++) {
            float x = startX + (i * step);
            float dist = Math.abs(i - centerIndex);

            float factor = 0f;
            if (dist <= voiceSpread) {
                float norm = dist / voiceSpread; // 0.0 at center, 1.0 at edge
                int pIdx = (int) (norm * (BURST_PEAKS.length - 1));
                float peak = BURST_PEAKS[Math.min(pIdx, BURST_PEAKS.length - 1)];
                float envelope = (float) Math.cos(norm * Math.PI / 2.0); // Smooth bell curve
                factor = peak * envelope;

                if (isPlaying) {
                    // Dynamic live oscillating waves when audio is playing
                    double phase = (now / 120.0) + (i * 0.65);
                    double osc = (Math.sin(phase) + 1.0) / 2.0; // 0.0 to 1.0
                    factor = (float) Math.min(1.0, Math.max(0.18, factor * 0.35 + osc * 0.65 * envelope));
                }
            }

            boolean isPast = (i <= activeBarCount && progress > 0f);
            Paint paintToUse = isPast ? activePaint : inactivePaint;

            float barHeight = factor * maxWaveHeight;
            if (barHeight > (dotRadius * 2.2f)) {
                // Rounded vertical spectrum bar
                float halfHeight = barHeight / 2f;
                canvas.drawLine(x, centerY - halfHeight, x, centerY + halfHeight, paintToUse);
            } else {
                // Small round dot (silent / baseline voice note pattern)
                canvas.drawCircle(x, centerY, dotRadius, paintToUse);
            }
        }

        if (isPlaying) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            float newProgress = x / getWidth();
            if (newProgress < 0f) newProgress = 0f;
            if (newProgress > 1f) newProgress = 1f;
            setProgress(newProgress);
            if (seekListener != null) {
                seekListener.onSeek(newProgress);
            }
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(false);
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
}

