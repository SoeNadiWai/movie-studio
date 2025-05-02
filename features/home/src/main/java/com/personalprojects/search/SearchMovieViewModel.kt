package com.personalprojects.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalprojects.core.data.Movie
import com.personalprojects.core.data.response.Genre
import com.personalprojects.core.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchScreenUiState(
    // Genre Filter State
    val genres: List<Genre> = emptyList(),
    val isLoadingGenres: Boolean = false,
    val genreError: String? = null,
    val selectedGenreIds: List<Int> = emptyList(),

    // Keyword Search State
    val currentSearchQuery: String = "", // Track the active query
    val keywordSearchResults: List<Movie> = emptyList(),
    val isLoadingKeywordResults: Boolean = false,
    val keywordResultsError: String? = null,
    val keywordCurrentPage: Int = 1,
    val isKeywordLastPage: Boolean = false,
    val isLoadingMoreKeywords: Boolean = false,

    // Genre Search Results State
    val genreSearchResults: List<Movie> = emptyList(),
    val isLoadingGenreResults: Boolean = false,
    val genreResultsError: String? = null,
    val lastSearchedGenres: List<Int> = emptyList(),
    val genreCurrentPage: Int = 1,
    val isGenreLastPage: Boolean = false,
    val isLoadingMoreGenres: Boolean = false,

    // General State
    val searchMode: SearchMode = SearchMode.NONE, // Track current search type
    val favoriteMovieIds: List<Int> = emptyList() // List of favorite IDs
)

// Enum to track the current search mode
enum class SearchMode {
    NONE, // Initial state or no active search
    BY_KEYWORD,
    BY_GENRE
}

@HiltViewModel
class SearchMovieViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchScreenUiState())
    val uiState: StateFlow<SearchScreenUiState> = _uiState.asStateFlow()

    // Flow for debouncing search query input
    private val searchQueryFlow = MutableStateFlow("")

    init {
        fetchGenres() // Fetch genres when ViewModel is created
        observeFavorites() // Start observing favorite IDs
        observeSearchQuery() // Start observing debounced search query
    }

    /** Observes the flow of favorite movie IDs from the repository. */
    private fun observeFavorites() {
        viewModelScope.launch {
            movieRepository.getAllFavoriteIdsFlow().collectLatest { favoriteIds ->
                _uiState.update { it.copy(favoriteMovieIds = favoriteIds) }
            }
        }
    }

    /** Observes the search query flow with debouncing. */
    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(500) // Wait for 500ms pause in typing
                .distinctUntilChanged() // Only emit if text actually changed
                .collectLatest { query ->
                    if (query.isBlank()) {
                        // Clear keyword results if query is blank
                        _uiState.update {
                            it.copy(
                                keywordSearchResults = emptyList(),
                                keywordResultsError = null,
                                searchMode = if (it.lastSearchedGenres.isNotEmpty()) SearchMode.BY_GENRE else SearchMode.NONE // Revert mode if needed
                            )
                        }
                    } else {
                        // Trigger search for non-blank query
                        searchMoviesByKeyword(query)
                    }
                }
        }
    }

    /** Called by UI when search text changes. Updates the flow for debouncing. */
    fun onSearchQueryChanged(query: String) {
        searchQueryFlow.value = query // Update the flow, debounce handles the rest
    }

    // --- Keyword Search ---

    /** Initiates a new search by keyword, resetting pagination. */
    fun searchMoviesByKeyword(query: String) {
        if (query.isBlank()) return // Don't search for blank

        // Reset state for a new keyword search
        _uiState.update {
            it.copy(
                searchMode = SearchMode.BY_KEYWORD, // Set mode
                currentSearchQuery = query, // Store the query being searched
                isLoadingKeywordResults = true,
                isLoadingMoreKeywords = false,
                keywordResultsError = null,
                keywordCurrentPage = 1,
                isKeywordLastPage = false,
                keywordSearchResults = emptyList(), // Clear previous keyword results
                // Optionally clear genre results/selection when starting keyword search
                // selectedGenreIds = emptySet(),
                // genreSearchResults = emptyList(),
                // lastSearchedGenres = emptySet()
            )
        }
        fetchKeywordResultsPage(query, 1)
    }

    /** Loads the next page of keyword search results. */
    fun loadMoreKeywordResults() {
        val currentState = _uiState.value
        if (currentState.isLoadingKeywordResults || currentState.isLoadingMoreKeywords || currentState.isKeywordLastPage || currentState.currentSearchQuery.isBlank()) {
            return
        }
        _uiState.update { it.copy(isLoadingMoreKeywords = true) }
        val nextPage = currentState.keywordCurrentPage + 1
        fetchKeywordResultsPage(currentState.currentSearchQuery, nextPage)
    }

    /** Helper to fetch a specific page of keyword results. */
    private fun fetchKeywordResultsPage(query: String, page: Int) {
        viewModelScope.launch {
            movieRepository.searchMovies(query, page)
                .onSuccess { movies ->
                    _uiState.update {
                        if (it.currentSearchQuery == query) { // Check if query is still the same
                            it.copy(
                                keywordSearchResults = if (page == 1) movies else it.keywordSearchResults + movies,
                                isLoadingKeywordResults = false,
                                isLoadingMoreKeywords = false,
                                keywordCurrentPage = page,
                                isKeywordLastPage = movies.isEmpty()
                            )
                        } else {
                            it
                        } // Ignore if query changed
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        if (it.currentSearchQuery == query) {
                            it.copy(
                                keywordResultsError = error.message ?: "Failed search",
                                isLoadingKeywordResults = false,
                                isLoadingMoreKeywords = false
                            )
                        } else {
                            it
                        } // Ignore if query changed
                    }
                }
        }
    }

    // --- Genre Search ---

    /** Fetches movies from the repository based on the currently selected genres. */
    fun searchMoviesBySelectedGenres() {
        val selectedIds = _uiState.value.selectedGenreIds
        // Optionally clear results if no genres are selected
        // if (selectedIds.isEmpty()) {
        //     _uiState.update { it.copy(searchResults = emptyList(), resultsError = null, lastSearchedGenres = emptySet()) }
        //     return
        // }

        // Prevent re-fetching for the same selection if results are already loaded
        if (selectedIds == _uiState.value.lastSearchedGenres && _uiState.value.genreSearchResults.isNotEmpty()) {
            _uiState.update { it.copy(isLoadingGenreResults = false) }
            return
        }

        _uiState.update {
            it.copy(
                isLoadingGenreResults = true,
                isLoadingMoreGenres = false,
                genreResultsError = null,
                lastSearchedGenres = selectedIds,
                genreCurrentPage = 1, // Reset to page 1
                isGenreLastPage = false,
                genreSearchResults = emptyList() // Clear previous results
            )
        }

        fetchGenreResultsPage(selectedIds, 1)
    }

    /** Loads the next page of results for the currently selected genres. */
    fun loadMoreGenreResults() {
        val currentState = _uiState.value
        // Prevent loading more if already loading, or last page reached, or no genres selected
        if (currentState.isLoadingGenreResults || currentState.isLoadingMoreGenres || currentState.isGenreLastPage || currentState.lastSearchedGenres.isEmpty()) {
            return
        }

        _uiState.update { it.copy(isLoadingMoreGenres = true) } // Indicate loading more
        val nextPage = currentState.genreCurrentPage + 1
        fetchGenreResultsPage(currentState.lastSearchedGenres, nextPage)
    }

    /** Helper function to fetch a specific page of genre results. */
    private fun fetchGenreResultsPage(genreIds: List<Int>, page: Int) {
        viewModelScope.launch {
            val genreIdString = genreIds.joinToString(",")
            // Assuming repository handles empty string case if needed, or check here
            movieRepository.discoverMoviesByGenre(genreIdString, page) // Pass page number
                .onSuccess { movies ->
                    _uiState.update {
                        // Only update if the search was for the currently tracked genres
                        if (it.lastSearchedGenres == genreIds) {
                            it.copy(
                                // Append new movies to existing list if page > 1
                                genreSearchResults = if (page == 1) movies else it.genreSearchResults + movies,
                                isLoadingGenreResults = false, // Turn off initial load flag
                                isLoadingMoreGenres = false, // Turn off loading more flag
                                genreCurrentPage = page,
                                isGenreLastPage = movies.isEmpty() // Assume last page if response is empty
                            )
                        } else {
                            it // Ignore result if selection changed during fetch
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        if (it.lastSearchedGenres == genreIds) {
                            it.copy(
                                genreError = error.message ?: "Failed to fetch results",
                                isLoadingGenreResults = false,
                                isLoadingMoreGenres = false // Turn off loading more on error too
                            )
                        } else {
                            it // Ignore error if selection changed
                        }
                    }
                }
        }
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

    /** Fetches the list of genres from the repository and updates the UI state. */
    private fun fetchGenres() {
        // Prevent fetching if already loading or already fetched successfully
        if (_uiState.value.isLoadingGenres || _uiState.value.genres.isNotEmpty()) return

        _uiState.update { it.copy(isLoadingGenres = true, genreError = null) }
        viewModelScope.launch {
            movieRepository.getGenres()
                .onSuccess { fetchedGenres ->
                    _uiState.update { it.copy(genres = fetchedGenres, isLoadingGenres = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            genreError = error.message ?: "Failed to load genres",
                            isLoadingGenres = false
                        )
                    }
                }
        }
    }

    /** Toggles the selection state of a specific genre ID. */
    fun toggleGenreSelection(genreId: Int) {
        _uiState.update { currentState ->
            val currentSelection = currentState.selectedGenreIds
            val newSelection = if (currentSelection.contains(genreId)) {
                currentSelection - genreId
            } else {
                currentSelection + genreId
            }
            currentState.copy(selectedGenreIds = newSelection)
        }
    }

    // TODO: Implement searchMoviesByKeyword(query: String)
    // TODO: Implement loadMoreMovies() for pagination
}