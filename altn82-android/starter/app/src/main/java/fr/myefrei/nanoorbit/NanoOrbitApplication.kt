package fr.myefrei.nanoorbit

import android.app.Application
import fr.myefrei.nanoorbit.data.api.NanoOrbitApiClient
import fr.myefrei.nanoorbit.data.local.NanoOrbitDatabase
import fr.myefrei.nanoorbit.data.preferences.FavoritesPreferences
import fr.myefrei.nanoorbit.data.preferences.nanoOrbitDataStore
import fr.myefrei.nanoorbit.data.repository.NanoOrbitRepository
import fr.myefrei.nanoorbit.worker.CommunicationWindowNotificationWorker
import fr.myefrei.nanoorbit.worker.PendingFenetreSyncWorker
import org.osmdroid.config.Configuration

class NanoOrbitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().userAgentValue = packageName

        repository = NanoOrbitRepository(
            dao = NanoOrbitDatabase.getDatabase(this).nanoOrbitDao(),
            favoritesPreferences = FavoritesPreferences(nanoOrbitDataStore),
            api = NanoOrbitApiClient.create(),
            schedulePendingSync = { PendingFenetreSyncWorker.enqueue(this) }
        )

        CommunicationWindowNotificationWorker.createNotificationChannel(this)
        CommunicationWindowNotificationWorker.schedulePeriodicChecks(this)
        PendingFenetreSyncWorker.enqueue(this)
    }

    companion object {
        var repository: NanoOrbitRepository? = null
            private set
    }
}
