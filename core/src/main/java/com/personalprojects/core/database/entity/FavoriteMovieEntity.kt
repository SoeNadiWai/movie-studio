package com.personalprojects.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_movies")
data class FavoriteMovieEntity(
    @PrimaryKey val movieId: Int,
    val title: String?,
    val posterPath: String?,
    val voteAverage: Double?,
    val releaseYear: String?,
    val addedDate: Long
)