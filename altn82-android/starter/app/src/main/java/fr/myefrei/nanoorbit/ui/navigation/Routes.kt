package fr.myefrei.nanoorbit.ui.navigation

object Routes {
    const val DASHBOARD = "dashboard"
    const val PLANNING = "planning"
    const val MAP = "map"
    const val DETAIL_PATTERN = "detail/{satelliteId}"

    fun detail(satelliteId: String): String = "detail/$satelliteId"
}
