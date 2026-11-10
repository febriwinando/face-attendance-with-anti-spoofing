package go.pemkott.appsandroidmobiletebingtinggi.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class ZoomableImageView extends AppCompatImageView {

    private final Matrix matrix = new Matrix();
    private final float[] matrixValues = new float[9];

    private float minScale = 1.0f;
    private float maxScale = 5.0f;
    private float currentScale = 1.0f;

    private int viewWidth = 0;
    private int viewHeight = 0;

    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private PointF lastTouch = new PointF();
    private boolean isDragging = false;

    public interface OnSingleTapListener {
        void onSingleTap();
    }
    private OnSingleTapListener singleTapListener;

    public ZoomableImageView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ZoomableImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomableImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setScaleType(ScaleType.MATRIX);

        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                float targetScale = currentScale * scaleFactor;

                if (targetScale < minScale) {
                    scaleFactor = minScale / currentScale;
                    currentScale = minScale;
                } else if (targetScale > maxScale) {
                    scaleFactor = maxScale / currentScale;
                    currentScale = maxScale;
                } else {
                    currentScale = targetScale;
                }

                matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
                checkBoundsAndCenter();
                setImageMatrix(matrix);
                return true;
            }
        });

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (singleTapListener != null) {
                    singleTapListener.onSingleTap();
                    return true;
                }
                return performClick();
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (currentScale > minScale * 1.2f) {
                    resetZoom();
                } else {
                    float target = 2.5f;
                    float factor = target / currentScale;
                    currentScale = target;
                    matrix.postScale(factor, factor, e.getX(), e.getY());
                    checkBoundsAndCenter();
                    setImageMatrix(matrix);
                }
                return true;
            }
        });
    }

    public void setOnSingleTapListener(OnSingleTapListener listener) {
        this.singleTapListener = listener;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        fitImageCenter();
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        post(this::fitImageCenter);
    }

    public void resetZoom() {
        currentScale = minScale;
        fitImageCenter();
    }

    private void fitImageCenter() {
        Drawable drawable = getDrawable();
        if (drawable == null || viewWidth == 0 || viewHeight == 0) return;

        int dWidth = drawable.getIntrinsicWidth();
        int dHeight = drawable.getIntrinsicHeight();
        if (dWidth <= 0 || dHeight <= 0) return;

        matrix.reset();
        float scaleX = (float) viewWidth / (float) dWidth;
        float scaleY = (float) viewHeight / (float) dHeight;
        float scale = Math.min(scaleX, scaleY);

        matrix.postScale(scale, scale);
        float redundantXSpace = (float) viewWidth - (scale * (float) dWidth);
        float redundantYSpace = (float) viewHeight - (scale * (float) dHeight);
        redundantXSpace /= 2;
        redundantYSpace /= 2;
        matrix.postTranslate(redundantXSpace, redundantYSpace);

        currentScale = 1.0f;
        setImageMatrix(matrix);
    }

    private void checkBoundsAndCenter() {
        Drawable drawable = getDrawable();
        if (drawable == null || viewWidth == 0 || viewHeight == 0) return;

        RectF rect = new RectF(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        matrix.mapRect(rect);

        float width = rect.width();
        float height = rect.height();

        float deltaX = 0, deltaY = 0;

        if (width < viewWidth) {
            deltaX = (viewWidth - width) / 2 - rect.left;
        } else if (rect.left > 0) {
            deltaX = -rect.left;
        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right;
        }

        if (height < viewHeight) {
            deltaY = (viewHeight - height) / 2 - rect.top;
        } else if (rect.top > 0) {
            deltaY = -rect.top;
        } else if (rect.bottom < viewHeight) {
            deltaY = viewHeight - rect.bottom;
        }

        matrix.postTranslate(deltaX, deltaY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (scaleDetector != null) scaleDetector.onTouchEvent(event);
        if (gestureDetector != null) gestureDetector.onTouchEvent(event);

        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastTouch.set(event.getX(), event.getY());
                isDragging = false;
                if (getParent() != null && currentScale > 1.05f) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1 && currentScale > 1.05f) {
                    float dx = event.getX() - lastTouch.x;
                    float dy = event.getY() - lastTouch.y;
                    if (Math.abs(dx) > 2 || Math.abs(dy) > 2) {
                        isDragging = true;
                        matrix.postTranslate(dx, dy);
                        checkBoundsAndCenter();
                        setImageMatrix(matrix);
                        lastTouch.set(event.getX(), event.getY());
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
        }
        return true;
    }
}

