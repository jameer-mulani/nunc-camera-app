package com.nuncsystems.cameraapp

import android.content.Context
import androidx.navigation.fragment.NavHostFragment
import com.nuncsystems.cameraapp.view.AppFragmentFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BaseNavHostFragment : NavHostFragment() {

    @Inject
    lateinit var fragmentFactory: AppFragmentFactory

    override fun onAttach(context: Context) {
        super.onAttach(context)
        childFragmentManager.fragmentFactory = fragmentFactory
    }

}