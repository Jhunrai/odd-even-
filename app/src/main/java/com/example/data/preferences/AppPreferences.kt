package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game24_settings")

data class AppSettings(
    val apiUrl: String = DEFAULT_API_URL,
    val page: Int = DEFAULT_PAGE,
    val limit: Int = DEFAULT_LIMIT,
    val type: Int = DEFAULT_TYPE,
    val autoRefreshEnabled: Boolean = true,
    val refreshIntervalSeconds: Int = DEFAULT_REFRESH_INTERVAL,
    val hapticFeedbackEnabled: Boolean = true,
    val patternStrategy: String = STRATEGY_SMART_ADAPTIVE,
    val invertSignal: Boolean = false,
    val bullsCount: Int = 6,
    val autoAntiLossProtection: Boolean = true,
    val parityRule: String = RULE_LAST_DIGIT,
    val colorMappingVerified: Boolean = false, // Must not claim verified color without source
    val activeForecastModel: String = MODEL_COMBINED_SMOOTHED,
    val backtestWindow: Int = 50,
    val paSureMode: Boolean = true,
    val simpleMode: Boolean = true
) {
    companion object {
        const val DEFAULT_API_URL = "https://manage.im2015.com/api/game/guess_odd"
        const val DEFAULT_PAGE = 1
        const val DEFAULT_LIMIT = 720
        const val DEFAULT_TYPE = 24
        const val DEFAULT_REFRESH_INTERVAL = 15 // seconds

        // Pattern Strategy Constants
        const val STRATEGY_SMART_ADAPTIVE = "SMART_ADAPTIVE"
        const val STRATEGY_TREND_DRAGON = "TREND_DRAGON"
        const val STRATEGY_CHOP_ALTERNATOR = "CHOP_ALTERNATOR"
        const val STRATEGY_PATTERN_NGRAM = "PATTERN_NGRAM"
        const val STRATEGY_CONTRARIAN = "CONTRARIAN"

        // Parity Settlement Rules
        const val RULE_LAST_DIGIT = "LAST_DIGIT" // Standard Game 24: rightmost digit
        const val RULE_DIGIT_SUM = "DIGIT_SUM"   // Sum of all digits modulo 2
        const val RULE_DIRECT_API = "DIRECT_API" // Direct API result value

        // Forecast Models
        const val MODEL_BASELINE = "BASELINE"
        const val MODEL_HISTORICAL_FREQ = "HISTORICAL_FREQ"
        const val MODEL_ROLLING_FREQ = "ROLLING_FREQ"
        const val MODEL_MARKOV_TRANSITION = "MARKOV_TRANSITION"
        const val MODEL_COMBINED_SMOOTHED = "COMBINED_SMOOTHED"
    }
}

class AppPreferences(private val context: Context) {
    private object Keys {
        val API_URL = stringPreferencesKey("pref_api_url")
        val PAGE = intPreferencesKey("pref_page")
        val LIMIT = intPreferencesKey("pref_limit")
        val TYPE = intPreferencesKey("pref_type")
        val AUTO_REFRESH_ENABLED = booleanPreferencesKey("pref_auto_refresh")
        val REFRESH_INTERVAL = intPreferencesKey("pref_refresh_interval")
        val HAPTIC_ENABLED = booleanPreferencesKey("pref_haptic")
        val PATTERN_STRATEGY = stringPreferencesKey("pref_pattern_strategy")
        val INVERT_SIGNAL = booleanPreferencesKey("pref_invert_signal")
        val BULLS_COUNT = intPreferencesKey("pref_bulls_count")
        val AUTO_ANTI_LOSS = booleanPreferencesKey("pref_auto_anti_loss")
        val PARITY_RULE = stringPreferencesKey("pref_parity_rule")
        val COLOR_MAPPING_VERIFIED = booleanPreferencesKey("pref_color_verified")
        val ACTIVE_FORECAST_MODEL = stringPreferencesKey("pref_forecast_model")
        val BACKTEST_WINDOW = intPreferencesKey("pref_backtest_window")
        val PA_SURE_MODE = booleanPreferencesKey("pref_pa_sure_mode")
        val SIMPLE_MODE = booleanPreferencesKey("pref_simple_mode")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            apiUrl = prefs[Keys.API_URL] ?: AppSettings.DEFAULT_API_URL,
            page = prefs[Keys.PAGE] ?: AppSettings.DEFAULT_PAGE,
            limit = prefs[Keys.LIMIT] ?: AppSettings.DEFAULT_LIMIT,
            type = prefs[Keys.TYPE] ?: AppSettings.DEFAULT_TYPE,
            autoRefreshEnabled = prefs[Keys.AUTO_REFRESH_ENABLED] ?: true,
            refreshIntervalSeconds = prefs[Keys.REFRESH_INTERVAL] ?: AppSettings.DEFAULT_REFRESH_INTERVAL,
            hapticFeedbackEnabled = prefs[Keys.HAPTIC_ENABLED] ?: true,
            patternStrategy = prefs[Keys.PATTERN_STRATEGY] ?: AppSettings.STRATEGY_SMART_ADAPTIVE,
            invertSignal = prefs[Keys.INVERT_SIGNAL] ?: false,
            bullsCount = prefs[Keys.BULLS_COUNT] ?: 6,
            autoAntiLossProtection = prefs[Keys.AUTO_ANTI_LOSS] ?: true,
            parityRule = prefs[Keys.PARITY_RULE] ?: AppSettings.RULE_LAST_DIGIT,
            colorMappingVerified = prefs[Keys.COLOR_MAPPING_VERIFIED] ?: false,
            activeForecastModel = prefs[Keys.ACTIVE_FORECAST_MODEL] ?: AppSettings.MODEL_COMBINED_SMOOTHED,
            backtestWindow = prefs[Keys.BACKTEST_WINDOW] ?: 50,
            paSureMode = prefs[Keys.PA_SURE_MODE] ?: true,
            simpleMode = prefs[Keys.SIMPLE_MODE] ?: true
        )
    }

    suspend fun updateSimpleMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SIMPLE_MODE] = enabled
        }
    }

    suspend fun updatePaSureMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PA_SURE_MODE] = enabled
        }
    }

    suspend fun updateParityRule(rule: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PARITY_RULE] = rule
        }
    }

    suspend fun updateColorMappingVerified(verified: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.COLOR_MAPPING_VERIFIED] = verified
        }
    }

    suspend fun updateActiveForecastModel(model: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_FORECAST_MODEL] = model
        }
    }

    suspend fun updateBacktestWindow(window: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BACKTEST_WINDOW] = window
        }
    }

    suspend fun updateAutoAntiLoss(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_ANTI_LOSS] = enabled
        }
    }

    suspend fun updatePatternStrategy(strategy: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PATTERN_STRATEGY] = strategy
        }
    }

    suspend fun updateInvertSignal(invert: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.INVERT_SIGNAL] = invert
        }
    }

    suspend fun updateBullsCount(count: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BULLS_COUNT] = count
        }
    }

    suspend fun updateApiSettings(url: String, page: Int, limit: Int, type: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.API_URL] = url.trim()
            prefs[Keys.PAGE] = page
            prefs[Keys.LIMIT] = limit
            prefs[Keys.TYPE] = type
        }
    }

    suspend fun updateAutoRefresh(enabled: Boolean, intervalSeconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_REFRESH_ENABLED] = enabled
            prefs[Keys.REFRESH_INTERVAL] = intervalSeconds
        }
    }

    suspend fun updateHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAPTIC_ENABLED] = enabled
        }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { prefs ->
            prefs[Keys.API_URL] = AppSettings.DEFAULT_API_URL
            prefs[Keys.PAGE] = AppSettings.DEFAULT_PAGE
            prefs[Keys.LIMIT] = AppSettings.DEFAULT_LIMIT
            prefs[Keys.TYPE] = AppSettings.DEFAULT_TYPE
            prefs[Keys.AUTO_REFRESH_ENABLED] = true
            prefs[Keys.REFRESH_INTERVAL] = AppSettings.DEFAULT_REFRESH_INTERVAL
            prefs[Keys.HAPTIC_ENABLED] = true
            prefs[Keys.PARITY_RULE] = AppSettings.RULE_LAST_DIGIT
            prefs[Keys.COLOR_MAPPING_VERIFIED] = false
            prefs[Keys.ACTIVE_FORECAST_MODEL] = AppSettings.MODEL_COMBINED_SMOOTHED
            prefs[Keys.BACKTEST_WINDOW] = 50
        }
    }
}
