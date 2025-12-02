package com.example.where2upt.geo

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.where2upt.R

data class BuildingZone(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val radiusM: Double,
    @androidx.annotation.DrawableRes val backgroundRes: Int
)

val BUILDING_ZONES = listOf(
    BuildingZone("electro", "Electro", 45.74810, 21.23040, 55.0, R.drawable.bg_electro),
    BuildingZone("aspc", "ASPC", 45.74810, 21.23040, 55.0, R.drawable.bg_aspc),
    BuildingZone("chimie", "Chimie Parc",       45.74865, 21.23110, 55.0, R.drawable.bg_chimie)/*,
    BuildingZone("rectorat", "Rectorat", 45.74770, 21.22950, 60.0, R.drawable.bg_rectorat)*/
)

const val OFF_CAMPUS_ID = "OFF"

@Composable
fun CampusBackground(
    buildingId: String,
    modifier: Modifier = Modifier
) {
    val bgRes = when (buildingId) {
        "electro" -> R.drawable.bg_electro
        "aspc" -> R.drawable.bg_aspc
        "chimie" -> R.drawable.bg_chimie
        else -> R.drawable.bg_rectorat // off-campus / remote
    }

    Crossfade(
        targetState = bgRes,
        animationSpec = tween(durationMillis = 600) // fade smooth
    ) { res ->
        Image(
            painter = painterResource(id = res),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}
