package fr.myefrei.nanoorbit.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.myefrei.nanoorbit.data.models.FenetreCom
import fr.myefrei.nanoorbit.data.models.FormatCubeSat
import fr.myefrei.nanoorbit.data.models.PendingFenetrePlanification
import fr.myefrei.nanoorbit.data.models.PendingSyncStatus
import fr.myefrei.nanoorbit.data.models.Satellite
import fr.myefrei.nanoorbit.data.models.StatutFenetre
import fr.myefrei.nanoorbit.data.models.StatutSatellite
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "satellites")
data class SatelliteEntity(
    @PrimaryKey
    val idSatellite: String,
    val nomSatellite: String,
    val dateLancementIso: String,
    val masseKg: Double,
    val formatCubesat: String,
    val statut: String,
    val dureeViePrevueMois: Int,
    val capaciteBatterieWh: Double,
    val idOrbite: Int,
    val lastUpdatedEpochMillis: Long
)

@Entity(tableName = "fenetres_com")
data class FenetreEntity(
    @PrimaryKey
    val idFenetre: Int,
    val datetimeDebutIso: String,
    val debutFormate: String?,
    val dureeSecondes: Int,
    val dureeFormatee: String?,
    val elevationMaxDegres: Double,
    val volumeDonneesMb: Double?,
    val statut: String,
    val idSatellite: String,
    val nomSatellite: String?,
    val codeStation: String,
    val nomStation: String?,
    val idCentre: Int?,
    val nomCentre: String?,
    val lastUpdatedEpochMillis: Long
)

@Entity(tableName = "pending_fenetres")
data class PendingFenetreEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val satelliteId: String,
    val codeStation: String,
    val datetimeDebutIso: String,
    val dureeSecondes: Int,
    val elevationMaxDegres: Double,
    val status: String,
    val createdAtEpochMillis: Long,
    val lastError: String?,
    val retryCount: Int
)

@Dao
interface NanoOrbitDao {
    @Query("SELECT * FROM satellites ORDER BY idSatellite")
    fun getSatelliteEntities(): List<SatelliteEntity>

    @Query("SELECT MAX(lastUpdatedEpochMillis) FROM satellites")
    fun getSatellitesLastUpdatedAt(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertSatelliteEntities(satellites: List<SatelliteEntity>)

    @Query("DELETE FROM satellites")
    fun clearSatellites(): Int

    @Query("SELECT * FROM fenetres_com ORDER BY datetimeDebutIso")
    fun getFenetreEntities(): List<FenetreEntity>

    @Query("SELECT * FROM fenetres_com ORDER BY datetimeDebutIso")
    fun observeFenetreEntities(): Flow<List<FenetreEntity>>

    @Query("SELECT MAX(lastUpdatedEpochMillis) FROM fenetres_com")
    fun getFenetresLastUpdatedAt(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertFenetreEntities(fenetres: List<FenetreEntity>)

    @Query("DELETE FROM fenetres_com")
    fun clearFenetres(): Int

    @Query("SELECT * FROM pending_fenetres ORDER BY createdAtEpochMillis")
    fun observePendingFenetreEntities(): Flow<List<PendingFenetreEntity>>

    @Query("SELECT * FROM pending_fenetres WHERE status = 'PENDING' ORDER BY createdAtEpochMillis")
    fun getPendingFenetreSyncCandidates(): List<PendingFenetreEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPendingFenetreEntity(entity: PendingFenetreEntity): Long

    @Query("DELETE FROM pending_fenetres WHERE localId = :localId")
    fun deletePendingFenetreEntity(localId: Long): Int

    @Query(
        """
        UPDATE pending_fenetres
           SET status = :status,
               lastError = :lastError,
               retryCount = retryCount + 1
         WHERE localId = :localId
        """
    )
    fun updatePendingFenetreStatus(
        localId: Long,
        status: String,
        lastError: String?
    ): Int
}

@Database(
    entities = [
        SatelliteEntity::class,
        FenetreEntity::class,
        PendingFenetreEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class NanoOrbitDatabase : RoomDatabase() {
    abstract fun nanoOrbitDao(): NanoOrbitDao

    companion object {
        @Volatile
        private var INSTANCE: NanoOrbitDatabase? = null

        fun getDatabase(context: Context): NanoOrbitDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NanoOrbitDatabase::class.java,
                    "nanoorbit.db"
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { database -> INSTANCE = database }
            }
        }
    }
}

fun Satellite.toEntity(lastUpdatedEpochMillis: Long): SatelliteEntity {
    return SatelliteEntity(
        idSatellite = idSatellite,
        nomSatellite = nomSatellite,
        dateLancementIso = dateLancement.toString(),
        masseKg = masseKg,
        formatCubesat = formatCubesat.name,
        statut = statut.name,
        dureeViePrevueMois = dureeViePrevueMois,
        capaciteBatterieWh = capaciteBatterieWh,
        idOrbite = idOrbite,
        lastUpdatedEpochMillis = lastUpdatedEpochMillis
    )
}

fun SatelliteEntity.toModel(): Satellite {
    return Satellite(
        idSatellite = idSatellite,
        nomSatellite = nomSatellite,
        dateLancement = LocalDate.parse(dateLancementIso),
        masseKg = masseKg,
        formatCubesat = FormatCubeSat.valueOf(formatCubesat),
        statut = StatutSatellite.valueOf(statut),
        dureeViePrevueMois = dureeViePrevueMois,
        capaciteBatterieWh = capaciteBatterieWh,
        idOrbite = idOrbite
    )
}

fun FenetreCom.toEntity(lastUpdatedEpochMillis: Long): FenetreEntity {
    return FenetreEntity(
        idFenetre = idFenetre,
        datetimeDebutIso = datetimeDebut.toString(),
        debutFormate = debutFormate,
        dureeSecondes = dureeSecondes,
        dureeFormatee = dureeFormatee,
        elevationMaxDegres = elevationMaxDegres,
        volumeDonneesMb = volumeDonneesMb,
        statut = statut.name,
        idSatellite = idSatellite,
        nomSatellite = nomSatellite,
        codeStation = codeStation,
        nomStation = nomStation,
        idCentre = idCentre,
        nomCentre = nomCentre,
        lastUpdatedEpochMillis = lastUpdatedEpochMillis
    )
}

fun FenetreEntity.toModel(): FenetreCom {
    return FenetreCom(
        idFenetre = idFenetre,
        datetimeDebut = LocalDateTime.parse(datetimeDebutIso),
        debutFormate = debutFormate,
        dureeSecondes = dureeSecondes,
        dureeFormatee = dureeFormatee,
        elevationMaxDegres = elevationMaxDegres,
        volumeDonneesMb = volumeDonneesMb,
        statut = StatutFenetre.valueOf(statut),
        idSatellite = idSatellite,
        nomSatellite = nomSatellite,
        codeStation = codeStation,
        nomStation = nomStation,
        idCentre = idCentre,
        nomCentre = nomCentre
    )
}

fun PendingFenetreEntity.toModel(): PendingFenetrePlanification {
    return PendingFenetrePlanification(
        localId = localId,
        satelliteId = satelliteId,
        codeStation = codeStation,
        datetimeDebut = LocalDateTime.parse(datetimeDebutIso),
        dureeSecondes = dureeSecondes,
        elevationMaxDegres = elevationMaxDegres,
        status = PendingSyncStatus.valueOf(status),
        createdAtEpochMillis = createdAtEpochMillis,
        lastError = lastError,
        retryCount = retryCount
    )
}
