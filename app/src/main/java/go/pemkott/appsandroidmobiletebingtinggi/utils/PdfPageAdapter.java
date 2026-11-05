package go.pemkott.appsandroidmobiletebingtinggi.utils;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PdfPageAdapter extends RecyclerView.Adapter<PdfPageAdapter.PageViewHolder> {

    private final PdfRenderer pdfRenderer;
    private float scaleFactor = 1.0f;

    public PdfPageAdapter(PdfRenderer renderer) {
        this.pdfRenderer = renderer;
    }

    public void setScaleFactor(float scale) {
        this.scaleFactor = Math.max(0.5f, Math.min(scale, 4.0f));
        notifyDataSetChanged();
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        imageView.setAdjustViewBounds(true);
        imageView.setPadding(0, 8, 0, 8);
        return new PageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        if (pdfRenderer == null) return;

        try (PdfRenderer.Page page = pdfRenderer.openPage(position)) {
            int renderWidth = Math.max(100, (int) (page.getWidth() * scaleFactor));
            int renderHeight = Math.max(100, (int) (page.getHeight() * scaleFactor));

            Bitmap bitmap = Bitmap.createBitmap(
                    renderWidth,
                    renderHeight,
                    Bitmap.Config.ARGB_8888
            );

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            holder.imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return pdfRenderer != null ? pdfRenderer.getPageCount() : 0;
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        PageViewHolder(@NonNull ImageView itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }
    }
}
