package fr.myefrei.nanoorbit.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.nanoOrbitDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "nanoorbit_preferences"
)

class FavoritesPreferences(
    private val dataStore: DataStore<Preferences>
) {
    val favoriteSatelliteIds: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[FAVORITE_SATELLITES_KEY].orEmpty()
    }

    suspend fun toggleFavoriteSatellite(satelliteId: String) {
        dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITE_SATELLITES_KEY]
                ?.toMutableSet()
                ?: mutableSetOf()

            if (satelliteId in currentFavorites) {
                currentFavorites.remove(satelliteId)
            } else {
                currentFavorites.add(satelliteId)
            }

            preferences[FAVORITE_SATELLITES_KEY] = currentFavorites
        }
    }

    private companion object {
        val FAVORITE_SATELLITES_KEY = stringSetPreferencesKey("favorite_satellite_ids")
    }
}
