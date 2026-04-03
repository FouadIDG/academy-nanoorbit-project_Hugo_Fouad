package fr.myefrei.nanoorbit.data.repository

import fr.myefrei.nanoorbit.data.api.NanoOrbitApi
import fr.myefrei.nanoorbit.data.mock.MockData
import fr.myefrei.nanoorbit.data.models.FenetreCom
import fr.myefrei.nanoorbit.data.models.Instrument
import fr.myefrei.nanoorbit.data.models.Satellite
import kotlinx.coroutines.delay

class NanoOrbitRepository(
    private val api: NanoOrbitApi? = null
) {
    suspend fun getSatellites(): List<Satellite> {
        delay(NETWORK_LATENCY_MS)
        return api?.getSatellites() ?: MockData.satellites
    }

    suspend fun getSatelliteInstruments(satelliteId: String): List<Instrument> {
        delay(NETWORK_LATENCY_MS)

        api?.let { return it.getSatelliteInstruments(satelliteId) }

        val refsForSatellite = MockData.embarquements
            .filter { embarquement -> embarquement.idSatellite == satelliteId }
            .map { embarquement -> embarquement.refInstrument }
            .toSet()

        return MockData.instruments.filter { instrument ->
            instrument.refInstrument in refsForSatellite
        }
    }

    suspend fun getFenetres(): List<FenetreCom> {
        delay(NETWORK_LATENCY_MS)
        return api?.getFenetres() ?: MockData.fenetresCom
    }

    /**
     * RG-F04 côté client : la durée d'une fenêtre doit rester dans [1, 900] secondes.
     * C'est le miroir applicatif du CHECK Oracle chk_fenetre_duree et de la validation PL/SQL.
     */
    fun validateFenetreDuration(dureeSecondes: Int): Result<Unit> {
        return if (dureeSecondes in MIN_WINDOW_DURATION_SECONDS..MAX_WINDOW_DURATION_SECONDS) {
            Result.success(Unit)
        } else {
            Result.failure(
                IllegalArgumentException(
                    "Durée invalide : elle doit être comprise entre 1 et 900 secondes."
                )
            )
        }
    }

    private companion object {
        const val NETWORK_LATENCY_MS = 500L
        const val MIN_WINDOW_DURATION_SECONDS = 1
        const val MAX_WINDOW_DURATION_SECONDS = 900
    }
}
