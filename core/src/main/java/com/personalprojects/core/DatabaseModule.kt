package com.personalprojects.core

import android.content.Context
import androidx.room.Room
import com.personalprojects.core.database.dao.FavoriteMovieDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Provide these at the Application level (as singletons)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "movie_studio_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFavoriteMovieDao(appDatabase: AppDatabase): FavoriteMovieDao {
        // Call the abstract function defined in your AppDatabase class to get the DAO
        return appDatabase.favoriteMovieDao()
    }
}