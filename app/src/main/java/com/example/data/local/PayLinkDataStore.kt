package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.Recipient
import com.example.domain.repository.AppSettings
import com.example.domain.repository.RecipientRepository
import com.example.domain.repository.SettingsRepository
import com.example.domain.repository.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.payLinkDataStore: DataStore<Preferences> by preferencesDataStore(name = "paylink_prefs")

class PayLinkDataStore(private val context: Context) : RecipientRepository, SettingsRepository {

    private object PreferencesKeys {
        val YOOMONEY_WALLET = stringPreferencesKey("yoomoney_wallet")
        val PHONE_NUMBER = stringPreferencesKey("phone_number")
        val BANK_ID = stringPreferencesKey("bank_id")
        val BANK_NAME = stringPreferencesKey("bank_name")

        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")

        val SBP_MERCHANT_ID = stringPreferencesKey("sbp_merchant_id")
        val SBP_API_KEY = stringPreferencesKey("sbp_api_key")
        val SBP_CUSTOM_ENDPOINT = stringPreferencesKey("sbp_custom_endpoint")
    }

    override val recipientFlow: Flow<Recipient> = context.payLinkDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            Recipient(
                yoomoneyWallet = preferences[PreferencesKeys.YOOMONEY_WALLET],
                phoneNumber = preferences[PreferencesKeys.PHONE_NUMBER],
                bankId = preferences[PreferencesKeys.BANK_ID],
                bankName = preferences[PreferencesKeys.BANK_NAME]
            )
        }

    override suspend fun saveRecipient(recipient: Recipient) {
        context.payLinkDataStore.edit { preferences ->
            if (recipient.yoomoneyWallet != null) {
                preferences[PreferencesKeys.YOOMONEY_WALLET] = recipient.yoomoneyWallet
            } else {
                preferences.remove(PreferencesKeys.YOOMONEY_WALLET)
            }

            if (recipient.phoneNumber != null) {
                preferences[PreferencesKeys.PHONE_NUMBER] = recipient.phoneNumber
            } else {
                preferences.remove(PreferencesKeys.PHONE_NUMBER)
            }

            if (recipient.bankId != null) {
                preferences[PreferencesKeys.BANK_ID] = recipient.bankId
            } else {
                preferences.remove(PreferencesKeys.BANK_ID)
            }

            if (recipient.bankName != null) {
                preferences[PreferencesKeys.BANK_NAME] = recipient.bankName
            } else {
                preferences.remove(PreferencesKeys.BANK_NAME)
            }
        }
    }

    override suspend fun clearRecipient() {
        context.payLinkDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.YOOMONEY_WALLET)
            preferences.remove(PreferencesKeys.PHONE_NUMBER)
            preferences.remove(PreferencesKeys.BANK_ID)
            preferences.remove(PreferencesKeys.BANK_NAME)
        }
    }

    override val settingsFlow: Flow<AppSettings> = context.payLinkDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeModeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            val themeMode = try {
                ThemeMode.valueOf(themeModeStr)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
            val dynamicColors = preferences[PreferencesKeys.DYNAMIC_COLORS] ?: true
            val merchantId = preferences[PreferencesKeys.SBP_MERCHANT_ID].orEmpty()
            val apiKey = preferences[PreferencesKeys.SBP_API_KEY].orEmpty()
            val customEndpoint = preferences[PreferencesKeys.SBP_CUSTOM_ENDPOINT].orEmpty()

            AppSettings(
                themeMode = themeMode,
                dynamicColors = dynamicColors,
                sbpMerchantId = merchantId,
                sbpApiKey = apiKey,
                sbpCustomEndpoint = customEndpoint
            )
        }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        context.payLinkDataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    override suspend fun setDynamicColors(enabled: Boolean) {
        context.payLinkDataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLORS] = enabled
        }
    }

    override suspend fun saveSbpConfig(merchantId: String, apiKey: String, customEndpoint: String) {
        context.payLinkDataStore.edit { preferences ->
            preferences[PreferencesKeys.SBP_MERCHANT_ID] = merchantId.trim()
            preferences[PreferencesKeys.SBP_API_KEY] = apiKey.trim()
            preferences[PreferencesKeys.SBP_CUSTOM_ENDPOINT] = customEndpoint.trim()
        }
    }
}
