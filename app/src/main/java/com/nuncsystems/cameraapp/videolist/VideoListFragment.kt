package com.nuncsystems.cameraapp.videolist

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nuncsystems.cameraapp.R
import com.nuncsystems.cameraapp.databinding.FragmentVideoListBinding
import com.nuncsystems.cameraapp.util.isAtLeastP
import com.nuncsystems.cameraapp.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Fragment shows the list of recorded video files
 */
@AndroidEntryPoint
class VideoListFragment @Inject constructor(private val videoListAdapter: VideoListAdapter): Fragment() {
    companion object {
        private const val TAG = "VideoListFragment"
        private val REQUIRED_PERMISSIONS = mutableListOf<String>().also {
            if (isAtLeastP()){
                it.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){permission->
        var hasAllPermissionGranted = true
        permission.entries.forEach {eachPermission->
            if (eachPermission.key in REQUIRED_PERMISSIONS && !eachPermission.value){
                hasAllPermissionGranted = false
            }
        }
        if (!hasAllPermissionGranted){
            val message = "Please grant permission."
            requireActivity().showToast(message)
        }else{
            subscribeToVideoListDataInternal()
        }
    }
    private var binding: FragmentVideoListBinding? = null
    private lateinit var videoListViewModel: VideoListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_video_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoListViewModel = ViewModelProvider(requireActivity())[VideoListViewModel::class.java]
        binding = FragmentVideoListBinding.bind(view)
        binding?.run {
            isListEmpty = videoListAdapter.items.isEmpty()
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

        subscribeToVideoListData()
    }

    private fun subscribeToVideoListData(){
        if (isAtLeastP()){
            permissionLauncher.launch(REQUIRED_PERMISSIONS)
        }else{
            subscribeToVideoListDataInternal()
        }
    }

    override fun onStart() {
        super.onStart()
        subscribeToVideoListData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun subscribeToVideoListDataInternal() {
        videoListViewModel.videoListLiveData.observe(requireActivity()) {
            binding?.isListEmpty = it.isEmpty()
            videoListAdapter.apply {
                items = it
                notifyDataSetChanged()
            }
        }
        videoListViewModel.also {
            if (!isAtLeastP()) {
                it.contentResolver = requireActivity().contentResolver
            }
            it.loadData()
        }
    }

}