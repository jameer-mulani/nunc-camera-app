package com.nuncsystems.cameraapp.usecase

import com.nuncsystems.cameraapp.model.RecordedVideo
import com.nuncsystems.cameraapp.videolist.OsPAndBelowRecordedVideoLoader
import java.io.File
import javax.inject.Inject

class OsPAndBelowRecordedVideoLoadUseCaseImpl @Inject constructor() : OsPAndBelowRecordedVideoLoadUseCase {
    override suspend fun invoke(input: File): List<RecordedVideo> {
        val osPAndBelowRecordedVideoLoader = OsPAndBelowRecordedVideoLoader(file = input)
        return osPAndBelowRecordedVideoLoader.loadData()
    }
}