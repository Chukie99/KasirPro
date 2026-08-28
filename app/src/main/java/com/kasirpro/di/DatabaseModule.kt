package com.kasirpro.di

import android.content.Context
import android.content.SharedPreferences
import com.kasirpro.data.database.AppDatabase
import com.kasirpro.data.repository.Repository
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt dependency-injection module.
 * Provides:
 *   - AppDatabase (Room, singleton)
 *   - Repository
 *   - SharedPreferences (for activation status storage)
 *   - DataStore (modern preferences; replaces direct SharedPreferences)
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideRepository(db: AppDatabase): Repository {
        return Repository.getInstance(db)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("kasirpro_prefs", Context.MODE_PRIVATE)
    }
}
