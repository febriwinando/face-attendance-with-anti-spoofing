package go.pemkott.appsandroidmobiletebingtinggi.adapter.chat;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatPrivacyResponse.BlockedEmployeeItem;

public class BlockedContactsAdapter extends RecyclerView.Adapter<BlockedContactsAdapter.ViewHolder> {

    public interface OnUnblockListener {
        void onUnblock(BlockedEmployeeItem item);
    }

    private final Context context;
    private List<BlockedEmployeeItem> list;
    private final OnUnblockListener listener;

    public BlockedContactsAdapter(Context context, List<BlockedEmployeeItem> list, OnUnblockListener listener) {
        this.context = context;
        this.list = list != null ? list : new ArrayList<>();
        this.listener = listener;
    }

    public void setList(List<BlockedEmployeeItem> list) {
        this.list = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeItem(long employeeId) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == employeeId) {
                list.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_blocked_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BlockedEmployeeItem item = list.get(position);
        holder.tvName.setText(item.getNama());
        holder.tvOpd.setText(!TextUtils.isEmpty(item.getOpdName()) ? item.getOpdName() : "NIP: " + item.getNip());

        if (!TextUtils.isEmpty(item.getAvatarUrl())) {
            Glide.with(context)
                    .load(item.getAvatarUrl())
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
                    .into(holder.civAvatar);
        } else {
            holder.civAvatar.setImageResource(R.drawable.ic_baseline_person_24);
        }

        holder.btnUnblock.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUnblock(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView civAvatar;
        TextView tvName, tvOpd;
        MaterialButton btnUnblock;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            civAvatar = itemView.findViewById(R.id.civBlockedAvatar);
            tvName = itemView.findViewById(R.id.tvBlockedName);
            tvOpd = itemView.findViewById(R.id.tvBlockedOpd);
            btnUnblock = itemView.findViewById(R.id.btnUnblock);
        }
    }
}

