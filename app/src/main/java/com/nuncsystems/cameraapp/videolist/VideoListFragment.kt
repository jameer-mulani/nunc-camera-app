package com.nuncsystems.cameraapp.videolist

import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nuncsystems.cameraapp.R
import com.nuncsystems.cameraapp.databinding.FragmentVideoListBinding

/**
 * Fragment shows the list of recorded video files
 */
class VideoListFragment : Fragment() {

    companion object {
        private const val TAG = "VideoListFragment"
    }

    private var _binding: FragmentVideoListBinding? = null
    private val binding get() = _binding!!
    private val videoListAdapter by lazy { VideoListAdapter() }
    private val videoListContentProviderLiveData by lazy {
        VideoListContentProviderLiveData(
            requireActivity(),
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVideoListBinding.inflate(inflater, container, false)
        binding.run {
            binding.isListEmpty = videoListAdapter.items.isEmpty()
            fab.setOnClickListener {
                findNavController().navigate(R.id.action_FirstFragment_to_videoCapturerActivity)
            }
            toolbar.apply {
                title = getString(R.string.video_list_fragment_label)
            }
            videoList.apply {
                layoutManager = LinearLayoutManager(requireActivity())
                setHasFixedSize(true)
                adapter = videoListAdapter
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        videoListContentProviderLiveData.observe(requireActivity()) {
            it.forEach { item -> Log.d(TAG, "onDownloadsData: $item") }
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}