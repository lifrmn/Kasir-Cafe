package com.kasircafe.pos.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore by preferencesDataStore(name = "session")

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tokenKey = stringPreferencesKey("token")
    private val roleKey = stringPreferencesKey("role")

    val tokenFlow: Flow<String> = context.sessionDataStore.data.map { prefs: Preferences ->
        prefs[tokenKey] ?: ""
    }

    val roleFlow: Flow<String> = context.sessionDataStore.data.map { prefs: Preferences ->
        prefs[roleKey] ?: ""
    }

    suspend fun saveSession(token: String, role: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[tokenKey] = token
            prefs[roleKey] = role
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(tokenKey)
            prefs.remove(roleKey)
        }
    }
}
