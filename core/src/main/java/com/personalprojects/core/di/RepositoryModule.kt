package com.personalprojects.core.di

import com.personalprojects.core.TMDBApiService
import com.personalprojects.core.database.dao.FavoriteMovieDao
import com.personalprojects.core.repository.MovieRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideMovieRepository(tMDBApiService: TMDBApiService, favoriteMovieDao: FavoriteMovieDao): MovieRepository {
        return MovieRepository(tMDBApiService, favoriteMovieDao)
    }
}