package com.nuncsystems.cameraapp.di

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import com.nuncsystems.cameraapp.usecase.OsPAndAboveRecordedVideoLoadUseCase
import com.nuncsystems.cameraapp.usecase.OsPAndAboveRecordedVideoLoadUseCaseImpl
import com.nuncsystems.cameraapp.usecase.OsPAndBelowRecordedVideoLoadUseCase
import com.nuncsystems.cameraapp.usecase.OsPAndBelowRecordedVideoLoadUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * ViewModel wide hilt dependency provision.
 */
@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    fun provideOsPAndBelowRecordedVideoLoadUseCase() =
        OsPAndBelowRecordedVideoLoadUseCaseImpl() as OsPAndBelowRecordedVideoLoadUseCase

    @SuppressLint("NewApi")
    @Provides
    fun provideOsPAndAboveRecordedVideoLoadUseCase() =
        OsPAndAboveRecordedVideoLoadUseCaseImpl() as OsPAndAboveRecordedVideoLoadUseCase

    @Provides
    fun provideFilePathForOs28() =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context) = context.contentResolver


}