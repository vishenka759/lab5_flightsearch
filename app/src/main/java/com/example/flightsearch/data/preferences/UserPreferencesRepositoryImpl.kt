package com.example.flightsearch.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.flightsearch.domain.repository.UserPreferencesRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class UserPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    private object Keys {
        val LAST_SEARCH_QUERY = stringPreferencesKey("last_search_query")
    }

    override val lastSearchQuery: Flow<String?> =
        dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { prefs ->
                prefs[Keys.LAST_SEARCH_QUERY]
            }

    override suspend fun saveLastSearchQuery(iataCode: String) {
        val normalized = iataCode.trim().uppercase()

        dataStore.edit { prefs ->
            if (normalized.isBlank()) {
                prefs.remove(Keys.LAST_SEARCH_QUERY)
            } else {
                prefs[Keys.LAST_SEARCH_QUERY] = normalized
            }
        }
    }
}

