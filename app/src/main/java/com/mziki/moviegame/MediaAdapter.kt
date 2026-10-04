package com.mziki.moviegame

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MediaAdapter(
    private val items: List<MediaItem>,
    private val accent: Int
) : RecyclerView.Adapter<MediaAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val index: TextView = view.findViewById(R.id.itemIndex)
        val title: TextView = view.findViewById(R.id.itemTitle)
        val subtitle: TextView = view.findViewById(R.id.itemSubtitle)
        val image: ImageView = view.findViewById(R.id.itemImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.index.text = "#${position + 1}"
        holder.index.setTextColor(accent)
        holder.title.text = item.title
        holder.subtitle.text = item.subtitle
        holder.subtitle.visibility = if (item.subtitle.isBlank()) View.GONE else View.VISIBLE

        if (!item.imageUrl.isNullOrBlank()) {
            Glide.with(holder.image.context)
                .load(item.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(holder.image)
        } else {
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery)
            holder.image.setBackgroundColor(Color.argb(40, Color.red(accent), Color.green(accent), Color.blue(accent)))
        }
    }

    override fun getItemCount() = items.size
}
