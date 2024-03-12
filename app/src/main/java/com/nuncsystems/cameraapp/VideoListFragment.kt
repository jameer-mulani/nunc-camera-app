package com.nuncsystems.cameraapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.nuncsystems.cameraapp.databinding.FragmentVideoListBinding

/**
 * Fragment shows the list of recorded video files
 */
class VideoListFragment : Fragment() {

    private var _binding: FragmentVideoListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVideoListBinding.inflate(inflater, container, false)
        binding.run {
            fab.setOnClickListener {
                Snackbar.make(it, "Some action", Snackbar.LENGTH_SHORT).show()
            }
            toolbar.apply {
                title = getString(R.string.video_list_fragment_label)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}