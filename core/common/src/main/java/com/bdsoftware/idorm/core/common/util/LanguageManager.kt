package com.bdsoftware.idorm.core.common.util

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Utility to change the app locale at runtime.
 *
 * - Android 13+ (API 33): Uses [LocaleManager] for seamless per-app locale switching.
 *   This triggers a configuration change automatically, Compose will recompose.
 * - Android 12 and below (API 30-32): Wraps the base context with the desired locale
 *   via [wrapContext] in `attachBaseContext`, and uses `recreate()` only when the
 *   user actively switches language at runtime.
 */
object LanguageManager {

    /**
     * Apply the given language code to the running application.
     * On Android 13+ this takes effect immediately (config change).
     * On Android 12 and below, this updates the default locale and
     * requires the caller to recreate the Activity for full effect.
     *
     * @param context  an Activity or Application context
     * @param langCode ISO-639 language code, e.g. "vi" or "en"
     */
    fun applyLanguage(context: Context, langCode: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            localeManager.applicationLocales = LocaleList.forLanguageTags(langCode)
        } else {
            val locale = Locale(langCode)
            Locale.setDefault(locale)
            
            // Update Activity resources configuration
            val config = context.resources.configuration
            config.setLocale(locale)
            config.setLocales(LocaleList(locale))
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)

            // Also update Application resources configuration to ensure global resources match
            val appContext = context.applicationContext
            val appConfig = appContext.resources.configuration
            appConfig.setLocale(locale)
            appConfig.setLocales(LocaleList(locale))
            @Suppress("DEPRECATION")
            appContext.resources.updateConfiguration(appConfig, appContext.resources.displayMetrics)
        }
    }

    /**
     * Wraps a base [Context] with the given locale. Call from
     * `Activity.attachBaseContext(LanguageManager.wrapContext(base, langCode))`.
     */
    fun wrapContext(base: Context, langCode: String): Context {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLocales(LocaleList(locale))
        return base.createConfigurationContext(config)
    }

    /**
     * Returns the current app locale language code (e.g. "vi", "en").
     */
    fun getCurrentLanguage(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            val locales = localeManager.applicationLocales
            if (!locales.isEmpty) locales[0]!!.language else Locale.getDefault().language
        } else {
            context.resources.configuration.locales[0].language
        }
    }
}
