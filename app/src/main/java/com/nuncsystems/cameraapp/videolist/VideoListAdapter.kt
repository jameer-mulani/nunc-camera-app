package com.nuncsystems.cameraapp.videolist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.nuncsystems.cameraapp.databinding.VideoListItemBinding
import com.nuncsystems.cameraapp.model.RecordedVideo

class VideoListAdapter(var items: List<RecordedVideo> = emptyList()) :
    RecyclerView.Adapter<VideoListAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val videoListItemBinding = VideoListItemBinding.inflate(LayoutInflater.from(parent.context))
        return ItemViewHolder(binding = videoListItemBinding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    class ItemViewHolder(private val binding: VideoListItemBinding) : ViewHolder(binding.root) {
        fun bind(item: RecordedVideo) {
            binding.videoName.text = item.name
            binding.executePendingBindings()
        }

    }

}