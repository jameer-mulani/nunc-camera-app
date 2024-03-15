package com.nuncsystems.cameraapp.usecase

import android.content.ContentResolver
import com.nuncsystems.cameraapp.model.RecordedVideo
import java.io.File

/**
 * Suspend use case is represent the application use cases, provides suspend function so that could utilise from
 * Coroutine thus can be executed asynchronously.
 */
interface SuspendUseCase<in Input, out Output> {
    suspend operator fun invoke(input: Input): Output
}

/**
 * Represent loading recorded files from device OS P and below.
 */
interface OsPAndBelowRecordedVideoLoadUseCase : SuspendUseCase<File, List<RecordedVideo>>

/**
 * Represent loading recorded files from device OS Q and below.
 */
interface OsPAndAboveRecordedVideoLoadUseCase : SuspendUseCase<ContentResolver, List<RecordedVideo>>