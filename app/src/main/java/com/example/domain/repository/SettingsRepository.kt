package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColors: Boolean = true,
    val sbpMerchantId: String = "",
    val sbpApiKey: String = "",
    val sbpCustomEndpoint: String = ""
)

interface SettingsRepository {
    val settingsFlow: Flow<AppSettings>
    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun setDynamicColors(enabled: Boolean)
    suspend fun saveSbpConfig(merchantId: String, apiKey: String, customEndpoint: String)
}
