package com.nuncsystems.cameraapp.view

import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("isSelected", requireAll = true)
fun bindFlashButtonState(iv : ImageView, state : Boolean){
    iv.isSelected = state
}