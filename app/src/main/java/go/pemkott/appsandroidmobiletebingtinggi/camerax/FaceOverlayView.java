package go.pemkott.appsandroidmobiletebingtinggi.camerax;


import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

public class FaceOverlayView extends View {

    private Paint paint;
    private boolean faceInside = false;

    // FRAME NORMALIZED (0–1)
    private final RectF frameNorm = new RectF(
            0.05f,  // left
            0.05f,  // top
            0.95f,  // right
            0.7f   // bottom
    );

    public FaceOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8f);
        paint.setAntiAlias(true);
    }

    public RectF getFrameNormalized() {
        return frameNorm;
    }

    public void setFaceInside(boolean inside) {
        faceInside = inside;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(faceInside ? Color.GREEN : Color.RED);

        RectF framePx = new RectF(
                frameNorm.left * getWidth(),
                frameNorm.top * getHeight(),
                frameNorm.right * getWidth(),
                frameNorm.bottom * getHeight()
        );

        canvas.drawOval(framePx, paint);
    }
}

