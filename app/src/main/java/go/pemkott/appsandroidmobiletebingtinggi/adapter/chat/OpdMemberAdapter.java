package go.pemkott.appsandroidmobiletebingtinggi.adapter.chat;

import android.content.Context;
import android.text.TextUtils;
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

import de.hdodenhof.circleimageview.CircleImageView;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.OpdGroupProfileResponse;

public class OpdMemberAdapter extends RecyclerView.Adapter<OpdMemberAdapter.MemberViewHolder> {

    public interface OnMemberClickListener {
        void onMemberClick(OpdGroupProfileResponse.OpdMemberItem member);
    }

    private final Context context;
    private final List<OpdGroupProfileResponse.OpdMemberItem> originalList;
    private final List<OpdGroupProfileResponse.OpdMemberItem> filteredList;
    private final OnMemberClickListener listener;

    public OpdMemberAdapter(Context context, List<OpdGroupProfileResponse.OpdMemberItem> list, OnMemberClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    public void filter(String query) {
        filteredList.clear();
        if (TextUtils.isEmpty(query)) {
            filteredList.addAll(originalList);
        } else {
            String lower = query.toLowerCase().trim();
            for (OpdGroupProfileResponse.OpdMemberItem item : originalList) {
                String nama = item.getNama() != null ? item.getNama().toLowerCase() : "";
                String nip = item.getNip() != null ? item.getNip() : "";
                String jabatan = item.getJabatan() != null ? item.getJabatan().toLowerCase() : "";
                if (nama.contains(lower) || nip.contains(lower) || jabatan.contains(lower)) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        OpdGroupProfileResponse.OpdMemberItem item = filteredList.get(position);

        holder.tvMemberName.setText(item.getNama() != null ? item.getNama() : "Pegawai");
        holder.tvMemberJabatan.setText(!TextUtils.isEmpty(item.getJabatan()) ? item.getJabatan() : "Pegawai OPD");
        holder.tvMemberNip.setText("NIP: " + (!TextUtils.isEmpty(item.getNip()) ? item.getNip() : "-"));

        if (item.isMe()) {
            holder.tvMeBadge.setVisibility(View.VISIBLE);
            holder.ivStartDirectChat.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
        } else {
            holder.tvMeBadge.setVisibility(View.GONE);
            holder.ivStartDirectChat.setVisibility(View.VISIBLE);
            View.OnClickListener clickListener = v -> {
                if (listener != null) {
                    listener.onMemberClick(item);
                }
            };
            holder.itemView.setOnClickListener(clickListener);
            holder.ivStartDirectChat.setOnClickListener(clickListener);
        }

        if (!TextUtils.isEmpty(item.getAvatarUrl())) {
            Glide.with(context)
                    .load(item.getAvatarUrl())
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
                    .into(holder.civMemberAvatar);
        } else {
            holder.civMemberAvatar.setImageResource(R.drawable.ic_baseline_person_24);
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        CircleImageView civMemberAvatar;
        TextView tvMemberName, tvMeBadge, tvMemberJabatan, tvMemberNip;
        ImageView ivStartDirectChat;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            civMemberAvatar = itemView.findViewById(R.id.civMemberAvatar);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMeBadge = itemView.findViewById(R.id.tvMeBadge);
            tvMemberJabatan = itemView.findViewById(R.id.tvMemberJabatan);
            tvMemberNip = itemView.findViewById(R.id.tvMemberNip);
            ivStartDirectChat = itemView.findViewById(R.id.ivStartDirectChat);
        }
    }
}

