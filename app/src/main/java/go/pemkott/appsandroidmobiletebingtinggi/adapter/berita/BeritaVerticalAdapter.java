package go.pemkott.appsandroidmobiletebingtinggi.adapter.berita;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.berita.DetailBeritaActivity;
import go.pemkott.appsandroidmobiletebingtinggi.model.berita.BeritaItem;

public class BeritaVerticalAdapter extends RecyclerView.Adapter<BeritaVerticalAdapter.ViewHolder> {

    private final Context context;
    private List<BeritaItem> list = new ArrayList<>();

    public BeritaVerticalAdapter(Context context) {
        this.context = context;
    }

    public void setList(List<BeritaItem> list) {
        this.list = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_berita_vertical, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BeritaItem item = list.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvDate.setText(item.getDateFormatted());
        holder.tvViews.setText("👁️ " + item.getViews() + "x");
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
            Intent intent = new Intent(context, DetailBeritaActivity.class);
            intent.putExtra(DetailBeritaActivity.EXTRA_SLUG, item.getSlug());
            intent.putExtra(DetailBeritaActivity.EXTRA_TITLE, item.getTitle());
            intent.putExtra(DetailBeritaActivity.EXTRA_THUMBNAIL, item.getThumbnailUrl());
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
            ivThumbnail = itemView.findViewById(R.id.ivVerticalThumbnail);
            tvCategory = itemView.findViewById(R.id.tvVerticalCategory);
            tvTitle = itemView.findViewById(R.id.tvVerticalTitle);
            tvDate = itemView.findViewById(R.id.tvVerticalDate);
            tvViews = itemView.findViewById(R.id.tvVerticalViews);
        }
    }
}
