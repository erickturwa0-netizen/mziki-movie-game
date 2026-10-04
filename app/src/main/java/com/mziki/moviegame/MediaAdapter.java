package com.mziki.moviegame;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.VH> {

    private final List<MediaItem> items;
    private final int accent;

    public MediaAdapter(List<MediaItem> items, int accent) {
        this.items = items;
        this.accent = accent;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView index;
        TextView title;
        TextView subtitle;
        ImageView image;

        VH(View view) {
            super(view);
            index = view.findViewById(R.id.itemIndex);
            title = view.findViewById(R.id.itemTitle);
            subtitle = view.findViewById(R.id.itemSubtitle);
            image = view.findViewById(R.id.itemImage);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_media, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        MediaItem item = items.get(position);
        holder.index.setText("#" + (position + 1));
        holder.index.setTextColor(accent);
        holder.title.setText(item.title);
        holder.subtitle.setText(item.subtitle);
        holder.subtitle.setVisibility(
                item.subtitle == null || item.subtitle.isEmpty() ? View.GONE : View.VISIBLE
        );

        if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
            Glide.with(holder.image.getContext())
                    .load(item.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(holder.image);
        } else {
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery);
            holder.image.setBackgroundColor(Color.argb(
                    40,
                    Color.red(accent),
                    Color.green(accent),
                    Color.blue(accent)
            ));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
