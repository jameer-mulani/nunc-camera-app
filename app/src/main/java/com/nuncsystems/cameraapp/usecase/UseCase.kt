package com.nuncsystems.cameraapp.usecase

import android.content.ContentResolver
import com.nuncsystems.cameraapp.model.RecordedVideo
import java.io.File

interface SuspendUseCase<in Input, out Output> {
    suspend operator fun invoke(input: Input): Output
}

interface OsPAndBelowRecordedVideoLoadUseCase : SuspendUseCase<File, List<RecordedVideo>>
interface OsPAndAboveRecordedVideoLoadUseCase : SuspendUseCase<ContentResolver, List<RecordedVideo>>