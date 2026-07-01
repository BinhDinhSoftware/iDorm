package com.bdsoftware.idorm.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bdsoftware.idorm.core.model.WifiNetworkConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import com.bdsoftware.idorm.core.network.retrofit.AuthTokenProvider
import com.bdsoftware.idorm.core.network.retrofit.HcmcAuthTokenProvider
import dagger.hilt.android.qualifiers.ApplicationContext

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "idorm_prefs")

@Singleton
class IDormPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) : AuthTokenProvider, HcmcAuthTokenProvider {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val USER_FULLNAME_KEY = stringPreferencesKey("fullname")
        private val USER_ROOM_KEY = stringPreferencesKey("user_room")
        private val USER_AVATAR_URL_KEY = stringPreferencesKey("user_avatar_url")
        private val USER_MOBILE_KEY = stringPreferencesKey("user_mobile")
        private val USER_STUDENT_CODE_KEY = stringPreferencesKey("student_code")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val DISMISSED_CAMPAIGN_ID_KEY = stringPreferencesKey("dismissed_campaign_id")
        private val WIFI_GATEWAY_URL_KEY = stringPreferencesKey("wifi_gateway_url")
        private val WIFI_AWING_URL_KEY = stringPreferencesKey("wifi_awing_url")
        private val WIFI_CONFIGS_JSON_KEY = stringPreferencesKey("wifi_configs_json")
        private val WIFI_AUTO_BYPASS_ENABLED_KEY = booleanPreferencesKey("wifi_auto_bypass_enabled")
        private val APP_LANGUAGE_KEY = stringPreferencesKey("app_language")

        /** Default WiFi configs for KTX mesh networks */
        val DEFAULT_WIFI_CONFIGS = listOf(
            WifiNetworkConfig(
                ssid = "1. Free Wi-MESH",
                gatewayUrl = "http://free.wi-mesh.vn",
                awingUrl = "http://v1.awingconnect.vn"
            ),
            WifiNetworkConfig(
                ssid = "Free Wi-MESH - Rescue",
                gatewayUrl = "http://186.186.0.1",
                awingUrl = "http://v1.awingconnect.vn"
            ),
             WifiNetworkConfig(
                ssid = "Free Wi-MESH",
                gatewayUrl = "http://172.172.0.1",
                awingUrl = "http://v1.awingconnect.vn"
            )
        )



        // HCMC Auth keys
        private val HCMC_ACCESS_TOKEN_KEY = stringPreferencesKey("hcmc_access_token")
        private val HCMC_REFRESH_TOKEN_KEY = stringPreferencesKey("hcmc_refresh_token")
        private val HCMC_USER_ID_KEY = stringPreferencesKey("hcmc_user_id")

        fun serializeWifiConfigs(configs: List<WifiNetworkConfig>): String {
            val jsonArray = JSONArray()
            configs.forEach { config ->
                val obj = JSONObject().apply {
                    put("ssid", config.ssid)
                    put("gatewayUrl", config.gatewayUrl)
                    put("awingUrl", config.awingUrl)
                    put("enabled", config.enabled)
                }
                jsonArray.put(obj)
            }
            return jsonArray.toString()
        }

        fun deserializeWifiConfigs(json: String): List<WifiNetworkConfig> {
            return try {
                val jsonArray = JSONArray(json)
                (0 until jsonArray.length()).map { i ->
                    val obj = jsonArray.getJSONObject(i)
                    WifiNetworkConfig(
                        ssid = obj.getString("ssid"),
                        gatewayUrl = obj.optString("gatewayUrl", "http://186.186.0.1"),
                        awingUrl = obj.optString("awingUrl", "http://v1.awingconnect.vn"),
                        enabled = obj.optBoolean("enabled", true)
                    )
                }
            } catch (e: Exception) {
                DEFAULT_WIFI_CONFIGS
            }
        }
    }


    override val hcmcAccessToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[HCMC_ACCESS_TOKEN_KEY]
    }

    override val hcmcRefreshToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[HCMC_REFRESH_TOKEN_KEY]
    }

    val hcmcUserId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[HCMC_USER_ID_KEY]
    }

    override val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    val userId: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    val userFullName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_FULLNAME_KEY]
    }

    val userRoom: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ROOM_KEY]
    }

    val userAvatarUrl: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_AVATAR_URL_KEY]
    }

    val userMobile: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_MOBILE_KEY]
    }

    val userStudentCode: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_STUDENT_CODE_KEY]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }

    val isLoggedIn: Flow<Boolean> = token.map { it != null }

    val dismissedCampaignId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[DISMISSED_CAMPAIGN_ID_KEY]
    }

    val wifiGatewayUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[WIFI_GATEWAY_URL_KEY] ?: "http://186.186.0.1"
    }

    val wifiAwingUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[WIFI_AWING_URL_KEY] ?: "http://v1.awingconnect.vn"
    }

    val wifiConfigsJson: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[WIFI_CONFIGS_JSON_KEY] ?: serializeWifiConfigs(DEFAULT_WIFI_CONFIGS)
    }

    val wifiAutoBypassEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[WIFI_AUTO_BYPASS_ENABLED_KEY] ?: false
    }

    val appLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_LANGUAGE_KEY] ?: "VI"
    }



    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun saveUserInfo(
        id: Int?,
        fullname: String?,
        room: String?,
        avatarUrl: String?,
        mobile: String?,
        studentCode: String?,
        email: String?
    ) {
        context.dataStore.edit { preferences ->
            id?.let { preferences[USER_ID_KEY] = it } ?: preferences.remove(USER_ID_KEY)
            fullname?.let { preferences[USER_FULLNAME_KEY] = it } ?: preferences.remove(USER_FULLNAME_KEY)
            room?.let { preferences[USER_ROOM_KEY] = it } ?: preferences.remove(USER_ROOM_KEY)
            avatarUrl?.let { preferences[USER_AVATAR_URL_KEY] = it } ?: preferences.remove(USER_AVATAR_URL_KEY)
            mobile?.let { preferences[USER_MOBILE_KEY] = it } ?: preferences.remove(USER_MOBILE_KEY)
            studentCode?.let { preferences[USER_STUDENT_CODE_KEY] = it } ?: preferences.remove(USER_STUDENT_CODE_KEY)
            email?.let { preferences[USER_EMAIL_KEY] = it } ?: preferences.remove(USER_EMAIL_KEY)
        }
    }



    suspend fun saveWifiUrls(gatewayUrl: String, awingUrl: String) {
        context.dataStore.edit { preferences ->
            preferences[WIFI_GATEWAY_URL_KEY] = gatewayUrl
            preferences[WIFI_AWING_URL_KEY] = awingUrl
        }
    }

    suspend fun saveWifiConfig(gatewayUrl: String, awingUrl: String) {
        saveWifiUrls(gatewayUrl, awingUrl)
    }

    suspend fun saveWifiConfigsJson(json: String) {
        context.dataStore.edit { preferences ->
            preferences[WIFI_CONFIGS_JSON_KEY] = json
        }
    }

    suspend fun setWifiAutoBypassEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WIFI_AUTO_BYPASS_ENABLED_KEY] = enabled
        }
    }

    // ── JSON serialization helpers for WifiNetworkConfig ──



    suspend fun saveDismissedCampaignId(campaignId: String) {
        context.dataStore.edit { preferences ->
            preferences[DISMISSED_CAMPAIGN_ID_KEY] = campaignId
        }
    }

    suspend fun dismissCampaign(campaignId: String) {
        saveDismissedCampaignId(campaignId)
    }

    suspend fun saveHcmcAuth(accessToken: String?, refreshToken: String?, userId: String?) {
        context.dataStore.edit { preferences ->
            accessToken?.let { preferences[HCMC_ACCESS_TOKEN_KEY] = it } ?: preferences.remove(HCMC_ACCESS_TOKEN_KEY)
            refreshToken?.let { preferences[HCMC_REFRESH_TOKEN_KEY] = it } ?: preferences.remove(HCMC_REFRESH_TOKEN_KEY)
            userId?.let { preferences[HCMC_USER_ID_KEY] = it } ?: preferences.remove(HCMC_USER_ID_KEY)
        }
    }

    suspend fun saveAppLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_LANGUAGE_KEY] = language
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            val lang = preferences[APP_LANGUAGE_KEY]
            preferences.clear()
            lang?.let { preferences[APP_LANGUAGE_KEY] = it }
        }
    }

    suspend fun clearToken() {
        clear()
    }
}
