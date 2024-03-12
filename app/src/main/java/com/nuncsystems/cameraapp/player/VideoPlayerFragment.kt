package com.nuncsystems.cameraapp.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.nuncsystems.cameraapp.R
import com.nuncsystems.cameraapp.databinding.FragmentVideoPlayerBinding
import com.nuncsystems.cameraapp.util.isAtLeastM
import com.nuncsystems.cameraapp.util.isGreaterThanM


class VideoPlayerFragment : Fragment() {


    private lateinit var binding: FragmentVideoPlayerBinding
    private var player: ExoPlayer? = null
    private var mediaToPlay: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //content://media/external/downloads/1635
        mediaToPlay = arguments?.getString("uri")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_video_player, container, false)
        binding = FragmentVideoPlayerBinding.bind(view)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initExoPlayer() {
        player = ExoPlayer.Builder(requireActivity())
            .build().also { player ->
                binding.playerView.player = player
                val mediaItem = mediaToPlay?.let { MediaItem.fromUri(it) }
                mediaItem?.let { player.setMediaItem(it) }
                player.prepare()
            }
    }

    override fun onStart() {
        super.onStart()
        if (isGreaterThanM()){
            initExoPlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (isAtLeastM() || player == null){
            initExoPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isAtLeastM()){
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (isGreaterThanM()){
            releasePlayer()
        }
    }

    private fun hideSystemUi(){
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        WindowInsetsControllerCompat(requireActivity().window, binding.playerView).let { controller->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun releasePlayer(){
        player?.let {exoPlayer ->
            exoPlayer.release()
        }
        player = null
    }
}