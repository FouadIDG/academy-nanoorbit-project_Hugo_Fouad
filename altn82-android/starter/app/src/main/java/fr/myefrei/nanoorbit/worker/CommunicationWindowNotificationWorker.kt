package fr.myefrei.nanoorbit.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import fr.myefrei.nanoorbit.data.local.NanoOrbitDatabase
import fr.myefrei.nanoorbit.data.local.toModel
import fr.myefrei.nanoorbit.data.models.FenetreCom
import fr.myefrei.nanoorbit.data.models.StatutFenetre
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class CommunicationWindowNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val now = LocalDateTime.now()
        val upcomingLimit = now.plusMinutes(NOTIFICATION_LOOKAHEAD_MINUTES)

        val dao = NanoOrbitDatabase.getDatabase(applicationContext).nanoOrbitDao()
        val fenetres = dao.getFenetreEntities().map { fenetreEntity ->
            fenetreEntity.toModel()
        }

        fenetres
            .filter { fenetre ->
                fenetre.statut == StatutFenetre.PLANIFIEE &&
                    !fenetre.datetimeDebut.isBefore(now) &&
                    !fenetre.datetimeDebut.isAfter(upcomingLimit)
            }
            .forEach { fenetre ->
                sendWindowNotification(fenetre = fenetre, now = now)
            }

        return Result.success()
    }

    private fun sendWindowNotification(
        fenetre: FenetreCom,
        now: LocalDateTime
    ) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val minutesBeforePass = Duration.between(now, fenetre.datetimeDebut)
            .toMinutes()
            .coerceAtLeast(0)
        val satelliteLabel = fenetre.nomSatellite ?: fenetre.idSatellite
        val stationLabel = fenetre.nomStation ?: fenetre.codeStation
        val dureeLabel = fenetre.dureeFormatee ?: "${fenetre.dureeSecondes} s"

        val notification = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle("Passage imminent $satelliteLabel")
            .setContentText(
                "$stationLabel · dans $minutesBeforePass min · $dureeLabel"
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(
            NOTIFICATION_ID_BASE + fenetre.idFenetre,
            notification
        )
    }

    companion object {
        const val CHANNEL_ID = "nanoorbit_windows"
        private const val CHANNEL_NAME = "Passages NanoOrbit"
        private const val UNIQUE_WORK_NAME = "nanoorbit-window-notifications"
        private const val NOTIFICATION_LOOKAHEAD_MINUTES = 15L
        private const val NOTIFICATION_ID_BASE = 10_000

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications 15 minutes avant les fenêtres de communication."
            }

            val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        fun schedulePeriodicChecks(context: Context) {
            val workRequest =
                PeriodicWorkRequestBuilder<CommunicationWindowNotificationWorker>(
                    15,
                    TimeUnit.MINUTES
                ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }
}
