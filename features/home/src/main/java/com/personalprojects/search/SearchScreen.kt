package com.personalprojects.search


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.personalprojects.core.data.response.Genre
import com.personalprojects.movielist.MovieListItem
import kotlinx.coroutines.launch

/**
 * The main Search Screen composable. Includes search bar, filter button,
 * bottom sheet for genre selection, and displays search results with pagination.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchMovieViewModel = hiltViewModel(),
    onNavigateToDetail: (movieId: String) -> Unit,
    onNavigateUp: () -> Unit = {}
) {
    // --- State Management ---
    var searchQuery by remember { mutableStateOf("") } // Local state for TextField
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState() // State for the results list

    // Sync local searchQuery state with ViewModel's debounced flow
    LaunchedEffect(searchQuery) {
        viewModel.onSearchQueryChanged(searchQuery)
    }

    // Helper function to show the bottom sheet
    val openGenreFilterSheet: () -> Unit = {
        if (!showBottomSheet) {
            scope.launch { showBottomSheet = true }
        }
    }

    // --- Screen Layout ---
    Column(modifier = Modifier.fillMaxSize()) {

        // --- Search Bar ---
        SearchBarWithFilter(
            searchQuery = searchQuery, // Use local state for immediate UI update
            onQueryChange = { searchQuery = it }, // Update local state directly
            onFilterClick = openGenreFilterSheet
        )

        // --- Main Content Area (Search Results) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Takes remaining space
        ) {
            // Determine which results list and state to use based on searchMode
            val resultsToShow = when (uiState.searchMode) {
                SearchMode.BY_KEYWORD -> uiState.keywordSearchResults
                SearchMode.BY_GENRE -> uiState.genreSearchResults
                SearchMode.NONE -> emptyList() // Show placeholder or recommendations
            }
            val isLoading = when (uiState.searchMode) {
                SearchMode.BY_KEYWORD -> uiState.isLoadingKeywordResults && resultsToShow.isEmpty()
                SearchMode.BY_GENRE -> uiState.isLoadingGenreResults && resultsToShow.isEmpty()
                SearchMode.NONE -> false
            }
            val isLoadingMore = when (uiState.searchMode) {
                SearchMode.BY_KEYWORD -> uiState.isLoadingMoreKeywords
                SearchMode.BY_GENRE -> uiState.isLoadingMoreGenres
                SearchMode.NONE -> false
            }
            val error = when (uiState.searchMode) {
                SearchMode.BY_KEYWORD -> uiState.keywordResultsError
                SearchMode.BY_GENRE -> uiState.genreResultsError
                SearchMode.NONE -> null
            }
            val isLastPage = when (uiState.searchMode) {
                SearchMode.BY_KEYWORD -> uiState.isKeywordLastPage
                SearchMode.BY_GENRE -> uiState.isGenreLastPage
                SearchMode.NONE -> true
            }

            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                error != null && resultsToShow.isEmpty() -> {
                    Text(
                        "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                // Show placeholder if no search active and no results
                resultsToShow.isEmpty() && uiState.searchMode == SearchMode.NONE -> {
                    Text(
                        "Search movies or select genres using the filter.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                // Show list (even if empty after a search)
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(resultsToShow, key = { it.id ?: -1 }) { movie ->
                            val isFavorite = uiState.favoriteMovieIds.contains(movie.id)
                            MovieListItem(
                                movie = movie,
                                isFavorite = isFavorite,
                                onItemClick = { movie.id?.let { id -> onNavigateToDetail(id.toString()) } },
                                onToggleFavorite = { viewModel.toggleFavorite(movie) }
                            )
                        }

                        // --- Loading More Indicator ---
                        item {
                            if (isLoadingMore) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                    } // End LazyColumn

                    // --- Pagination Trigger ---
                    val endOfListReached by remember {
                        derivedStateOf {
                            val layoutInfo = listState.layoutInfo
                            val visibleItemsInfo = layoutInfo.visibleItemsInfo
                            if (layoutInfo.totalItemsCount == 0) {
                                false
                            } else {
                                val lastVisibleItem = visibleItemsInfo.lastOrNull()
                                (lastVisibleItem?.index
                                    ?: -1) >= (layoutInfo.totalItemsCount - 1 - 5)
                            }
                        }
                    }

                    LaunchedEffect(endOfListReached) {
                        if (endOfListReached && !isLoadingMore && !isLastPage) {
                            // Call the correct load more function based on mode
                            when (uiState.searchMode) {
                                SearchMode.BY_KEYWORD -> viewModel.loadMoreKeywordResults()
                                SearchMode.BY_GENRE -> viewModel.loadMoreGenreResults()
                                SearchMode.NONE -> {} // Do nothing
                            }
                        }
                    }
                    // --- End Pagination Trigger ---

                } // End Else (Show List/Placeholder)
            } // End When
        } // End Box


        // --- Bottom Sheet Definition ---
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState// Limit height
            ) {
                GenreBottomSheetContent(
                    genres = uiState.genres,
                    selectedGenreIds = uiState.selectedGenreIds,
                    isLoading = uiState.isLoadingGenres,
                    error = uiState.genreError,
                    onGenreToggle = { genreId ->
                        viewModel.toggleGenreSelection(genreId)
                    },
                    onApplyClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showBottomSheet = false
                            viewModel.searchMoviesBySelectedGenres() // Trigger Genre search
                        }
                    }
                )
            }
        }
    } // End Main Column
}

/**
 * Composable function for the content displayed inside the Genre Filter Bottom Sheet.
 * Uses FilterChips and FlowRow for tag-like appearance and multi-select.
 */
@OptIn(ExperimentalLayoutApi::class) // For FilterChip, FlowRow
@Composable
fun GenreBottomSheetContent(
    genres: List<Genre>,
    selectedGenreIds: List<Int>, // Receive the set of selected IDs
    isLoading: Boolean,
    error: String?,
    onGenreToggle: (genreId: Int) -> Unit, // Callback when a chip is toggled
    onApplyClick: () -> Unit, // Callback for the Apply button
    modifier: Modifier = Modifier
) {
    // Use Column to allow placing Button below the scrollable genres
    Column(
        modifier = modifier
            .fillMaxWidth()
            // Apply navigationBarsPadding to avoid system bar overlap within the sheet
            .navigationBarsPadding()
            // Add padding to the overall content *within* the sheet
            .padding(top = 16.dp) // Add some padding at the top below drag handle
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.6f)

    ) {
        Text(
            "Select Genres",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 12.dp
            ) // Padding around title
        )

        // Scrollable area for the genre chips - Use weight to push button down
        Column(
            modifier = Modifier
                .weight(1f) // <-- Make this Column take up available vertical space
                .verticalScroll(rememberScrollState()) // Make scrollable
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    )
                }

                error != null -> {
                    Text(
                        "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                genres.isEmpty() -> {
                    Text(
                        "No genres found.",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                else -> {
                    // Use FlowRow for tag-like layout within the scrollable column
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 16.dp
                            ), // Padding around chips, including bottom
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        genres.forEach { genre ->
                            genre.id.let { genreId -> // Ensure ID is not null
                                val isSelected = selectedGenreIds.contains(genreId)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onGenreToggle(genreId) }, // Toggle selection
                                    label = { Text(genre.name ?: "Unknown") },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "Selected",
                                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                                            )
                                        }
                                    } else {
                                        null
                                    }
                                )
                            }
                        }
                    } // End FlowRow
                }
            } // End When
            // Removed Spacer from here - weight modifier handles the space now
        } // End Scrollable Column

        // Apply Button is now placed AFTER the weighted Column, sticking it to the bottom
        Button(
            onClick = onApplyClick,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                // Add padding around the button itself
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxWidth() // Make button fill width within padding
        ) {
            Text("Show Results")
        }
    } // End Outer Column
}


@Composable
fun SearchBarWithFilter(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    placeholderText: String = "Search movie"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp), // Adjust padding as needed
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp) // Space between TextField and Button
    ) {
        // --- Search Text Field ---
        TextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f),
            placeholder = {
                Text(placeholderText, color = Color.Gray) // Placeholder text color
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = Color.Gray // Icon color
                )
            },
            shape = RoundedCornerShape(30.dp), // High corner radius for rounded look
            singleLine = true,
            colors = TextFieldDefaults.colors( // Use colors function for M3 TextField
                // Background color of the TextField
                focusedContainerColor = Color.LightGray.copy(alpha = 0.5f), // Adjust color/alpha
                unfocusedContainerColor = Color.LightGray.copy(alpha = 0.5f),
                disabledContainerColor = Color.LightGray.copy(alpha = 0.3f),
                // Text/Cursor colors
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                cursorColor = MaterialTheme.colorScheme.primary,
                // Remove the indicator line by making colors transparent
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            )
        )

        // --- Filter Icon Button ---
        IconButton(
            onClick = onFilterClick,
            modifier = Modifier.background(
                MaterialTheme.colorScheme.primary,
                CircleShape
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Tune,
                contentDescription = "Filter Options",
                tint = Color.White.copy(alpha = 0.8f) // Icon tint
            )
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF111111)
@Composable
fun PreviewSearchBarWithFilter() {
    var query by remember { mutableStateOf("") }
    SearchBarWithFilter(searchQuery = query, onQueryChange = { query = it }, onFilterClick = {})
}
