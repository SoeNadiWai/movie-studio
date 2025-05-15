package com.personalprojects.core

import androidx.room.Database
import androidx.room.RoomDatabase
import com.personalprojects.core.database.dao.FavoriteMovieDao
import com.personalprojects.core.database.entity.FavoriteMovieEntity

@Database(entities = [FavoriteMovieEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    // Abstract function to provide the DAO
    abstract fun favoriteMovieDao(): FavoriteMovieDao
}