package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.AppLang
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "khabarcal_prefs")

class UserPrefsRepository(private val context: Context) {
    private val langKey = stringPreferencesKey("preferred_lang")

    val preferredLang: Flow<AppLang> = context.dataStore.data.map { prefs ->
        AppLang.fromCode(prefs[langKey])
    }

    suspend fun setPreferredLang(lang: AppLang) {
        context.dataStore.edit { prefs ->
            prefs[langKey] = lang.code
        }
    }
}
