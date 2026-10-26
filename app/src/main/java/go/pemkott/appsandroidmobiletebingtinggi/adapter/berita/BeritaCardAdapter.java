package go.pemkott.appsandroidmobiletebingtinggi.adapter.berita;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.berita.BeritaItem;

public class BeritaCardAdapter extends RecyclerView.Adapter<BeritaCardAdapter.ViewHolder> {

    private final Context context;
    private List<BeritaItem> list = new ArrayList<>();

    public BeritaCardAdapter(Context context) {
        this.context = context;
    }

    public void setList(List<BeritaItem> list) {
        this.list = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_berita_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BeritaItem item = list.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvDate.setText(item.getDateFormatted());
        holder.tvViews.setText("👁️ " + item.getViews());
        if (item.getCategory() != null && !item.getCategory().isEmpty()) {
            holder.tvCategory.setText(item.getCategory().toUpperCase());
        } else {
            holder.tvCategory.setText("DISKOMINFO");
        }

        String imgUrl = item.getThumbnailUrl();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            Glide.with(context)
                    .load(imgUrl)
                    .placeholder(R.drawable.one_person)
                    .error(R.drawable.one_person)
                    .into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.one_person);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, go.pemkott.appsandroidmobiletebingtinggi.berita.DetailBeritaActivity.class);
            intent.putExtra(go.pemkott.appsandroidmobiletebingtinggi.berita.DetailBeritaActivity.EXTRA_SLUG, item.getSlug());
            intent.putExtra(go.pemkott.appsandroidmobiletebingtinggi.berita.DetailBeritaActivity.EXTRA_TITLE, item.getTitle());
            intent.putExtra(go.pemkott.appsandroidmobiletebingtinggi.berita.DetailBeritaActivity.EXTRA_THUMBNAIL, item.getThumbnailUrl());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvCategory, tvTitle, tvDate, tvViews;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvViews = itemView.findViewById(R.id.tvViews);
        }
    }
}
