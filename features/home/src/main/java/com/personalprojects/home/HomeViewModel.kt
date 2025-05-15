package com.personalprojects.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalprojects.core.data.Movie
import com.personalprojects.core.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadAllHomeScreenData()
    }

    fun loadAllHomeScreenData() {
        viewModelScope.launch {
            val popularMovies = async { repository.getPopularMovies() }
            val nowPlayingMovies = async { repository.getNowPlayingMovies() }
            val topRatedMovies = async { repository.getTopRatedMovies() }
            val upcomingMovies = async { repository.getUpcomingMovies() }

            updateStateWithResult(MovieCategory.POPULAR, popularMovies.await())
            updateStateWithResult(MovieCategory.NOW_PLAYING, nowPlayingMovies.await())
            updateStateWithResult(MovieCategory.TOP_RATED, topRatedMovies.await())
            updateStateWithResult(MovieCategory.UPCOMING, upcomingMovies.await())
        }
    }

    private fun updateStateWithResult(category: MovieCategory, result: Result<List<Movie>>) {
        _uiState.update { currentState ->
            val newLists = currentState.movieLists.toMutableMap()
            val newLoading = currentState.isLoading.toMutableMap()
            val newErrors = currentState.isError.toMutableMap()

            newLoading[category] = false // Stop loading for this category
            result.onSuccess { movies ->
                newLists[category] = movies
                newErrors[category] = false // Clear error on success
            }.onFailure { error ->
                newErrors[category] = true
                // newErrors[category] = error.message ?: "An unknown error occurred"
                // Optionally clear list on error: newLists[category] = emptyList()
            }
            currentState.copy(movieLists = newLists, isLoading = newLoading, isError = newErrors)
        }
    }
}

data class HomeUiState(
    val movieLists: Map<MovieCategory, List<Movie>> = emptyMap(),
    val isLoading: Map<MovieCategory, Boolean> = emptyMap(),
    val isError: Map<MovieCategory, Boolean> = emptyMap(),
) {
    val shouldFetchMovies = movieLists.isEmpty()
    val isAllLoading = false
    val isAllError = false
}

enum class MovieCategory {
    POPULAR, NOW_PLAYING, TOP_RATED, UPCOMING;

    val displayTitle: String
        get() = when (this) {
            POPULAR -> "Popular Movies"
            NOW_PLAYING -> "Now Playing"
            TOP_RATED -> "Top Rated"
            UPCOMING -> "Upcoming"
        }
}
