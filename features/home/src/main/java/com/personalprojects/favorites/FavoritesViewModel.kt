package com.personalprojects.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalprojects.core.database.entity.FavoriteMovieEntity
import com.personalprojects.core.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesScreenUiState(
    val isLoading: Boolean = false, // Initially false, list updates via flow
    val favoriteMovies: List<FavoriteMovieEntity> = emptyList()
    // Add error state if needed
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {

    // Directly expose the flow from the repository, mapped to the UI state
    val uiState: StateFlow<FavoritesScreenUiState> = movieRepository.getFavoritesFlow()
        .map { favoritesList ->
            FavoritesScreenUiState(isLoading = false, favoriteMovies = favoritesList)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keep flow active 5s after last subscriber
            initialValue = FavoritesScreenUiState(isLoading = true) // Initial loading state
        )

    // Function to remove a favorite
    fun removeFavorite(movieId: Int) {
        viewModelScope.launch {
            try {
                movieRepository.removeFavorite(movieId)
            } catch (e: Exception) {
                // TODO: Handle potential errors (e.g., show snackbar)
                println("Error removing favorite: ${e.message}")
            }
        }
    }
}