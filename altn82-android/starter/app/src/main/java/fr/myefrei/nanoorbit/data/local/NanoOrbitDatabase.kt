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
import fr.myefrei.nanoorbit.data.models.Satellite
import fr.myefrei.nanoorbit.data.models.StatutFenetre
import fr.myefrei.nanoorbit.data.models.StatutSatellite
import java.time.LocalDate
import java.time.LocalDateTime

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

    @Query("SELECT MAX(lastUpdatedEpochMillis) FROM fenetres_com")
    fun getFenetresLastUpdatedAt(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertFenetreEntities(fenetres: List<FenetreEntity>)

    @Query("DELETE FROM fenetres_com")
    fun clearFenetres(): Int
}

@Database(
    entities = [SatelliteEntity::class, FenetreEntity::class],
    version = 2,
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
