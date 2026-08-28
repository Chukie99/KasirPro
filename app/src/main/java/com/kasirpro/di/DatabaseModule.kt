package com.kasirpro.di

import android.content.Context
import android.content.SharedPreferences
import com.kasirpro.data.database.AppDatabase
import com.kasirpro.data.repository.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.buildDatabase(context)
    }

    @Provides
    @Singleton
    fun provideRepository(db: AppDatabase): Repository {
        return Repository(db)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("kasirpro_prefs", Context.MODE_PRIVATE)
    }
}
