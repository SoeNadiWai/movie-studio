package com.personalprojects.movielist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.personalprojects.core.data.Movie
import com.personalprojects.home.MovieCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    viewModel: MovieListViewModel = hiltViewModel(),
    onNavigateToDetail: (movieId: String) -> Unit,
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabTitles = remember(uiState.availableCategories) {
        uiState.availableCategories.map { getDisplayTitleForCategory(it) } // Map enum to display string
    }
    val selectedTabIndex = remember(uiState.selectedCategory, uiState.availableCategories) {
        uiState.availableCategories.indexOf(uiState.selectedCategory).coerceAtLeast(0)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.screenTitle ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer, // Example color
//                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
//                )
            )
        }
    ) { paddingValues ->
        // Use a Column to place Tabs above the LazyColumn
        Column(modifier = Modifier.padding(paddingValues)) {

            // --- Category Tabs ---
//            if (tabTitles.isNotEmpty()) {
//                CategoryTabs( // Use the CategoryTabs composable from earlier
//                    categories = tabTitles,
//                    selectedTabIndex = selectedTabIndex,
//                    onTabSelected = { index ->
//                        // Tell ViewModel to select the category based on index
//                        val selectedCategoryEnum = uiState.availableCategories[index]
//                        viewModel.selectCategory(selectedCategoryEnum)
//                    },
//                    modifier = Modifier.fillMaxWidth() // Tabs take full width
//                )
//            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when {
                    uiState.isLoading && uiState.movies.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    uiState.error != null && uiState.movies.isEmpty() -> {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(2.dp), // Padding around the list
                            verticalArrangement = Arrangement.spacedBy(12.dp) // Space between items
                        ) {
                            items(uiState.movies, key = { it.id }) { movie ->
                                val isFavorite = uiState.favoriteMovieIds.contains(movie.id)
                                MovieListItem(
                                    movie = movie,
                                    isFavorite = isFavorite,
                                    onItemClick = { onNavigateToDetail(movie.id.toString()) },
                                    onToggleFavorite = { viewModel.toggleFavorite(movie) }
                                )
                            }

                            // Loading indicator at the bottom for pagination
                            if (uiState.isLoading && uiState.movies.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                            // Error indicator at the bottom for pagination failure
                            if (uiState.error != null && uiState.movies.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Error loading more: ${uiState.error}",
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                        // TODO: Add logic to trigger viewModel.loadMoreMovies() when scrolling near the end
                        // (e.g., using LaunchedEffect with lazyListState.layoutInfo.visibleItemsInfo)
                    }
                }
            }
        }
    }
}

@Composable
fun MovieListItem(
    movie: Movie, // Use your MoviePoster data class
    isFavorite: Boolean, // Whether this movie is currently favorited
    onItemClick: () -> Unit, // Action when the item itself is clicked
    onToggleFavorite: () -> Unit, // Action when the favorite button is clicked
    modifier: Modifier = Modifier
) {
    val baseImageUrl = "https://image.tmdb.org/t/p/w342"
    // Set a fixed height for the image. The Row will adapt to this height.
    val imageFixedHeight = 130.dp // Adjust this for desired item compactness

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            // Use heightIn to allow row to shrink if content is smaller, but not exceed image height
            modifier = Modifier.heightIn(min = imageFixedHeight),
            verticalAlignment = Alignment.CenterVertically // Center items vertically in the Row
        ) {
            // --- Movie Poster Image ---
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(baseImageUrl + movie.posterPath)
                    .crossfade(true)
                    // .placeholder(R.drawable.placeholder_poster)
                    // .error(R.drawable.error_poster)
                    .build(),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(imageFixedHeight) // Set fixed height
                    .aspectRatio(2f / 3f) // Calculate width from fixed height
                    .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
            )

            // --- Movie Details Column ---
            Column(
                modifier = Modifier
                    .weight(1f) // Take remaining horizontal space
                    .padding(
                        start = 12.dp,
                        end = 8.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    ), // Padding inside column
                verticalArrangement = Arrangement.Top // Align text elements to the top
            ) {
                Text(
                    text = movie.title ?: "No Title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    // No top padding needed here if Column aligns top
                )
                Spacer(modifier = Modifier.height(4.dp)) // Space below title
                // Row for Year and Rating - this will now be below the title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Display Release Year
                    movie.releaseDate?.takeIf { it.length >= 4 }?.let {
                        Text(
                            text = it.substring(0, 4),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Display Rating
                    movie.voteAverage?.let { rating ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = "Rating",
                                modifier = Modifier.size(16.dp),
                                tint = Color.Yellow
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format("%.1f", rating),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                // Removed the Spacer with weight(1f) here

            } // End Details Column

            // --- Favorite Button ---
            // IconButton is vertically centered because the parent Row uses CenterVertically
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.padding(end = 8.dp, start = 4.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }

        } // End Row
    } // End Card
}

// Helper to get display titles (or put this logic in VM/UiState)
private fun getDisplayTitleForCategory(category: MovieCategory): String {
    return when (category) {
        MovieCategory.POPULAR -> "Popular"
        MovieCategory.NOW_PLAYING -> "New"
        MovieCategory.TOP_RATED -> "Top Rated"
        MovieCategory.UPCOMING -> "UpComing"
    }
}