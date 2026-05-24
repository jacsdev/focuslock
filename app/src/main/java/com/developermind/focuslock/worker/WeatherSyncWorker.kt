package com.developermind.focuslock.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.developermind.focuslock.data.repository.WeatherRepository

class WeatherSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val city = inputData.getString(KEY_CITY) ?: return Result.failure()
        return WeatherRepository(applicationContext).fetchAndCache(city).fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }

    companion object {
        const val KEY_CITY = "city"
        const val WORK_TAG = "weather_sync"
        const val WORK_PERIODIC_NAME = "weather_sync_periodic"
        const val WORK_ONCE_NAME = "weather_sync_once"
    }
}
