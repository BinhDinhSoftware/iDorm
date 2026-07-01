package com.bdsoftware.idorm.core.model

data class AppConfig(
    val androidForceUpdate: Boolean = false,
    val androidLatestVersion: String = "1.0.0",
    val androidMinVersion: String = "1.0.0",
    val maintenanceMessage: String = "",
    val maintenanceMode: Boolean = false
)
