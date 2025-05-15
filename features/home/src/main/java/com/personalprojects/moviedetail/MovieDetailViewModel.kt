package com.personalprojects.moviedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalprojects.core.data.MovieDetails
import com.personalprojects.core.data.toWatchProvider
import com.personalprojects.core.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<MovieDetailUiState>(MovieDetailUiState.Loading)
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    // Retrieve movieId INTERNALLY from SavedStateHandle
    private val movieId: Int =
        savedStateHandle.get<Int>("movieId") // Key MUST match nav argument name
            ?: throw IllegalStateException("movieId not found in navigation arguments")

    private val isFavoriteFlow = movieRepository.isFavoriteMovie(movieId)

    init {
        fetchDetails()
    }

    fun fetchDetails() {
        viewModelScope.launch {
            _uiState.value = MovieDetailUiState.Loading
            try {
                val details = movieRepository.getMovieDetail(movieId)

                details.onSuccess { movieDetails ->
                    // Determine the target region (e.g., Japan "JP" or device default)
                    // Note: ULocale requires API 24+
                    val targetRegion = Locale.getDefault().country.uppercase() // e.g., "US", "JP"
                    println("Target region for providers: $targetRegion") // For debugging

                    // Extract and filter providers for the target region
                    val countryProviders = movieDetails.watchProviders?.results?.get(targetRegion)
                        ?: movieDetails.watchProviders?.results?.values?.firstOrNull()

                    // Sort by display priority within each category
                    val flatRate = countryProviders?.flatrate
                        ?.mapNotNull { it.toWatchProvider() }
                        ?.sortedBy { it.displayPriority }
                    val rent = countryProviders?.rent
                        ?.mapNotNull { it.toWatchProvider() }
                        ?.sortedBy { it.displayPriority }
                    val buy = countryProviders?.buy
                        ?.mapNotNull { it.toWatchProvider() }
                        ?.sortedBy { it.displayPriority }

                    val movieDetailsWithProviders = movieDetails.copy(
                        watchProvidersLink = countryProviders?.link,
                        flatrateProviders = flatRate,
                        rentProviders = rent,
                        buyProviders = buy
                    )

                    println("Final movie detail = $movieDetailsWithProviders")
                    isFavoriteFlow.collect { isFav ->
                        _uiState.value =
                            MovieDetailUiState.Success(movieDetailsWithProviders, isFav)
                    }
                }.onFailure {
                    _uiState.value = MovieDetailUiState.Error("Movie not found")

                }
            } catch (e: Exception) {
                _uiState.value = MovieDetailUiState.Error(e.message ?: "Failed to load details")
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MovieDetailUiState.Success) {
                if (currentState.isFavorite) {
                    movieRepository.removeFavorite(movieId)
                } else {
                    val movieDetails =currentState.movieDetails
                    movieRepository.addToFavorites(
                        movieDetails.id,
                        movieDetails.title,
                        movieDetails.posterPath,
                        movieDetails.voteAverage,
                        movieDetails.releaseDate
                    )
                }
            }
        }
    }
}

sealed interface MovieDetailUiState {
    object Loading : MovieDetailUiState
    data class Success(
        val movieDetails: MovieDetails,
        val isFavorite: Boolean = false
    ) : MovieDetailUiState

    data class Error(val message: String) : MovieDetailUiState
}