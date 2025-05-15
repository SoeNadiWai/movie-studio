package com.personalprojects.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.personalprojects.core.database.entity.FavoriteMovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteMovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteMovie(favoriteMovieEntity: FavoriteMovieEntity)

    @Query("DELETE FROM favorite_movies WHERE movieId = :movieId")
    suspend fun removeFavoriteMovie(movieId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_movies WHERE movieId = :movieId LIMIT 1)")
    fun isFavoriteFlow(movieId: Int): Flow<Boolean>

    @Query("SELECT movieId FROM favorite_movies")
    fun getAllFavoriteIdsFlow(): Flow<List<Int>>

    // Get all favorite movies, ordered by when they were added (newest first)
    @Query("SELECT * FROM favorite_movies ORDER BY addedDate DESC")
    fun getAllFavoritesFlow(): Flow<List<FavoriteMovieEntity>>
}