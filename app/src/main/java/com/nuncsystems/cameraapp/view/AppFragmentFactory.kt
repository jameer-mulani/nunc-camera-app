package com.nuncsystems.cameraapp.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.nuncsystems.cameraapp.videolist.VideoListAdapter
import com.nuncsystems.cameraapp.videolist.VideoListFragment
import javax.inject.Inject

class AppFragmentFactory @Inject constructor(
    private val videoListAdapter: VideoListAdapter
) :FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(className){
            VideoListFragment::class.java.name -> VideoListFragment(videoListAdapter)
            else -> super.instantiate(classLoader, className)
        }
    }
}