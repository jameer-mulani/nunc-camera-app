package com.nuncsystems.cameraapp.videolist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.RequestManager
import com.nuncsystems.cameraapp.R
import com.nuncsystems.cameraapp.databinding.VideoListItemBinding
import com.nuncsystems.cameraapp.model.RecordedVideo
import javax.inject.Inject

class VideoListAdapter @Inject constructor(private val glide: RequestManager) :
    RecyclerView.Adapter<VideoListAdapter.ItemViewHolder>() {

    var items: List<RecordedVideo> = emptyList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.video_list_item, parent, false)
        val videoListItemBinding = VideoListItemBinding.bind(view)
        return ItemViewHolder(binding = videoListItemBinding, glide)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    class ItemViewHolder(private val binding: VideoListItemBinding, val glide: RequestManager) :
        ViewHolder(binding.root) {
        fun bind(item: RecordedVideo) {
            binding.videoName.text = item.name
            glide.load(item.contentUri).into(binding.thumbnail)
            binding.executePendingBindings()
        }

    }

}