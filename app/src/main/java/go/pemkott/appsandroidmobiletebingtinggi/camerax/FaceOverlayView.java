package go.pemkott.appsandroidmobiletebingtinggi.camerax;


import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

public class FaceOverlayView extends View {

    private Paint paint;
    private Paint progressPaint;
    private Paint textPaint;
    private Paint scrimPaint;
    private Paint eraserPaint;

    private boolean faceInside = false;
    private RectF faceBox = null;
    private String matchText = "";
    private float progress = 0f;

    // FRAME NORMALIZED (0–1)
    private final RectF frameNorm = new RectF(
            0.15f,  // left
            0.15f,  // top
            0.85f,  // right
            0.65f   // bottom
    );

    public FaceOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(12f);
        paint.setAntiAlias(true);

        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(12f);
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(Color.parseColor("#2196F3")); // Blue progress

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(45f);
        textPaint.setFakeBoldText(true);
        textPaint.setShadowLayer(5f, 0, 0, Color.BLACK);

        scrimPaint = new Paint();
        scrimPaint.setColor(Color.parseColor("#99000000")); // Gelap transparan
        scrimPaint.setStyle(Paint.Style.FILL);

        eraserPaint = new Paint();
        eraserPaint.setAntiAlias(true);
        eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public RectF getFrameNormalized() {
        return frameNorm;
    }

    public void setFaceData(boolean inside, RectF faceBox, String matchText) {
        this.faceInside = inside;
        this.faceBox = faceBox;
        this.matchText = matchText;
        invalidate();
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

        RectF framePx = new RectF(
                frameNorm.left * getWidth(),
                frameNorm.top * getHeight(),
                frameNorm.right * getWidth(),
                frameNorm.bottom * getHeight()
        );

        // 1. Draw Scrim (Background gelap di luar oval)
        int saveCount = canvas.saveLayer(0, 0, getWidth(), getHeight(), null);
        canvas.drawRect(0, 0, getWidth(), getHeight(), scrimPaint);
        
        // Eraser effect for the oval hole
        canvas.drawOval(framePx, eraserPaint);
        canvas.restoreToCount(saveCount);

        // 2. Draw Oval Border
        paint.setColor(faceInside ? Color.parseColor("#4CAF50") : Color.WHITE);
        canvas.drawOval(framePx, paint);

        // 3. Draw Progress Arc
        if (faceInside && progress > 0) {
            float sweepAngle = progress * 360f;
            canvas.drawArc(framePx, -90, sweepAngle, false, progressPaint);
        }

        // 4. Draw Percentage Text
        if (faceBox != null && !matchText.isEmpty()) {
            float textY = faceBox.top * getHeight() - 15;
            float textX = faceBox.left * getWidth();
            canvas.drawText(matchText, textX, textY, textPaint);
        }
    }
}
