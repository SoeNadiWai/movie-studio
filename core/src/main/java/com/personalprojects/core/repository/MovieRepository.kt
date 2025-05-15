package com.personalprojects.core.repository

import com.personalprojects.core.TMDBApiService
import com.personalprojects.core.data.Movie
import com.personalprojects.core.data.Movie.Companion.toMovie
import com.personalprojects.core.data.MovieDetails
import com.personalprojects.core.data.MovieDetails.Companion.toMovieDetails
import com.personalprojects.core.data.response.Genre
import com.personalprojects.core.database.dao.FavoriteMovieDao
import com.personalprojects.core.database.entity.FavoriteMovieEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MovieRepository @Inject constructor(
    private val apiService: TMDBApiService,
    private val favoriteMovieDao: FavoriteMovieDao
) {
    private val movieApiKey = "81681a7a2958897396cc2064653021e1"
    suspend fun getPopularMovies(page: Int = 1): Result<List<Movie>> {
        return try {
            val response = apiService.getPopularMovies(movieApiKey)
            Result.success(response.results.map(::toMovie))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNowPlayingMovies(page: Int = 1): Result<List<Movie>> {
        return try {
            val response = apiService.getNowPlayingMovies(movieApiKey)
            Result.success(response.results.map(::toMovie))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTopRatedMovies(page: Int = 1): Result<List<Movie>> {
        return try {
            val response = apiService.getTopRatedMovies(movieApiKey)
            Result.success(response.results.map(::toMovie))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUpcomingMovies(page: Int = 1): Result<List<Movie>> {
        return try {
            val response = apiService.getUpcomingMovies(movieApiKey)
            Result.success(response.results.map(::toMovie))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovieDetail(movieId: Int): Result<MovieDetails> {
        return try {
            val response = apiService.getMovieDetail(movieId, movieApiKey)
            Result.success(toMovieDetails(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isFavoriteMovie(movieId: Int) = favoriteMovieDao.isFavoriteFlow(movieId)

    fun getAllFavoriteIdsFlow(): Flow<List<Int>> {
        return favoriteMovieDao.getAllFavoriteIdsFlow()
    }

    fun getFavoritesFlow(): Flow<List<FavoriteMovieEntity>> {
        return favoriteMovieDao.getAllFavoritesFlow()
    }

    suspend fun addToFavorites(movieId: Int?, movieTitle: String?, posterPath: String?, voteAverage: Double?, releaseYear: String?) {
        movieId?.let {
            favoriteMovieDao.addFavoriteMovie(
                FavoriteMovieEntity(
                    movieId = it,
                    title = movieTitle,
                    posterPath = posterPath,
                    voteAverage = voteAverage,
                    releaseYear = releaseYear,
            addedDate = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun removeFavorite(movieId: Int) {
        favoriteMovieDao.removeFavoriteMovie(movieId)
    }

    // Simple in-memory cache for genres
    private val genresCache = MutableStateFlow<List<Genre>?>(null)

    // Function to get genres, checks cache first
    suspend fun getGenres(): Result<List<Genre>> {
        // Return cached value if available
        genresCache.value?.let { cachedGenres ->
            if (cachedGenres.isNotEmpty()) return Result.success(cachedGenres)
        }

        // If no cache, fetch from network
        return try {
            val response = apiService.getMovieGenres(movieApiKey)
            val genres = response.genres ?: emptyList()
            if (genres.isNotEmpty()) {
                genresCache.value = genres // Update cache
                Result.success(genres)
            } else {
                Result.failure(Exception("No genres found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Expose genres as a Flow (optional but good for observing)
    fun getGenresFlow(): Flow<List<Genre>> = genresCache.filterNotNull()

    suspend fun discoverMoviesByGenre(genreIds: String, page: Int = 1): Result<List<Movie>> {
        return try {
            val response = apiService.discoverMoviesByGenre(
                apiKey = movieApiKey,
                page = page,
                withGenres = genreIds
            )
            Result.success(response.results.map(::toMovie))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Searches for movies based on a keyword query.
     *
     * @param query The user's search term.
     * @param page The page number of results to fetch.
     * @return A Result containing a list of matching MoviePosters or an error.
     */
    suspend fun searchMovies(query: String, page: Int = 1): Result<List<Movie>> {
        // Ensure query is not blank before making the API call
        if (query.isBlank()) {
            return Result.success(emptyList()) // Return empty list for blank query
        }
        return withContext(Dispatchers.IO) { // Perform network call on IO thread
            try {
                val response = apiService.searchMovies(
                    apiKey = movieApiKey, // Replace securely
                    query = query,
                    page = page
                )
                // Map the list of MoviePosterResponse to list of MoviePoster (domain model)
                val domainMovies = response.results.map((::toMovie))
                Result.success(domainMovies)
            } catch (e: Exception) {
                println("Error searching movies: ${e.message}") // Log the error
                Result.failure(e) // Return failure
            }
        }
    }
}