package go.pemkott.appsandroidmobiletebingtinggi.adapter.chat;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import go.pemkott.appsandroidmobiletebingtinggi.R;

public class SearchableMultiSelectAdapter extends RecyclerView.Adapter<SearchableMultiSelectAdapter.ViewHolder> {

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    private Context context;
    private List<MultiSelectItem> fullList;
    private List<MultiSelectItem> displayList;
    private OnSelectionChangedListener listener;
    private boolean isOpdMode;

    public SearchableMultiSelectAdapter(Context context, List<MultiSelectItem> list, boolean isOpdMode, OnSelectionChangedListener listener) {
        this.context = context;
        this.fullList = list != null ? list : new ArrayList<>();
        this.displayList = new ArrayList<>(this.fullList);
        this.isOpdMode = isOpdMode;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_multi_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MultiSelectItem item = displayList.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvSubtitle.setText(item.getSubtitle());
        holder.cbCheck.setChecked(item.isChecked());

        if (isOpdMode) {
            holder.civIcon.setImageResource(R.drawable.ic_locasi_office);
        } else {
            if (!TextUtils.isEmpty(item.getAvatarUrl())) {
                Glide.with(context)
                        .load(item.getAvatarUrl())
                        .placeholder(R.drawable.ic_baseline_person_24)
                        .error(R.drawable.ic_baseline_person_24)
                        .into(holder.civIcon);
            } else {
                holder.civIcon.setImageResource(R.drawable.ic_baseline_person_24);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            boolean newState = !item.isChecked();
            item.setChecked(newState);
            holder.cbCheck.setChecked(newState);

            if (listener != null) {
                listener.onSelectionChanged(getSelectedIds().size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    public void filter(String query) {
        displayList.clear();
        if (TextUtils.isEmpty(query)) {
            displayList.addAll(fullList);
        } else {
            String lower = query.toLowerCase().trim();
            for (MultiSelectItem item : fullList) {
                if (item.getTitle().toLowerCase().contains(lower) || item.getSubtitle().toLowerCase().contains(lower)) {
                    displayList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void selectAll(boolean select) {
        for (MultiSelectItem item : fullList) {
            item.setChecked(select);
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(getSelectedIds().size());
        }
    }

    public List<Long> getSelectedIds() {
        List<Long> selected = new ArrayList<>();
        for (MultiSelectItem item : fullList) {
            if (item.isChecked()) {
                selected.add(item.getId());
            }
        }
        return selected;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView civIcon;
        TextView tvTitle, tvSubtitle;
        CheckBox cbCheck;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            civIcon = itemView.findViewById(R.id.civItemIcon);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvSubtitle = itemView.findViewById(R.id.tvItemSubtitle);
            cbCheck = itemView.findViewById(R.id.cbItemCheck);
        }
    }
}
