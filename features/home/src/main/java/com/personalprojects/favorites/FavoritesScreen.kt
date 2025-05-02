package com.personalprojects.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.personalprojects.FiveStarRatingIndicator
import com.personalprojects.core.database.entity.FavoriteMovieEntity
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onNavigateToDetail: (movieId: String) -> Unit,
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorite Movies") },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateUp) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
//                    }
//                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Example color
                    titleContentColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.favoriteMovies.isEmpty()) {
                Text(
                    "No favorite movies added yet.",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.favoriteMovies, key = { it.movieId }) { favoriteMovie ->
                        FavoriteMovieListItem(
                            favoriteMovie = favoriteMovie,
                            onItemClick = {
                                // Navigate to detail screen, ensure ID is converted to String
                                onNavigateToDetail(favoriteMovie.movieId.toString())
                            },
                            onRemoveFavoriteClick = {
                                viewModel.removeFavorite(favoriteMovie.movieId)
                            }
                        )
                    }
                }
            }
        }
    }
}


/**
 * Composable for displaying a single favorite movie item, matching the target design.
 */
@Composable
fun FavoriteMovieListItem(
    favoriteMovie: FavoriteMovieEntity,
    onItemClick: () -> Unit,
    onRemoveFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseImageUrl = "https://image.tmdb.org/t/p/w342" // Smaller image size might be sufficient
    val posterPath = favoriteMovie.posterPath
    val title = favoriteMovie.title ?: "Unknown Title"
    val rating = favoriteMovie.voteAverage
    val year = favoriteMovie.releaseYear

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(8.dp), // Use consistent rounding
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min), // Row height determined by tallest element
            verticalAlignment = Alignment.CenterVertically // Center items vertically
        ) {
            // --- Poster Image ---
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(baseImageUrl + posterPath)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(100.dp) // Adjust width as needed
                    .aspectRatio(2.5f / 3f) // Maintain poster aspect ratio
                    // Clip only the start corners to match the card edge
                    .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
            )

            // --- Text Details ---
            Column(
                modifier = Modifier
                    .weight(1f) // Text column takes the rest of the horizontal space
                    .padding(horizontal = 12.dp, vertical = 8.dp), // Padding inside the column
                verticalArrangement = Arrangement.spacedBy(4.dp) // Space between text elements
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Display Rating
                rating?.let {
                    FiveStarRatingIndicator(
                        ratingOutOf10 = rating,
                        starSize = 20.dp,
                        starColor = MaterialTheme.colorScheme.primary
                    )
                }
                // Display Year
                year?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } // End Column for Text Details

            // --- Favorite Button ---
            IconButton(
                onClick = onRemoveFavoriteClick,
                modifier = Modifier.padding(start = 4.dp, end = 8.dp) // Padding around button
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite, // Always filled heart for remove
                    contentDescription = "Remove from Favorites",
                    tint = MaterialTheme.colorScheme.primary, // Use theme color
                    modifier = Modifier.size(24.dp) // Control icon size
                )
            } // End Favorite Button

        } // End Row
    } // End Card
}

// --- Placeholder Data/Entities (Ensure these match your project) ---
// @Entity(tableName = "favorite_movies")
// data class FavoriteMovieEntity(
//     @PrimaryKey val movieId: Int,
//     val title: String?,
//     val posterPath: String?,
//     val voteAverage: Double?,
//     val releaseYear: String?,
//     val addedDate: Long
// )

