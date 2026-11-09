package go.pemkott.appsandroidmobiletebingtinggi.chat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.ChatMessageAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMessageItem;

public class ChatSwipeToReplyCallback extends ItemTouchHelper.SimpleCallback {

    public interface OnSwipeReplyListener {
        void onSwipeToReply(ChatMessageItem item);
    }

    private final Context context;
    private final ChatMessageAdapter adapter;
    private final OnSwipeReplyListener listener;

    private final Drawable replyIcon;
    private final Paint backgroundPaint;
    private boolean hasVibrated = false;

    public ChatSwipeToReplyCallback(Context context, ChatMessageAdapter adapter, OnSwipeReplyListener listener) {
        super(0, ItemTouchHelper.RIGHT);
        this.context = context;
        this.adapter = adapter;
        this.listener = listener;

        this.replyIcon = ContextCompat.getDrawable(context, R.drawable.ic_reply);
        if (this.replyIcon != null) {
            this.replyIcon.setTint(Color.parseColor("#FFFFFF"));
        }

        this.backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.backgroundPaint.setColor(Color.parseColor("#16A34A")); // Hijau khas tema Pemkot
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int pos = viewHolder.getAdapterPosition();
        if (pos != RecyclerView.NO_POSITION && adapter != null) {
            ChatMessageItem item = adapter.getItemAt(pos);
            if (item != null && listener != null) {
                listener.onSwipeToReply(item);
            }
            adapter.notifyItemChanged(pos);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        View itemView = viewHolder.itemView;
        float maxDx = 160f * context.getResources().getDisplayMetrics().density;
        float actualDx = Math.min(dX, maxDx);

        // Haptic feedback saat melewati batas threshold swipe (70dp)
        float threshold = 65f * context.getResources().getDisplayMetrics().density;
        if (actualDx >= threshold && !hasVibrated) {
            hasVibrated = true;
            try {
                itemView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            } catch (Exception ignored) {}
        } else if (actualDx < threshold) {
            hasVibrated = false;
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && actualDx > 0) {
            float progress = Math.min(1.0f, actualDx / threshold);

            float circleRadius = (18f * context.getResources().getDisplayMetrics().density) * progress;
            float circleCenterX = itemView.getLeft() + (actualDx / 2f);
            float circleCenterY = itemView.getTop() + (itemView.getHeight() / 2f);

            if (circleRadius > 2f) {
                c.drawCircle(circleCenterX, circleCenterY, circleRadius, backgroundPaint);

                if (replyIcon != null && progress > 0.4f) {
                    int iconSize = (int) (18f * context.getResources().getDisplayMetrics().density * progress);
                    int iconLeft = (int) (circleCenterX - (iconSize / 2f));
                    int iconTop = (int) (circleCenterY - (iconSize / 2f));
                    replyIcon.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize);
                    replyIcon.setAlpha((int) (255 * progress));
                    replyIcon.draw(c);
                }
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, actualDx, dY, actionState, isCurrentlyActive);
    }
}

