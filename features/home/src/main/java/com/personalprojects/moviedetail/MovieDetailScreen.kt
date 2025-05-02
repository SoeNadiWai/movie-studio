package com.personalprojects.moviedetail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.personalprojects.core.data.MovieDetails
import com.personalprojects.core.data.WatchProvider
import com.personalprojects.home.ErrorDialog
import java.util.Locale

@Composable
fun MovieDetailScreen(
    viewModel: MovieDetailViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is MovieDetailUiState.Error -> ErrorDialog { viewModel.fetchDetails() }
            is MovieDetailUiState.Loading -> CircularProgressIndicator()
            is MovieDetailUiState.Success -> {
                MovieDetailContent(
                    movie = state.movieDetails,
                    onNavigateUp = onNavigateUp,
                    onToggleFavorite = { viewModel.toggleFavorite() },
                    isFavorite = state.isFavorite,
                    onShareClick = {
                        shareMovieDetails(
                            context = context,
                            movieTitle = state.movieDetails.title,
                            movieId = state.movieDetails.id,
                            imdbId = state.movieDetails.imdbId // Pass IMDb ID if available in your model
                        )
                    },
                    onWatchProviderClick = { url ->
                        openUrl(context, url)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class) // For stickyHeader
@Composable
fun MovieDetailContent(
    movie: MovieDetails,
    isFavorite: Boolean,
    onNavigateUp: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShareClick: () -> Unit,
    onWatchProviderClick: (url: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val baseImageUrl = "https://image.tmdb.org/t/p/w780"
    val lazyListState = rememberLazyListState() // State for scroll offset
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    // Target height for the backdrop image area
    val backdropTargetHeight = screenHeight * 0.5f // e.g., 50% of screen

    // Calculate the parallax offset based on the first item's scroll offset
    val parallaxOffset = remember {
        derivedStateOf {
            if (lazyListState.firstVisibleItemIndex == 0) {
                // Calculate offset only when the first item (image) is visible
                lazyListState.firstVisibleItemScrollOffset.toFloat() * 0.5f // Adjust 0.5f for parallax speed
            } else {
                0f // No offset if image is scrolled completely off screen
            }
        }
    }
    // Calculate alpha based on scroll offset
    val imageAlpha = remember {
        derivedStateOf {
            if (lazyListState.firstVisibleItemIndex == 0) {
                // Adjust fade: start fading later and fade less aggressively maybe
                1f - (lazyListState.firstVisibleItemScrollOffset.toFloat() / (backdropTargetHeight.value * 1.5f)).coerceIn(
                    0f,
                    0.6f
                )
            } else {
                0f // Fully faded if scrolled past
            }
        }
    }


    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black) // Background for the whole list area
        // No top content padding needed as sticky header handles spacing
    ) {

        // --- Item 1: Backdrop Image with Parallax ---
        item(key = "backdropImage") {
            Box( // Box to hold image and gradient
                modifier = Modifier
                    .fillMaxWidth()
                    .height(backdropTargetHeight) // Set the target height
                    // Apply graphics layer for parallax effect
                    .graphicsLayer {
                        translationY = parallaxOffset.value
                        alpha = imageAlpha.value
                    }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(baseImageUrl + movie.backdropPath)
                        .crossfade(true)
                        .build(),
                    contentDescription = movie.title + " backdrop",
                    contentScale = ContentScale.Crop, // Crop to fill the Box bounds
                    modifier = Modifier.fillMaxSize() // Fill the parent Box
                )
                // Gradient Overlay (Optional - remove if not desired for non-overlapping look)
                // Box(
                //     modifier = Modifier
                //         .fillMaxSize()
                //         .background(
                //             Brush.verticalGradient(
                //                 colors = listOf(Color.Transparent, Color.Black.copy(alpha=0.6f), Color.Black),
                //                 startY = backdropTargetHeight.value * 0.4f, // Start fade later
                //                 endY = backdropTargetHeight.value // End at bottom of image box
                //             )
                //         )
                // )
            }
        }

        // --- Item 2: Sticky Header for Action Buttons ---
        stickyHeader(key = "actionBar") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Use a solid background matching the content area
                    .background(Color.Black) // Solid black background
                    .statusBarsPadding() // Add padding for status bar
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween // Space out buttons
            ) {
                // Back Button
                IconButton(
                    onClick = onNavigateUp,
                    // Removed background modifier for cleaner look on solid header
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                // Share and Favorite Buttons
                Row {
                    IconButton(
                        onClick = onShareClick,
                        // Removed background modifier
                    ) {
                        Icon(Icons.Default.Share, "Share Movie", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onToggleFavorite,
                        // Removed background modifier
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }
                }
            }
        }

        // --- Item 3: Main Content Area ---
        item(key = "mainContent") {
            Column(
                modifier = Modifier
                    // Apply background ONLY to this content column
                    .background(Color.Black)
                    // Add padding for content spacing
                    .padding(16.dp)
            ) {
                // "MOVIE" tag
                Text(
                    text = "MOVIE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = movie.title ?: "No Title",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Genres Text
                Text(
                    text = movie.genres?.mapNotNull { it?.name }?.joinToString(", ")
                        ?: "No genres available.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))
                // Overview Text
                Text(
                    text = movie.overview ?: "No overview available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Metadata Badges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    movie.releaseDate?.let { MetadataBadge(text = it) }
                    movie.voteAverage?.let {
                        MetadataBadge(
                            text = String.format(Locale.getDefault(), "%.1f", it),
                            icon = Icons.Default.Star
                        )
                    }
                    movie.runtime?.let {
                        MetadataBadge(
                            text = formatDuration(it),
                            icon = Icons.Default.Schedule
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start, // Align buttons to the start
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // --- Favorite Button ---
                    Button( // Using a Button for potentially clearer visual cue
                        onClick = onToggleFavorite,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFavorite) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isFavorite) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = CircleShape // Make it rounded
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            modifier = Modifier.size(20.dp) // Adjust icon size if needed
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isFavorite) "Favorited" else "Add Favorite")
                    }

                    Spacer(modifier = Modifier.width(16.dp)) // Space between buttons

                    // --- Share Button ---
                    OutlinedButton( // Using OutlinedButton for visual difference
                        onClick = onShareClick,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        shape = CircleShape, // Make it rounded
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Movie",
                            modifier = Modifier.size(20.dp) // Adjust icon size if needed
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share")
                    }
                } // End Buttons Row

                // --- Section 2: Where to Watch ---
                val flatrate = movie.flatrateProviders ?: emptyList()
                val rent = movie.rentProviders ?: emptyList()
                val buy = movie.buyProviders ?: emptyList()
                val watchLink = movie.watchProvidersLink // Get the main TMDB watch link
                val hasAnyProvider = flatrate.isNotEmpty() || rent.isNotEmpty() || buy.isNotEmpty()

                // Only show the section if there are providers OR a general link
                if (hasAnyProvider || !watchLink.isNullOrBlank()) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = Color.Gray.copy(alpha = 0.5f)
                    )
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        // Main Title for the Section
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Where to Watch",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            // Show "See All Options" button only if the main link exists
                            if (!watchLink.isNullOrBlank()) {
                                TextButton(onClick = { onWatchProviderClick(watchLink) }) {
                                    Text("See All Options")
                                }
                            }
                        }

                        // --- Streaming Section ---
                        if (flatrate.isNotEmpty()) {
                            Text(
                                "Stream",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            WatchProviderRow(
                                providers = flatrate,
                                // Click action still opens the main TMDB watch link for simplicity
                                onProviderClick = {
                                    if (watchLink != null) {
                                        onWatchProviderClick(watchLink)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp)) // Space after streaming row
                        }

                        // --- Rent Section ---
                        if (rent.isNotEmpty()) {
                            Text(
                                "Rent",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            WatchProviderRow(
                                providers = rent,
                                onProviderClick = {
                                    if (watchLink != null) {
                                        onWatchProviderClick(watchLink)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp)) // Space after rent row
                        }

                        // --- Buy Section ---
                        if (buy.isNotEmpty()) {
                            Text(
                                "Buy",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            WatchProviderRow(
                                providers = buy,
                                onProviderClick = {
                                    if (watchLink != null) {
                                        onWatchProviderClick(watchLink)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp)) // Space after buy row
                        }

                        // Message if no specific providers were found but link exists
                        if (!hasAnyProvider && !watchLink.isNullOrBlank()) {
                            Text(
                                "Specific providers not listed for your region. Check TMDB for options.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                } // End Where to Watch section

                // --- Placeholder Sections ---
                Spacer(modifier = Modifier.height(16.dp))
                // Text("Cast Section Placeholder", ...)
                Spacer(modifier = Modifier.height(16.dp))
                // Text("Similar Movies Section Placeholder", ...)
                Spacer(modifier = Modifier.height(16.dp))

                // Add final padding at the bottom if needed, LazyColumn handles scroll end padding via contentPadding if necessary
                // Spacer(modifier = Modifier.height(32.dp))
            }
        } // End Main Content Item
    } // End LazyColumn
}

//@Composable
//fun MovieDetailContent(
//    movie: MovieDetails,
//    isFavorite: Boolean,
//    onNavigateUp: () -> Unit,
//    onToggleFavorite: () -> Unit,
//    onShareClick: () -> Unit,
//    onWatchProviderClick: (url: String) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val baseImageUrl = "https://image.tmdb.org/t/p/w780"
//    val backdropHeightFraction = 0.5f
//    val configuration = LocalConfiguration.current
//    val screenHeight = configuration.screenHeightDp.dp
//    val contentTopPadding = screenHeight * 0.4f // Adjust overlap
//    val lazyListState = rememberLazyListState() // State for scroll offset
//    val backdropTargetHeight = screenHeight * 0.5f
//
//    Box(
//        modifier = modifier
//            .fillMaxSize()
//            .background(Color.Black)
//    ) {
//
//        AsyncImage(
//            model = ImageRequest.Builder(LocalContext.current)
//                .data(baseImageUrl + movie.backdropPath)
//                .crossfade(true)
//                .build(),
//            contentDescription = movie.title + " backdrop",
//            contentScale = ContentScale.Crop,
//            modifier = Modifier
//                .fillMaxWidth()
//                .fillMaxHeight(backdropHeightFraction) // Fill top 50% of the screen height
//                .align(Alignment.TopCenter)
//        )
//
//        Box(
//            modifier = Modifier
//                .align(Alignment.TopCenter)
//                .fillMaxWidth()
//                .fillMaxHeight(0.5f)
//                .background(
//                    Brush.verticalGradient(
//                        colors = listOf(Color.Transparent, Color.Transparent, Color.Black),
//                        startY = 400f
//                    )
//                )
//        )
//
//        // --- SCROLLABLE CONTENT COLUMN ---
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(top = contentTopPadding) // Start content below overlap area
//                .verticalScroll(rememberScrollState()) // Make the whole column scrollable
//                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
//                .background(Color.Black) // Background for the content area
//        ) {
//            // --- Section 1: Core Info ---
//            Column(modifier = Modifier.padding(16.dp)) { // Padding for core info section
//                Text(
//                    "MOVIE",
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.padding(bottom = 4.dp)
//                )
//                Row( // Title Row
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.Top // Align content to top
//                ) {
//                    Text(
//                        text = movie.title ?: "No Title",
//                        style = MaterialTheme.typography.headlineMedium,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.White,
//                        modifier = Modifier
//                            .weight(1f)
//                            .padding(end = 8.dp) // Allow text to wrap
//                    )
//                    // Favorite button moved from here
//                }
//                Spacer(modifier = Modifier.height(4.dp))
//                Text( /* Genres */ movie.genres?.mapNotNull { it?.name }?.joinToString(", ") ?: "",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = Color.Gray
//                )
//                Spacer(modifier = Modifier.height(12.dp))
//                Text( /* Overview */ movie.overview ?: "No overview available.",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = Color.White.copy(alpha = 0.8f)
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) { /* Metadata Badges */
//                    movie.releaseDate?.let { MetadataBadge(text = it) }
//                    movie.voteAverage?.let {
//                        MetadataBadge(
//                            text = String.format(Locale.JAPAN, "%.1f", it),
//                            icon = Icons.Default.Star
//                        )
//                    }
//                    movie.runtime?.let {
//                        MetadataBadge(
//                            text = formatDuration(it),
//                            icon = Icons.Default.Schedule
//                        )
//                    }
//                }
//
//                // --- Favorite and Share Buttons Row ---
//                Spacer(modifier = Modifier.height(24.dp)) // Add space before buttons
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.Start, // Align buttons to the start
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // --- Favorite Button ---
//                    Button( // Using a Button for potentially clearer visual cue
//                        onClick = onToggleFavorite,
//                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = if (isFavorite) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
//                            contentColor = if (isFavorite) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
//                        ),
//                        shape = CircleShape // Make it rounded
//                    ) {
//                        Icon(
//                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
//                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
//                            modifier = Modifier.size(20.dp) // Adjust icon size if needed
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(if (isFavorite) "Favorited" else "Add Favorite")
//                    }
//
//                    Spacer(modifier = Modifier.width(16.dp)) // Space between buttons
//
//                    // --- Share Button ---
//                    OutlinedButton( // Using OutlinedButton for visual difference
//                        onClick = onShareClick,
//                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
//                        shape = CircleShape, // Make it rounded
//                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Share,
//                            contentDescription = "Share Movie",
//                            modifier = Modifier.size(20.dp) // Adjust icon size if needed
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text("Share")
//                    }
//                } // End Buttons Row
//
//            } // End Core Info Column
//
//            // --- Section 2: Where to Watch ---
//            val flatrate = movie.flatrateProviders ?: emptyList()
//            val rent = movie.rentProviders ?: emptyList()
//            val buy = movie.buyProviders ?: emptyList()
//            val watchLink = movie.watchProvidersLink // Get the main TMDB watch link
//            val hasAnyProvider = flatrate.isNotEmpty() || rent.isNotEmpty() || buy.isNotEmpty()
//
//            // Only show the section if there are providers OR a general link
//            if (hasAnyProvider || !watchLink.isNullOrBlank()) {
//                Divider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp), thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.5f))
//                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
//                    // Main Title for the Section
//                    Row(
//                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Text(
//                            "Where to Watch",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.White
//                        )
//                        // Show "See All Options" button only if the main link exists
//                        if (!watchLink.isNullOrBlank()) {
//                            TextButton(onClick = { onWatchProviderClick(watchLink) }) {
//                                Text("See All Options")
//                            }
//                        }
//                    }
//
//                    // --- Streaming Section ---
//                    if (flatrate.isNotEmpty()) {
//                        Text(
//                            "Stream",
//                            style = MaterialTheme.typography.titleSmall,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.White.copy(alpha = 0.8f),
//                            modifier = Modifier.padding(bottom = 8.dp)
//                        )
//                        WatchProviderRow(
//                            providers = flatrate,
//                            // Click action still opens the main TMDB watch link for simplicity
//                            onProviderClick = {
//                                if (watchLink != null) {
//                                    onWatchProviderClick(watchLink)
//                                }
//                            }
//                        )
//                        Spacer(modifier = Modifier.height(16.dp)) // Space after streaming row
//                    }
//
//                    // --- Rent Section ---
//                    if (rent.isNotEmpty()) {
//                        Text(
//                            "Rent",
//                            style = MaterialTheme.typography.titleSmall,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.White.copy(alpha = 0.8f),
//                            modifier = Modifier.padding(bottom = 8.dp)
//                        )
//                        WatchProviderRow(
//                            providers = rent,
//                            onProviderClick = {
//                                if (watchLink != null) {
//                                    onWatchProviderClick(watchLink)
//                                }
//                            }
//                        )
//                        Spacer(modifier = Modifier.height(16.dp)) // Space after rent row
//                    }
//
//                    // --- Buy Section ---
//                    if (buy.isNotEmpty()) {
//                        Text(
//                            "Buy",
//                            style = MaterialTheme.typography.titleSmall,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.White.copy(alpha = 0.8f),
//                            modifier = Modifier.padding(bottom = 8.dp)
//                        )
//                        WatchProviderRow(
//                            providers = buy,
//                            onProviderClick = {
//                                if (watchLink != null) {
//                                    onWatchProviderClick(watchLink)
//                                }
//                            }
//                        )
//                        Spacer(modifier = Modifier.height(16.dp)) // Space after buy row
//                    }
//
//                    // Message if no specific providers were found but link exists
//                    if (!hasAnyProvider && !watchLink.isNullOrBlank()) {
//                        Text(
//                            "Specific providers not listed for your region. Check TMDB for options.",
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = Color.Gray,
//                            modifier = Modifier.padding(top = 8.dp)
//                        )
//                    }
//                }
//            } // End Where to Watch section
//
//            // --- Section 2: Cast (Example) ---
//            // CastContent(cast = movie.cast, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
//            Spacer(modifier = Modifier.height(16.dp)) // Space before next section
//
//            // --- Section 3: Similar Movies (Example) ---
//            // SimilarMoviesContent(movies = movie.similarMovies, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
//            Spacer(modifier = Modifier.height(16.dp)) // Space before next section
//
//            // Add other sections like Reviews directly if needed
//
//            Spacer(modifier = Modifier.height(32.dp)) // Add final padding at the bottom of scrollable content
//
//        } // End Scrollable Content Column
//
//
//        // --- Top Bar with Back Button ONLY ---
//        // Drawn last in Box to be on top
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .statusBarsPadding() // Add padding for status bar
//                .padding(horizontal = 8.dp, vertical = 8.dp)
//                // --- Add explicit alignment ---
//                .align(Alignment.TopStart), // Align this Row to the top-start of the parent Box
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.Start
//        ) {
//            // Back Button (Aligned Start)
//            IconButton(
//                onClick = onNavigateUp,
//                modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
//            ) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                    contentDescription = "Back",
//                    tint = Color.White
//                )
//            }
//            // Removed Share and Favorite buttons Row from here
//        } // End Top Bar Row
//
//    } // End Root Box
//}

/**
 * Displays a horizontal row of watch provider logos.
 * Clicking anywhere on the row triggers the provided callback.
 */
@Composable
fun WatchProviderRow(
    providers: List<WatchProvider>,
    onProviderClick: () -> Unit, // Single click action for the whole row
    modifier: Modifier = Modifier
) {
    val logoBaseUrl = "https://image.tmdb.org/t/p/w92" // Logo size

    // Sort providers by display_priority for potentially better ordering
    val sortedProviders = providers.sortedBy { it.displayPriority }

    // Use clickable Box or Row to open the main link
    Box(
        // Using Box allows background/clip for the whole clickable area easily
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)) // Optional: round corners for the row
            .clickable(onClick = onProviderClick) // Click anywhere on the row triggers the callback
            .padding(vertical = 8.dp), // Padding inside the clickable area
    ) {
        // LazyRow to display logos horizontally, allowing scrolling if many providers
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp), // Padding for logos inside the row
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(sortedProviders, key = { it.providerId ?: -1 }) { provider ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(logoBaseUrl + provider.logoPath)
                        .crossfade(true)
                        .build(),
                    contentDescription = provider.providerName, // Accessibility
                    modifier = Modifier
                        .size(40.dp) // Size of the logo
                        .clip(RoundedCornerShape(8.dp)) // Rounded logo corners
                )
            }
        }
    }
}

@Composable
fun MetadataBadge(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray, // Or onSurfaceVariant
                modifier = Modifier.size(16.dp) // Adjust size
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            color = Color.Gray, // Or onSurfaceVariant
            fontSize = 12.sp, // Adjust size
            fontWeight = FontWeight.Medium
        )
    }
}

fun formatDuration(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours > 0 && mins > 0 -> "${hours}h ${mins}min"
        hours > 0 -> "${hours}h"
        else -> "${mins}min"
    }
}

/**
 * Helper function to create and launch the Android Share Intent.
 */
fun shareMovieDetails(context: Context, movieTitle: String?, movieId: Int?, imdbId: String?) {
    val tmdbBaseUrl = "https://www.themoviedb.org/movie/"
    val imdbBaseUrl = "https://www.imdb.com/title/"

    // Construct the text to share
    val shareText = buildString {
        append("Check out this movie: ${movieTitle ?: "Unknown Movie"}\n\n")
        // Prefer IMDb link if available, otherwise TMDB link
        val link = when {
            !imdbId.isNullOrBlank() -> imdbBaseUrl + imdbId
            movieId != null -> tmdbBaseUrl + movieId
            else -> null
        }
        link?.let { append("Find out more: $it") }
    }

    // Create the Intent
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain" // Set MIME type
        putExtra(
            Intent.EXTRA_SUBJECT,
            movieTitle ?: "Movie Recommendation"
        ) // Optional: Subject for email apps
        putExtra(Intent.EXTRA_TEXT, shareText) // The actual text content to share
    }

    // Create and start the Chooser
    val chooserIntent = Intent.createChooser(shareIntent, "Share Movie via")
    context.startActivity(chooserIntent)
}

/**
 * Helper function to launch an Intent to view a URL (e.g., TMDB watch link or provider website).
 */
fun openUrl(context: Context, url: String?) {
    if (url.isNullOrBlank()) {
        println("Warning: Attempted to open a null or blank URL.")
        return
    }
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle exceptions, e.g., no browser installed
        println("Error opening URL '$url': $e")
        // Optionally show a Toast message to the user
    }
}