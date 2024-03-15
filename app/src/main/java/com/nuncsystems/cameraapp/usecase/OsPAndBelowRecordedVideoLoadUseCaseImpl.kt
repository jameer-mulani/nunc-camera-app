package com.nuncsystems.cameraapp.usecase

import com.nuncsystems.cameraapp.model.RecordedVideo
import com.nuncsystems.cameraapp.videolist.OsPAndBelowRecordedVideoLoader
import java.io.File
import javax.inject.Inject

/**
 * [SuspendUseCase] implementation to load the stored recorded files for device P and below
 */
class OsPAndBelowRecordedVideoLoadUseCaseImpl @Inject constructor() : OsPAndBelowRecordedVideoLoadUseCase {
    override suspend fun invoke(input: File): List<RecordedVideo> {
        val osPAndBelowRecordedVideoLoader = OsPAndBelowRecordedVideoLoader(file = input)
        return osPAndBelowRecordedVideoLoader.loadData()
    }
}