package fr.myefrei.nanoorbit.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import fr.myefrei.nanoorbit.data.api.NanoOrbitApiClient
import fr.myefrei.nanoorbit.data.local.NanoOrbitDatabase
import fr.myefrei.nanoorbit.data.repository.NanoOrbitRepository
import java.util.concurrent.TimeUnit

class PendingFenetreSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val repository = NanoOrbitRepository(
            dao = NanoOrbitDatabase.getDatabase(applicationContext).nanoOrbitDao(),
            api = NanoOrbitApiClient.create()
        )
        val result = repository.syncPendingFenetres()

        return if (result.hasTransientFailure) {
            Result.retry()
        } else {
            Result.success()
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "nanoorbit-pending-fenetre-sync"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<PendingFenetreSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}
