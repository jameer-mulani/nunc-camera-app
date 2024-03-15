package com.nuncsystems.cameraapp.view

import android.widget.ImageView
import androidx.databinding.BindingAdapter

/**
 * Binding adapter for [ImageView] to set Imageview selected or not
 * @param iv imageview
 * @param state selected state for imageview, if true then view.isSet = true otherwise false.
 */
@BindingAdapter("isSelected", requireAll = true)
fun bindFlashButtonState(iv : ImageView, state : Boolean){
    iv.isSelected = state
}