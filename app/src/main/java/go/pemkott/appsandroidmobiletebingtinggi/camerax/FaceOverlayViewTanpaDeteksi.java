package go.pemkott.appsandroidmobiletebingtinggi.camerax;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

public class FaceOverlayViewTanpaDeteksi extends View {

    private Paint borderPaint;
    private Paint progressPaint;
    private Paint scrimPaint;
    private Paint eraserPaint;
    
    private boolean faceInside = false;
    private float progress = 0f; // 0.0 to 1.0

    // FRAME NORMALIZED (0–1) - Centered and more professional
    private final RectF frameNorm = new RectF(
            0.15f,  // left
            0.15f,  // top
            0.85f,  // right
            0.65f   // bottom
    );

    public FaceOverlayViewTanpaDeteksi(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(10f);
        borderPaint.setAntiAlias(true);

        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(12f);
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        scrimPaint = new Paint();
        scrimPaint.setColor(Color.parseColor("#99000000")); // Dark semi-transparent
        scrimPaint.setStyle(Paint.Style.FILL);

        eraserPaint = new Paint();
        eraserPaint.setAntiAlias(true);
        eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public RectF getFrameNormalized() {
        return frameNorm;
    }

    public void setFaceInside(boolean inside) {
        if (this.faceInside != inside) {
            this.faceInside = inside;
            if (!inside) progress = 0f;
            invalidate();
        }
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();

        RectF framePx = new RectF(
                frameNorm.left * w,
                frameNorm.top * h,
                frameNorm.right * w,
                frameNorm.bottom * h
        );

        // 1. Draw Scrim with Oval Hole
        int saveCount = canvas.saveLayer(0, 0, w, h, null);
        canvas.drawRect(0, 0, w, h, scrimPaint);
        canvas.drawOval(framePx, eraserPaint);
        canvas.restoreToCount(saveCount);

        // 2. Draw Oval Border
        borderPaint.setColor(faceInside ? Color.parseColor("#4CAF50") : Color.WHITE);
        canvas.drawOval(framePx, borderPaint);

        // 3. Draw Progress Arc (only if face is inside)
        if (faceInside && progress > 0) {
            progressPaint.setColor(Color.parseColor("#2196F3")); // Blue for progress
            float sweepAngle = progress * 360f;
            // Start from top (-90 degrees)
            canvas.drawArc(framePx, -90, sweepAngle, false, progressPaint);
        }
    }
}
