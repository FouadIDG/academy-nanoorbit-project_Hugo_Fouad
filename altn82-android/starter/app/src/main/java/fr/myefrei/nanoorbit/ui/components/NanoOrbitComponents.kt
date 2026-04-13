package fr.myefrei.nanoorbit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.myefrei.nanoorbit.data.mock.MockData
import fr.myefrei.nanoorbit.data.models.FenetreCom
import fr.myefrei.nanoorbit.data.models.Instrument
import fr.myefrei.nanoorbit.data.models.Satellite
import fr.myefrei.nanoorbit.data.models.StatutFenetre
import fr.myefrei.nanoorbit.data.models.StatutSatellite
import fr.myefrei.nanoorbit.ui.theme.NanoOrbitTheme
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SatelliteCard(
    satellite: Satellite,
    modifier: Modifier = Modifier,
    orbiteLabel: String = "Orbite inconnue",
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onClick: () -> Unit
) {
    val isDesorbite = satellite.statut == StatutSatellite.DESORBITE

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isDesorbite) 0.62f else 1f)
            // Q3: on désactive l'interaction pour un satellite désorbité et on doit refaire
            // la même vérification avant toute planification, comme le trigger Oracle trg_valider_fenetre.
            .clickable(enabled = !isDesorbite, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDesorbite) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = satellite.nomSatellite,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${satellite.idSatellite} · ${satellite.formatCubesat.libelleOracle}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (isFavorite) {
                                Icons.Filled.Star
                            } else {
                                Icons.Outlined.StarBorder
                            },
                            contentDescription = if (isFavorite) {
                                "Retirer des favoris"
                            } else {
                                "Ajouter aux favoris"
                            },
                            tint = if (isFavorite) {
                                Color(0xFFF9A825)
                            } else {
                                MaterialTheme.colorScheme.outline
                            }
                        )
                    }
                    StatusBadge(statut = satellite.statut)
                }
            }

            Text(
                text = orbiteLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isDesorbite) {
                Text(
                    text = "DÉSORBITÉ · aucune nouvelle planification autorisée",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    statut: StatutSatellite,
    modifier: Modifier = Modifier
) {
    val badgeColor = when (statut) {
        StatutSatellite.OPERATIONNEL -> Color(0xFF1B5E20)
        StatutSatellite.EN_VEILLE -> Color(0xFFF57C00)
        StatutSatellite.DEFAILLANT -> Color(0xFFB71C1C)
        StatutSatellite.DESORBITE -> Color(0xFF616161)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = badgeColor.copy(alpha = 0.12f),
        contentColor = badgeColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = badgeColor, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statut.libelleOracle,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun FenetreCard(
    fenetre: FenetreCom,
    nomStation: String,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm", Locale.FRENCH)
    val satelliteLabel = fenetre.nomSatellite?.let { nomSatellite ->
        "$nomSatellite (${fenetre.idSatellite})"
    } ?: fenetre.idSatellite
    val dureeLabel = fenetre.dureeFormatee ?: formatDuration(fenetre.dureeSecondes)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fenetre.datetimeDebut.format(formatter),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                FenetreStatusBadge(statut = fenetre.statut)
            }

            Text(
                text = "$nomStation · $dureeLabel · $satelliteLabel",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            fenetre.nomCentre?.let { nomCentre ->
                Text(
                    text = "Centre de contrôle : $nomCentre",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            fenetre.volumeDonneesMb?.let { volume ->
                Text(
                    text = "Volume téléchargé : ${volume.toInt()} MB",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun InstrumentItem(
    instrument: Instrument,
    etatFonctionnement: String,
    modifier: Modifier = Modifier
) {
    val etatColor = when (etatFonctionnement) {
        "Nominal" -> Color(0xFF1B5E20)
        "Dégradé" -> Color(0xFFF57C00)
        else -> Color(0xFFB71C1C)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = instrument.typeInstrument,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = instrument.modele,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Résolution : ${instrument.resolution?.let { "$it m" } ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = etatColor.copy(alpha = 0.12f),
                contentColor = etatColor
            ) {
                Text(
                    text = etatFonctionnement,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun CacheStatusBanner(
    title: String = "Mode hors-ligne",
    cacheAgeLabel: String?,
    isMockMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (isMockMode) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.errorContainer
        },
        contentColor = if (isMockMode) {
            MaterialTheme.colorScheme.onTertiaryContainer
        } else {
            MaterialTheme.colorScheme.onErrorContainer
        }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = cacheAgeLabel ?: if (isMockMode) {
                    "Données locales MockData"
                } else {
                    "Données locales Room"
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun FenetreStatusBadge(
    statut: StatutFenetre,
    modifier: Modifier = Modifier
) {
    val badgeColor = when (statut) {
        StatutFenetre.PLANIFIEE -> Color(0xFF1565C0)
        StatutFenetre.REALISEE -> Color(0xFF1B5E20)
        StatutFenetre.ANNULEE -> Color(0xFFB71C1C)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = badgeColor.copy(alpha = 0.12f),
        contentColor = badgeColor
    ) {
        Text(
            text = statut.libelleOracle,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatDuration(dureeSecondes: Int): String {
    val minutes = dureeSecondes / 60
    val secondes = dureeSecondes % 60
    return "${minutes} min ${secondes.toString().padStart(2, '0')} s"
}

@Preview(showBackground = true)
@Composable
private fun SatelliteCardPreview() {
    NanoOrbitTheme {
        SatelliteCard(
            satellite = MockData.satellites.first(),
            orbiteLabel = "SSO · 550 km · Polaire globale — Europe / Arctique",
            modifier = Modifier.padding(16.dp),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusBadgePreview() {
    NanoOrbitTheme {
        StatusBadge(
            statut = StatutSatellite.OPERATIONNEL,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FenetreCardPreview() {
    NanoOrbitTheme {
        FenetreCard(
            fenetre = MockData.fenetresCom.first(),
            nomStation = MockData.stationsSol.first().nomStation,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InstrumentItemPreview() {
    NanoOrbitTheme {
        InstrumentItem(
            instrument = MockData.instruments.first(),
            etatFonctionnement = "Nominal",
            modifier = Modifier.padding(16.dp)
        )
    }
}
