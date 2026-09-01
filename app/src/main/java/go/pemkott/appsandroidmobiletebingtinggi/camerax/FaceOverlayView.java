package go.pemkott.appsandroidmobiletebingtinggi.camerax;


import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

public class FaceOverlayView extends View {

    private Paint paint;
    private Paint boxPaint;
    private Paint textPaint;
    private Paint scrimPaint;
    private boolean faceInside = false;
    private RectF faceBox = null;
    private String matchText = "";

    // FRAME NORMALIZED (0–1)
    private final RectF frameNorm = new RectF(
            0.15f,  // left
            0.15f,  // top
            0.85f,  // right
            0.65f   // bottom
    );

    public FaceOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(12f);
        paint.setAntiAlias(true);

        boxPaint = new Paint();
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(5f);
        boxPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(45f);
        textPaint.setFakeBoldText(true);
        textPaint.setShadowLayer(5f, 0, 0, Color.BLACK);

        scrimPaint = new Paint();
        scrimPaint.setColor(Color.parseColor("#99000000")); // Gelap transparan
        scrimPaint.setStyle(Paint.Style.FILL);
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
        Paint eraser = new Paint();
        eraser.setAntiAlias(true);
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawOval(framePx, eraser);
        canvas.restoreToCount(saveCount);

        // 2. Draw Oval Border
        paint.setColor(faceInside ? Color.GREEN : Color.WHITE);
        canvas.drawOval(framePx, paint);

        // 3. Draw Percentage Text only (Box removed)
        if (faceBox != null && !matchText.isEmpty()) {
            // Map faceBox top coordinate to view coordinates for text positioning
            float textY = faceBox.top * getHeight() - 15;
            float textX = faceBox.left * getWidth();
            canvas.drawText(matchText, textX, textY, textPaint);
        }
    }
}

