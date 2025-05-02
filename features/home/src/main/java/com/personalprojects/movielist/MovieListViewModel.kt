package com.personalprojects.movielist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalprojects.core.data.Movie
import com.personalprojects.core.repository.MovieRepository
import com.personalprojects.home.MovieCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieListViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryName = savedStateHandle.get<String>("categoryType")

    private val categoryType = categoryName?.let {
        try {
            println("CategoryType: $categoryName")
            enumValueOf<MovieCategory>(categoryName)
        } catch (e: IllegalArgumentException) {
            MovieCategory.POPULAR
        }
    } ?: MovieCategory.POPULAR

    private val _uiState =
        MutableStateFlow(
            MovieListUiState(
                selectedCategory = categoryType,
                screenTitle = categoryType.displayTitle
            )
        )
    val uiState: StateFlow<MovieListUiState> = _uiState.asStateFlow()

    private val categoryArg: String? = savedStateHandle.get<String>("categoryType")
    private var screenTitleOverride: String? = null
    private var genreIdToLoad: String? = null

    init {
        val category = if (categoryArg?.startsWith("genre_") == true) {
            // It's a genre search result
            val parts = categoryArg.split("_")
            genreIdToLoad = parts.getOrNull(1)
            screenTitleOverride = parts.getOrNull(2) // Get the name part for title
            MovieCategory.POPULAR // Or null, or a dedicated "GENRE_SEARCH" state - list type doesn't matter here
        } else {
            // It's a standard list type (Popular, New etc)
            categoryArg?.let { name ->
                try {
                    enumValueOf<MovieCategory>(name)
                } catch (e: Exception) {
                    null
                }
            }
                ?: MovieCategory.POPULAR
        }

        // Update initial state
        _uiState.update {
            it.copy(
                // selectedCategory = category, // Maybe remove selectedCategory if only showing one list type?
                screenTitle = screenTitleOverride ?: category.displayTitle
            )
        }

        // Load initial data
        if (genreIdToLoad != null) {
            loadMoviesByGenre(genreIdToLoad!!)
        } else {
            loadMovies(category) // Load based on list type (Popular, New etc.)
        }

        observeFavorites()
    }

    /** Observes the flow of favorite movie IDs from the repository. */
    private fun observeFavorites() {
        viewModelScope.launch {
            movieRepository.getAllFavoriteIdsFlow().collectLatest { favoriteIds ->
                _uiState.update { it.copy(favoriteMovieIds = favoriteIds) }
            }
        }
    }

    private fun loadMoviesByGenre(genreId: String, page: Int = 1) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = movieRepository.discoverMoviesByGenre(genreId, page)
            handleResult(result, page)
        }
    }

    private fun loadMovies(category: MovieCategory, page: Int = 1) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = when (category) {
                MovieCategory.POPULAR -> movieRepository.getPopularMovies(page)
                MovieCategory.NOW_PLAYING -> movieRepository.getNowPlayingMovies(page)
                MovieCategory.TOP_RATED -> movieRepository.getTopRatedMovies(page)
                MovieCategory.UPCOMING -> movieRepository.getUpcomingMovies(page)
            }
            handleResult(result, page)
        }
    }

    private fun handleResult(result: Result<List<Movie>>, page: Int) {
        result.onSuccess { fetchedMovies ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    movies = if (page == 1) fetchedMovies else it.movies + fetchedMovies,
                    currentPage = page,
                    isLastPage = fetchedMovies.isEmpty()
                )
            }
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to load movies"
                )
            }
        }
    }

    fun selectCategory(category: MovieCategory) {
        if (category == _uiState.value.selectedCategory) return // No change if same tab clicked

        _uiState.update {
            it.copy(
                selectedCategory = category,
                screenTitle = category.displayTitle,
                movies = emptyList(), // Clear old list immediately
                isLoading = true, // Show loading for new category
                error = null,
                currentPage = 1,
                isLastPage = false
            )
        }
        loadMovies(category)
    }

    /** Toggles the favorite status of a movie. */
    fun toggleFavorite(movie: Movie) {
        val isCurrentlyFavorite = _uiState.value.favoriteMovieIds.contains(movie.id)
        viewModelScope.launch {
            try {
                if (isCurrentlyFavorite) {
                    movie.id.let { movieRepository.removeFavorite(it) }
                } else {
                    movieRepository.addToFavorites(
                        movie.id,
                        movie.title,
                        movie.posterPath,
                        movie.voteAverage,
                        movie.releaseDate
                    )
                }
            } catch (e: Exception) {
                // TODO: Handle error (e.g., show snackbar)
                println("Error toggling favorite: ${e.message}")
            }
        }
    }

    data class MovieListUiState(
        val isLoading: Boolean = false,
        val movies: List<Movie> = emptyList(),
        val currentPage: Int = 1,
        val isLastPage: Boolean = false,
        val error: String? = null,
        val screenTitle: String? = null,
        val availableCategories: List<MovieCategory> = MovieCategory.entries,
        val selectedCategory: MovieCategory,
        val favoriteMovieIds: List<Int> = emptyList(),
    )
}