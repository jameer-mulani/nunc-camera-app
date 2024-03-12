package com.nuncsystems.cameraapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nuncsystems.cameraapp.databinding.ActivityMainBinding
import com.nuncsystems.cameraapp.view.AppFragmentFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var appFragmentFactory: AppFragmentFactory

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.fragmentFactory = appFragmentFactory
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}